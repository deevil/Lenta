package com.lenta.bp9.features.delegates

import android.content.Context
import com.lenta.bp9.R
import com.lenta.bp9.data.BarcodeParser
import com.lenta.bp9.features.loading.tasks.TaskListLoadingMode
import com.lenta.bp9.model.task.MarkingGoodsRegime
import com.lenta.bp9.model.task.getMarkingGoodsRegime
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.MarkType
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.model.task.TaskType
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.platform.requestCodeAddGoodsSurplus
import com.lenta.bp9.platform.requestCodeTypeBarCode
import com.lenta.bp9.platform.requestCodeTypeSap
import com.lenta.bp9.repos.IRepoInMemoryHolder
import com.lenta.bp9.requests.network.ZmpUtzGrz31V001NetRequest
import com.lenta.bp9.requests.network.ZmpUtzGrz31V001Params
import com.lenta.bp9.requests.network.ZmpUtzGrz31V001Result
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.resources.dao_ext.getEanInfoFromMaterial
import com.lenta.shared.fmp.resources.dao_ext.getProductInfoByMaterial
import com.lenta.shared.fmp.resources.slow.ZfmpUtz48V001
import com.lenta.shared.fmp.resources.slow.ZmpUtz25V001
import com.lenta.shared.models.core.*
import com.lenta.shared.requests.combined.scan_info.ScanInfoRequest
import com.lenta.shared.requests.combined.scan_info.ScanInfoRequestParams
import com.lenta.shared.requests.combined.scan_info.ScanInfoResult
import com.lenta.shared.utilities.Logg
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.*
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class SearchProductDelegate @Inject constructor(
        private val hyperHive: HyperHive,
        private val screenNavigator: IScreenNavigator,
        private val scanInfoRequest: ScanInfoRequest,
        private val taskManager: IReceivingTaskManager,
        private val sessionInfo: ISessionInfo,
        private val context: Context,
        private val zmpUtzGrz31V001NetRequest: ZmpUtzGrz31V001NetRequest,
        private val repoInMemoryHolder: IRepoInMemoryHolder
) : CoroutineScope {

    private var scanInfoResult: ScanInfoResult? = null

    private var searchFromScan: Boolean = false

    private var isDiscrepancy: Boolean = false

    private var scanResultHandler: ((ScanInfoResult?) -> Boolean)? = null

    private var codeWith12Digits: String? = null

    private val job = SupervisorJob()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private var barcodeData: BarcodeData? = null

    fun copy(): SearchProductDelegate {
        val searchProductDelegate = SearchProductDelegate(
                hyperHive,
                screenNavigator,
                scanInfoRequest,
                taskManager,
                sessionInfo,
                context,
                zmpUtzGrz31V001NetRequest,
                repoInMemoryHolder
        )
        searchProductDelegate.init(scanResultHandler)
        return searchProductDelegate
    }

    fun init(scanResultHandler: ((ScanInfoResult?) -> Boolean)? = null) {
        this.scanResultHandler = scanResultHandler
    }

    fun searchCode(code: String, fromScan: Boolean, isBarCode: Boolean? = null, isDiscrepancy: Boolean? = false) {
        searchFromScan = fromScan
        this.isDiscrepancy = isDiscrepancy == true
        if (isBarCode == null && code.length == SEARCH_CODE_LENGTH) {
            codeWith12Digits = code
            screenNavigator.openSelectTypeCodeScreen(requestCodeTypeSap, requestCodeTypeBarCode)
            return
        }

        launch {
            screenNavigator.showProgress(scanInfoRequest)
            barcodeData = BarcodeParser().getBarcodeData(code)
            scanInfoRequest(
                    ScanInfoRequestParams(
                            number = code,
                            tkNumber = sessionInfo.market.orEmpty(),
                            fromScan = fromScan,
                            isBarCode = isBarCode
                    )
            )
                    .either(::handleFailure, ::handleSearchSuccess)
            screenNavigator.hideProgress()
        }
    }

    private fun handleSearchSuccess(scanInfoResult: ScanInfoResult) {
        this.scanInfoResult = scanInfoResult
        searchProduct()
    }

    private fun handleFailure(failure: Failure) {
        screenNavigator.openAlertScreen(failure, "97")
    }

    fun handleResultCode(code: Int?, isDiscrepancy: Boolean? = false): Boolean {
        return when (code) {
            requestCodeTypeSap -> {
                searchCode(
                        code = "$BARCODE_PREFIX${codeWith12Digits?.takeLast(LAST_BARCODE_COUNT)}",
                        fromScan = false,
                        isBarCode = false,
                        isDiscrepancy = isDiscrepancy == true
                )
                codeWith12Digits = null
                true
            }
            requestCodeTypeBarCode -> {
                searchCode(
                        code = codeWith12Digits.orEmpty(),
                        fromScan = false,
                        isBarCode = true,
                        isDiscrepancy = isDiscrepancy == true
                )
                codeWith12Digits = null
                true
            }
            requestCodeAddGoodsSurplus -> {
                addGoodsSurplus()
                true
            }
            else -> false
        }

    }

    private fun addGoodsSurplus() {
        launch {
            screenNavigator.showProgress(scanInfoRequest)
            taskManager.getReceivingTask()?.let { task ->
                val params = ZmpUtzGrz31V001Params(
                        taskNumber = task.taskHeader.taskNumber,
                        materialNumber = scanInfoResult?.productInfo?.materialNumber.orEmpty(),
                        boxNumber = "",
                        stampCode = ""
                )
                zmpUtzGrz31V001NetRequest(params).either(::handleFailure, ::handleSuccessAddGoodsSurplus)
            }
            screenNavigator.hideProgress()
        }
    }

    private fun handleSuccessAddGoodsSurplus(result: ZmpUtzGrz31V001Result) = launch {
        Logg.d { "AddGoodsSurplus ${result}" }
        repoInMemoryHolder.manufacturers.value = result.manufacturers
        val materialInfo = withContext(Dispatchers.IO) { ZfmpUtz48V001(hyperHive).getProductInfoByMaterial(result.productSurplusDataPGE.materialNumber) }
        val eanInfo = withContext(Dispatchers.IO) { ZmpUtz25V001(hyperHive).getEanInfoFromMaterial(result.productSurplusDataPGE.materialNumber) }
        val isAlcoProduct = getIsAlcoProduct(result)
        val isExcProduct = getIsExcProduct(result)
        val goodsSurplus = TaskProductInfo(
                materialNumber = result.productSurplusDataPGE.materialNumber,
                description = result.productSurplusDataPGE.materialName,
                uom = scanInfoResult?.productInfo?.uom ?: Uom.DEFAULT,
                type = getProductType(isAlco = isAlcoProduct, isExcise = isExcProduct),
                isSet = scanInfoResult?.productInfo?.isSet ?: false,
                sectionId = scanInfoResult?.productInfo?.sectionId.orEmpty(),
                matrixType = scanInfoResult?.productInfo?.matrixType ?: MatrixType.Unknown,
                materialType = scanInfoResult?.productInfo?.materialType.orEmpty(),
                origQuantity = DEFAULT_DOUBLE_VALUE,
                orderQuantity = DEFAULT_DOUBLE_VALUE,
                quantityCapitalized = DEFAULT_DOUBLE_VALUE,
                purchaseOrderUnits = Uom.DEFAULT,
                overdToleranceLimit = DEFAULT_DOUBLE_VALUE,
                underdToleranceLimit = DEFAULT_DOUBLE_VALUE,
                upLimitCondAmount = DEFAULT_DOUBLE_VALUE,
                quantityInvest = result.productSurplusDataPGE.quantityInvestments,
                roundingSurplus = DEFAULT_DOUBLE_VALUE,
                roundingShortages = DEFAULT_DOUBLE_VALUE,
                isNoEAN = false,
                isWithoutRecount = false,
                isUFF = false,
                isNotEdit = false,
                generalShelfLife = DEFAULT_INT_VALUE,
                remainingShelfLife = DEFAULT_INT_VALUE,
                isRus = result.productSurplusDataPGE.isRus == MARKER_OF_AVAILABLE,
                isBoxFl = false,
                isMarkFl = getProductType(isAlco = isAlcoProduct, isExcise = isExcProduct) == ProductType.ExciseAlcohol, //елси это алкоголльный акциз, то ставим признак марочного товара, чтобы обрабатывать как марочный для этих товаров из тикета https://trello.com/c/WQg659Ww
                isVet = result.productSurplusDataPGE.isVet == MARKER_OF_AVAILABLE,
                numberBoxesControl = DEFAULT_INT_VALUE,
                numberStampsControl = DEFAULT_INT_VALUE,
                processingUnit = "",
                isGoodsAddedAsSurplus = true,
                mhdhbDays = materialInfo?.mhdhbDays ?: 0,
                mhdrzDays = materialInfo?.mhdrzDays ?: 0,
                markType = MarkType.None,
                isCountingBoxes = false,
                nestingInOneBlock = DEFAULT_DOUBLE_VALUE,
                isControlGTIN = false,
                isGrayZone = false,
                countPiecesBox = DEFAULT_INT_VALUE,
                numeratorConvertBaseUnitMeasure = eanInfo?.umrez?.toDouble() ?: 0.0,
                denominatorConvertBaseUnitMeasure = eanInfo?.umren?.toDouble() ?: 0.0,
                isZBatches = false,
                isNeedPrint = false,
                alternativeUnitMeasure = "",
                quantityAlternativeUnitMeasure = 0.0
        )
        taskManager
                .getReceivingTask()
                ?.taskRepository
                ?.getProducts()
                ?.addProduct(goodsSurplus)
        openProductScreen(taskProductInfo = goodsSurplus)
    }

    private fun getIsExcProduct(result: ZmpUtzGrz31V001Result): Boolean {
        return result.productSurplusDataPGE.isExc == MARKER_OF_AVAILABLE
    }

    private fun getIsAlcoProduct(result: ZmpUtzGrz31V001Result): Boolean {
        return result.productSurplusDataPGE.isAlco == MARKER_OF_AVAILABLE
    }

    private fun searchProduct() {
        Logg.d { "searchProduct ${scanInfoResult?.productInfo?.materialNumber}" }
        scanInfoResult?.let { infoResult ->
            val taskProductInfo = taskManager.getReceivingTask()?.taskRepository?.getProducts()?.findProduct(infoResult.productInfo.materialNumber)
            if (taskProductInfo == null) {
                if (taskManager.getReceivingTask()?.taskHeader?.taskType == TaskType.RecalculationCargoUnit) {
                    screenNavigator.openAddGoodsSurplusDialog(requestCodeAddGoodsSurplus) //эта проверка только для ПГЕ, карточки трелло https://trello.com/c/8P4mPlGN, https://trello.com/c/im9rJqrU, https://trello.com/c/WQg659Ww
                } else {
                    screenNavigator.openAlertGoodsNotInOrderScreen()
                }
                return
            }
            scanResultHandler?.let { handle ->
                if (handle(infoResult)) {
                    return
                }
            }
            openProductScreen(taskProductInfo)
        }
    }

    private fun openProductScreen(taskProductInfo: TaskProductInfo) {
        if (taskProductInfo.isNotEdit) {
            if (taskProductInfo.markType != MarkType.None) {
                screenNavigator.openMarkingGoodsDetailsScreen(taskProductInfo)
            } else {
                screenNavigator.openGoodsDetailsScreen(taskProductInfo)
            }
            return
        }

        //https://trello.com/c/NGsFfWgB маркированный товар
        if (taskProductInfo.markType != MarkType.None) {
            openMarkingProductScreen(taskProductInfo)
            return
        }

        /**Z-партии, логика пересчета вет товара не меняется если он является партионным.
        Z-партии ППП -> это IS_VET= пусто, IS_ZPARTS=X
        если IS_VET=X + IS_ZPARTS=X товар считается как обычный меркурианский в дополнение просто отображается признак z-партионного учета*/
        if (taskProductInfo.isZBatches && !taskProductInfo.isVet) {
            if (taskManager.getReceivingTask()?.taskHeader?.taskType == TaskType.DirectSupplier) {
                screenNavigator.openZBatchesInfoPPPScreen(taskProductInfo, isDiscrepancy, barcodeData)
            }

            if (taskManager.getReceivingTask()?.taskHeader?.taskType == TaskType.RecalculationCargoUnit) {
                screenNavigator.openZBatchesInfoPGEScreen(taskProductInfo, isDiscrepancy)
            }

            return
        }

        when (taskProductInfo.type) {
            ProductType.General -> openGeneralProductScreen(taskProductInfo)
            ProductType.ExciseAlcohol -> openExciseAlcoholProductScreen(taskProductInfo)
            ProductType.NonExciseAlcohol -> openNonExciseAlcoholProductScreen(taskProductInfo)
            else -> screenNavigator.openAlertUnknownGoodsTypeScreen() //сообщение о неизвестном типе товара
        }
    }

    private fun openMarkingProductScreen(taskProductInfo: TaskProductInfo) {
        when(getMarkingGoodsRegime(taskManager, taskProductInfo)) {
            MarkingGoodsRegime.UomStWithoutBoxes -> screenNavigator.openMarkingInfoScreen(taskProductInfo)
            MarkingGoodsRegime.UomStWithBoxes -> screenNavigator.openMarkingBoxInfoScreen(taskProductInfo)
            MarkingGoodsRegime.UomSTWithBoxesPGE -> screenNavigator.openMarkingBoxInfoPGEScreen(taskProductInfo)
            else -> screenNavigator.openInfoScreen(context.getString(R.string.data_retrieval_error))
        }
    }

    private fun openGeneralProductScreen(taskProductInfo: TaskProductInfo) {
        if (taskProductInfo.isVet &&
                //todo это условие прописано временно, т.к. на продакшене для ПГЕ и ПРЦ не реализована таблица ET_VET_DIFF, она приходит пустой  в 28 и 30 рестах, поэтому обрабатываем данные товары не как вет, а как обычные. Не делал условия для типов задания, чтобы если для других типов задания эта таблица будет пустая, то товары обрабатывались как обычные, а не веттовары
                !taskManager.getReceivingTask()?.taskRepository?.getMercuryDiscrepancies()?.getMercuryDiscrepancies().isNullOrEmpty()) {
            screenNavigator.openGoodsMercuryInfoScreen(taskProductInfo, isDiscrepancy, barcodeData)
        } else {
            if (taskManager.getReceivingTask()?.taskHeader?.taskType == TaskType.ShipmentPP) {
                screenNavigator.openGoodsInfoShipmentPPScreen(productInfo = taskProductInfo, isDiscrepancy = isDiscrepancy)
            } else {
                screenNavigator.openGoodsInfoScreen(productInfo = taskProductInfo, isDiscrepancy = isDiscrepancy)
            }
        }
    }

    private fun openExciseAlcoholProductScreen(taskProductInfo: TaskProductInfo) {
        val loadingMode = repoInMemoryHolder.taskList.value?.taskListLoadingMode
        when {
            taskProductInfo.isSet -> {
                screenNavigator.openInfoScreen(context.getString(R.string.data_retrieval_error)) //openNotImplementedScreenAlert("Информация о наборе")
                //screenNavigator.openSetsInfoScreen(taskProductInfo)
            }
            taskProductInfo.isBoxFl -> { //алкоголь, коробочный учет ППП https://trello.com/c/KbBbXj2t; коробочный учет ПГЕ https://trello.com/c/TzUSGIH7
                when (loadingMode) {
                    TaskListLoadingMode.Receiving -> screenNavigator.openExciseAlcoBoxAccInfoReceivingScreen(taskProductInfo)
                    TaskListLoadingMode.PGE -> screenNavigator.openExciseAlcoBoxAccInfoPGEScreen(taskProductInfo)
                    TaskListLoadingMode.Shipment -> screenNavigator.openInfoScreen(context.getString(R.string.data_retrieval_error)) //openNotImplementedScreenAlert("Информация о коробочном учете")
                    else -> screenNavigator.openAlertUnknownTaskTypeScreen() //сообщение о неизвестном типе задания
                }
            }
            taskProductInfo.isMarkFl -> { //алкоголь, марочный учет ПГЕ https://trello.com/c/Bx03dgxE;
                when (loadingMode) {
                    TaskListLoadingMode.Receiving -> screenNavigator.openInfoScreen(context.getString(R.string.data_retrieval_error)) //screenNavigator.openExciseAlcoStampAccInfoScreen(taskProductInfo) это экран для марочного учета ППП
                    TaskListLoadingMode.PGE -> screenNavigator.openExciseAlcoStampAccInfoPGEScreen(taskProductInfo)
                    TaskListLoadingMode.Shipment -> screenNavigator.openInfoScreen(context.getString(R.string.data_retrieval_error)) //openNotImplementedScreenAlert("Информация о марочном учете")
                    else -> screenNavigator.openAlertUnknownTaskTypeScreen() //сообщение о неизвестном типе задания
                }
            }
            else -> screenNavigator.openAlertUnknownGoodsTypeScreen() //сообщение о неизвестном типе товара
        }
    }

    private fun openNonExciseAlcoholProductScreen(taskProductInfo: TaskProductInfo) {
        //не акцизный алкоголь ППП https://trello.com/c/rmn2WFMD; ПГЕ https://trello.com/c/P9KBZcNB;
        when {
            taskProductInfo.isSet -> { //https://trello.com/c/yQ9jtjnZ
                when (repoInMemoryHolder.taskList.value?.taskListLoadingMode) {
                    TaskListLoadingMode.Receiving -> screenNavigator.openNonExciseSetsInfoReceivingScreen(productInfo = taskProductInfo, isDiscrepancy = isDiscrepancy)
                    TaskListLoadingMode.PGE -> screenNavigator.openNonExciseSetsInfoPGEScreen(productInfo = taskProductInfo, isDiscrepancy = isDiscrepancy)
                    TaskListLoadingMode.Shipment -> screenNavigator.openInfoScreen(context.getString(R.string.data_retrieval_error)) //openNotImplementedScreenAlert("Информация о не акцизном наборе")
                    else -> screenNavigator.openAlertUnknownTaskTypeScreen() //сообщение о неизвестном типе задания
                }
            }
            else -> {
                when (repoInMemoryHolder.taskList.value?.taskListLoadingMode) {
                    TaskListLoadingMode.Receiving -> screenNavigator.openNonExciseAlcoInfoReceivingScreen(productInfo = taskProductInfo, isDiscrepancy = isDiscrepancy)
                    TaskListLoadingMode.PGE -> screenNavigator.openNonExciseAlcoInfoPGEScreen(productInfo = taskProductInfo, isDiscrepancy = isDiscrepancy)
                    TaskListLoadingMode.Shipment -> screenNavigator.openInfoScreen(context.getString(R.string.data_retrieval_error)) //openNotImplementedScreenAlert("Информация о не акцизном алкоголе")
                    else -> screenNavigator.openAlertUnknownTaskTypeScreen() //сообщение о неизвестном типе задания
                }
            }
        }
    }

    companion object {
        private const val SEARCH_CODE_LENGTH = 12
        private const val LAST_BARCODE_COUNT = 6
        private const val BARCODE_PREFIX = "000000000000"
        private const val MARKER_OF_AVAILABLE = "X"
        private const val DEFAULT_DOUBLE_VALUE = "0.0"
        private const val DEFAULT_INT_VALUE = "0"
        const val UNIT_CODE_ST = "ST"
    }
}