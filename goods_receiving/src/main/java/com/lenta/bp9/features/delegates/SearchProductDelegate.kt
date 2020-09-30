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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class SearchProductDelegate @Inject constructor(
        private val hyperHive: HyperHive,
        private val screenNavigator: IScreenNavigator,
        private val scanInfoRequest: ScanInfoRequest,
        private val taskManager: IReceivingTaskManager,
        private val sessionInfo: ISessionInfo,
        private val context: Context,
        private val zmpUtzGrz31V001NetRequest: ZmpUtzGrz31V001NetRequest,
        private val repoInMemoryHolder: IRepoInMemoryHolder
) {

    private var scanInfoResult: ScanInfoResult? = null

    private var searchFromScan: Boolean = false

    private var isDiscrepancy: Boolean = false

    private lateinit var viewModelScope: () -> CoroutineScope

    private var scanResultHandler: ((ScanInfoResult?) -> Boolean)? = null

    private var codeWith12Digits: String? = null

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
        searchProductDelegate.init(viewModelScope, scanResultHandler)
        return searchProductDelegate
    }

    fun init(viewModelScope: () -> CoroutineScope, scanResultHandler: ((ScanInfoResult?) -> Boolean)? = null) {
        this.viewModelScope = viewModelScope
        this.scanResultHandler = scanResultHandler
    }

    fun searchCode(code: String, fromScan: Boolean, isBarCode: Boolean? = null, isDiscrepancy:  Boolean? = false) {
        searchFromScan = fromScan
        this.isDiscrepancy = isDiscrepancy!!
        if (isBarCode == null && code.length == 12) {
            codeWith12Digits = code
            screenNavigator.openSelectTypeCodeScreen(requestCodeTypeSap, requestCodeTypeBarCode)
            return
        }

        viewModelScope().launch {
            screenNavigator.showProgress(scanInfoRequest)
            barcodeData = BarcodeParser().getBarcodeData(code)
            scanInfoRequest(
                    ScanInfoRequestParams(
                            number = code,
                            tkNumber = sessionInfo.market ?: "",
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
                searchCode("000000000000${codeWith12Digits?.takeLast(6)}", fromScan = false, isBarCode = false, isDiscrepancy = isDiscrepancy!!)
                codeWith12Digits = null
                true
            }
            requestCodeTypeBarCode -> {
                searchCode(code = codeWith12Digits ?: "", fromScan = false, isBarCode = true, isDiscrepancy = isDiscrepancy!!)
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
        viewModelScope().launch {
            screenNavigator.showProgress(scanInfoRequest)
            taskManager.getReceivingTask()?.let { task ->
                val params = ZmpUtzGrz31V001Params(
                        taskNumber = task.taskHeader.taskNumber,
                        materialNumber = scanInfoResult?.productInfo?.materialNumber ?: "",
                        boxNumber = "",
                        stampCode = ""
                )
                zmpUtzGrz31V001NetRequest(params).either(::handleFailure, ::handleSuccessAddGoodsSurplus)
            }
            screenNavigator.hideProgress()

        }
    }

    private fun handleSuccessAddGoodsSurplus(result: ZmpUtzGrz31V001Result) {
        Logg.d { "AddGoodsSurplus ${result}" }
        repoInMemoryHolder.manufacturers.value = result.manufacturers
        val materialInfo = ZfmpUtz48V001(hyperHive).getProductInfoByMaterial(result.productSurplusDataPGE.materialNumber)
        val eanInfo = ZmpUtz25V001(hyperHive).getEanInfoFromMaterial(result.productSurplusDataPGE.materialNumber)
        val goodsSurplus = TaskProductInfo(
                materialNumber = result.productSurplusDataPGE.materialNumber,
                description = result.productSurplusDataPGE.materialName,
                uom = scanInfoResult?.productInfo?.uom ?: Uom(code = "", name = ""),
                type = getProductType(isAlco = result.productSurplusDataPGE.isAlco == "X", isExcise = result.productSurplusDataPGE.isExc == "X"),
                isSet = scanInfoResult?.productInfo?.isSet ?: false,
                sectionId = scanInfoResult?.productInfo?.sectionId.orEmpty(),
                matrixType = scanInfoResult?.productInfo?.matrixType ?: MatrixType.Unknown,
                materialType = scanInfoResult?.productInfo?.materialType.orEmpty(),
                origQuantity = "0.0",
                orderQuantity = "0.0",
                quantityCapitalized = "0.0",
                purchaseOrderUnits = Uom(code = "", name = ""),
                overdToleranceLimit = "0.0",
                underdToleranceLimit = "0.0",
                upLimitCondAmount = "0.0",
                quantityInvest = result.productSurplusDataPGE.quantityInvestments,
                roundingSurplus = "0.0",
                roundingShortages = "0.0",
                isNoEAN = false,
                isWithoutRecount = false,
                isUFF = false,
                isNotEdit = false,
                generalShelfLife = "0",
                remainingShelfLife = "0",
                isRus = result.productSurplusDataPGE.isRus == "X",
                isBoxFl = false,
                isMarkFl = getProductType(isAlco = result.productSurplusDataPGE.isAlco == "X", isExcise = result.productSurplusDataPGE.isExc == "X") == ProductType.ExciseAlcohol, //елси это алкоголльный акциз, то ставим признак марочного товара, чтобы обрабатывать как марочный для этих товаров из тикета https://trello.com/c/WQg659Ww
                isVet = result.productSurplusDataPGE.isVet == "X",
                numberBoxesControl = "0",
                numberStampsControl = "0",
                processingUnit = "",
                isGoodsAddedAsSurplus = true,
                mhdhbDays = materialInfo?.mhdhbDays ?: 0,
                mhdrzDays = materialInfo?.mhdrzDays ?: 0,
                markType = MarkType.None,
                isCountingBoxes = false,
                nestingInOneBlock = "0.0",
                isControlGTIN = false,
                isGrayZone = false,
                countPiecesBox = "0",
                numeratorConvertBaseUnitMeasure = eanInfo?.umrez?.toDouble() ?: 0.0,
                denominatorConvertBaseUnitMeasure = eanInfo?.umren?.toDouble() ?: 0.0,
                isZBatches = false,
                isNeedPrint = false
        )
        taskManager
                .getReceivingTask()
                ?.taskRepository
                ?.getProducts()
                ?.addProduct(goodsSurplus)
        openProductScreen(taskProductInfo = goodsSurplus)
    }

    private fun searchProduct() {
        Logg.d { "searchProduct ${scanInfoResult?.productInfo?.materialNumber}" }
        scanInfoResult?.let { infoResult ->
            val taskProductInfo = taskManager.getReceivingTask()!!.taskRepository.getProducts().findProduct(infoResult.productInfo.materialNumber)
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
            screenNavigator.openZBatchesInfoPPPScreen(taskProductInfo, isDiscrepancy, barcodeData)
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
        const val UNIT_CODE_ST = "ST"
    }

}