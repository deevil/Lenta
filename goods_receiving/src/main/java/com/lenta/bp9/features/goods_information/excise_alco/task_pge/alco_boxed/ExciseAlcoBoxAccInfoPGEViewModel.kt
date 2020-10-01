package com.lenta.bp9.features.goods_information.excise_alco.task_pge.alco_boxed

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.R
import com.lenta.bp9.features.delegates.SearchProductDelegate
import com.lenta.bp9.model.processing.ProcessExciseAlcoBoxAccPGEService
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.platform.TypeDiscrepanciesConstants
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.repos.IDataBaseRepo
import com.lenta.bp9.requests.network.ZmpUtzGrz31V001NetRequest
import com.lenta.bp9.requests.network.ZmpUtzGrz31V001Params
import com.lenta.bp9.requests.network.ZmpUtzGrz31V001Result
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.resources.dao_ext.getProductInfoByMaterial
import com.lenta.shared.fmp.resources.slow.ZfmpUtz48V001
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.combined.scan_info.ScanInfoResult
import com.lenta.shared.requests.combined.scan_info.pojo.QualityInfo
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toStringFormatted
import com.lenta.shared.utilities.orIfNull
import com.lenta.shared.view.OnPositionClickListener
import com.mobrun.plugin.api.HyperHive
import javax.inject.Inject

class ExciseAlcoBoxAccInfoPGEViewModel : CoreViewModel(), OnPositionClickListener {

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    @Inject
    lateinit var taskManager: IReceivingTaskManager

    @Inject
    lateinit var processExciseAlcoBoxAccPGEService: ProcessExciseAlcoBoxAccPGEService

    @Inject
    lateinit var dataBase: IDataBaseRepo

    @Inject
    lateinit var searchProductDelegate: SearchProductDelegate

    @Inject
    lateinit var context: Context

    @Inject
    lateinit var hyperHive: HyperHive

    @Inject
    lateinit var zmpUtzGrz31V001NetRequest: ZmpUtzGrz31V001NetRequest

    private val zfmpUtz48V001: ZfmpUtz48V001 by lazy {
        ZfmpUtz48V001(hyperHive)
    }

    val productInfo: MutableLiveData<TaskProductInfo> = MutableLiveData()
    val isEizUnit: MutableLiveData<Boolean> by lazy {
        MutableLiveData(productInfo.value?.purchaseOrderUnits?.code != productInfo.value?.uom?.code)
    }
    val tvAccept: MutableLiveData<String> by lazy {
        MutableLiveData(context.getString(R.string.accept, "${productInfo.value?.purchaseOrderUnits?.name}=${productInfo.value?.quantityInvest?.toDouble().toStringFormatted()} ${productInfo.value?.uom?.name}"))
    }
    val spinQuality: MutableLiveData<List<String>> = MutableLiveData()
    val spinQualitySelectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val suffix: MutableLiveData<String> = MutableLiveData()
    val requestFocusToCount: MutableLiveData<Boolean> = MutableLiveData(false)
    val isDefect: MutableLiveData<Boolean> = spinQualitySelectedPosition.map {
        !(qualityInfo.value?.get(it!!)?.code == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM
                || qualityInfo.value?.get(it!!)?.code == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_PGE_SURPLUS)
    }

    private val qualityInfo: MutableLiveData<List<QualityInfo>> = MutableLiveData()

    val count: MutableLiveData<String> = MutableLiveData("0")
    private val countValue: MutableLiveData<Double> = count.map { it?.toDoubleOrNull() ?: 0.0 }

    val acceptTotalCount: MutableLiveData<Double> = countValue.combineLatest(spinQualitySelectedPosition).map {
        val countAccept = taskManager.getReceivingTask()!!.taskRepository.getProductsDiscrepancies().getCountAcceptOfProductPGE(productInfo.value!!)

        if (qualityInfo.value?.get(it!!.second)?.code == "1" || qualityInfo.value?.get(it!!.second)?.code == "2") {
            convertEizToBei() + countAccept
        } else {
            countAccept
        }
    }

    val acceptTotalCountWithUom: MutableLiveData<String> = acceptTotalCount.map {
        val countAccept = taskManager.getReceivingTask()!!.taskRepository.getProductsDiscrepancies().getCountAcceptOfProductPGE(productInfo.value!!)
        when {
            (it ?: 0.0) > 0.0 -> {
                "+ ${it.toStringFormatted()} ${productInfo.value?.uom?.name}"
            }
            else -> { //если было введено отрицательное значение
                "${if (countAccept > 0.0) "+ " + countAccept.toStringFormatted() else countAccept.toStringFormatted()} ${productInfo.value?.uom?.name}"
            }
        }
    }

    val refusalTotalCount: MutableLiveData<Double> = countValue.combineLatest(spinQualitySelectedPosition).map {
        val countRefusal = taskManager.getReceivingTask()!!.taskRepository.getProductsDiscrepancies().getCountRefusalOfProductPGE(productInfo.value!!)
        if (qualityInfo.value?.get(it!!.second)?.code == "3" || qualityInfo.value?.get(it!!.second)?.code == "4" || qualityInfo.value?.get(it!!.second)?.code == "5") {
            convertEizToBei() + countRefusal
        } else {
            countRefusal
        }
    }

    val refusalTotalCountWithUom: MutableLiveData<String> = refusalTotalCount.map {
        val countRefusal = taskManager.getReceivingTask()!!.taskRepository.getProductsDiscrepancies().getCountRefusalOfProductPGE(productInfo.value!!)

        if ((it ?: 0.0) > 0.0) {
            "- ${it.toStringFormatted()} ${productInfo.value?.uom?.name}"
        } else { //если было введено отрицательное значение
            "${if (countRefusal > 0.0) "- " + countRefusal.toStringFormatted() else countRefusal.toStringFormatted()} ${productInfo.value?.uom?.name}"
        }
    }

    val checkStampControlVisibility: MutableLiveData<Boolean> = MutableLiveData()

    val tvStampControlVal: MutableLiveData<String> = countValue.combineLatest(spinQualitySelectedPosition).map {
        //ПГЕ https://trello.com/c/TzUSGIH7
        if (qualityInfo.value?.get(it?.second ?: 0)?.code == "1") {
            if ((productInfo.value?.numberBoxesControl?.toInt() == 0 && productInfo.value?.numberStampsControl?.toInt() == 0) ||
                    ((it?.first ?: 0.0) <= 0.0)) {
                checkStampControlVisibility.value = false
                context.getString(R.string.not_required)
            } else {
                checkStampControlVisibility.value = true
                if ((it?.first ?: 0.0) < (productInfo.value?.numberBoxesControl?.toDouble()
                                ?: 0.0)) {
                    "${it?.first.toStringFormatted()} кор x ${productInfo.value?.numberStampsControl?.toDouble().toStringFormatted()}"
                } else {
                    "${productInfo.value?.numberBoxesControl?.toDouble().toStringFormatted()} кор x ${productInfo.value?.numberStampsControl?.toDouble().toStringFormatted()}"
                }
            }
        } else {
            "" //это поле отображается только при выбранной категории "Норма"
        }
    }

    val checkStampControl: MutableLiveData<Boolean> = checkStampControlVisibility.map {
        //ПГЕ https://trello.com/c/TzUSGIH7
        taskManager.getReceivingTask()?.controlExciseStampsOfProduct(productInfo.value!!)
    }

    val checkBoxControlVisibility: MutableLiveData<Boolean> = MutableLiveData()

    val tvBoxControlVal: MutableLiveData<String> = countValue.combineLatest(spinQualitySelectedPosition).map {
        //ПГЕ https://trello.com/c/TzUSGIH7
        if (qualityInfo.value?.get(it?.second ?: 0)?.code == "1") {
            if ((productInfo.value?.numberBoxesControl?.toInt() == 0 && productInfo.value?.numberStampsControl?.toInt() == 0) ||
                    ((it?.first ?: 0.0) <= 0.0)) {
                checkBoxControlVisibility.value = false
                context.getString(R.string.not_required)
            } else {
                checkBoxControlVisibility.value = true
                if ((it?.first ?: 0.0) < (productInfo.value?.numberBoxesControl?.toDouble()
                                ?: 0.0)) {
                    "${taskManager.getReceivingTask()?.countBoxesPassedControlOfProduct(productInfo.value!!)} из ${it?.first.toStringFormatted()}"
                } else {
                    "${taskManager.getReceivingTask()?.countBoxesPassedControlOfProduct(productInfo.value!!)} из ${productInfo.value?.numberBoxesControl?.toDouble().toStringFormatted()}"
                }
            }
        } else {
            "" //это поле отображается только при выбранной категории "Норма"
        }
    }

    val checkBoxControl: MutableLiveData<Boolean> = checkBoxControlVisibility.map {
        //ПГЕ https://trello.com/c/TzUSGIH7
        taskManager.getReceivingTask()?.controlBoxesOfProduct(productInfo.value!!)
    }

    val enabledApplyButton: MutableLiveData<Boolean> = countValue.map {
        (it ?: 0.0) > 0.0
    }

    private val currentQualityInfoCode: String
        get() {
            val position = spinQualitySelectedPosition.value ?: -1
            return position
                    .takeIf { it >= 0 }
                    ?.run {
                        qualityInfo.value
                                ?.takeIf { it.isNotEmpty() }
                                ?.run { this[position].code }
                                .orEmpty()
                    }
                    .orEmpty()
        }


    private val scannedBoxNumber: MutableLiveData<String> = MutableLiveData("")

    init {
        launchUITryCatch {
            productInfo.value
                    ?.let {
                        if (processExciseAlcoBoxAccPGEService.newProcessExciseAlcoBoxPGEService(it) == null) {
                            screenNavigator.goBackAndShowAlertWrongProductType()
                            return@launchUITryCatch
                        }
                    }
                    .orIfNull {screenNavigator.goBack()
                        screenNavigator.goBackAndShowAlertWrongProductType()
                        return@launchUITryCatch
                    }

            searchProductDelegate.init(scanResultHandler = this@ExciseAlcoBoxAccInfoPGEViewModel::handleProductSearchResult)

            suffix.value = productInfo.value?.purchaseOrderUnits?.name
            qualityInfo.value = dataBase.getQualityInfoNormPGE()
            spinQuality.value = qualityInfo.value?.map {
                it.name
            }

            count.value = processExciseAlcoBoxAccPGEService.getInitialCount().toStringFormatted()
        }
    }

    fun onResume() {
        count.value = processExciseAlcoBoxAccPGEService.getInitialCount().toStringFormatted()
        requestFocusToCount.value = true
    }

    private fun handleProductSearchResult(@Suppress("UNUSED_PARAMETER") scanInfoResult: ScanInfoResult?): Boolean {
        screenNavigator.goBack()
        return false
    }

    fun onClickBoxes() {
        processExciseAlcoBoxAccPGEService.setInitialCount(countValue.value ?: 0.0)
        processExciseAlcoBoxAccPGEService.setCountAcceptRefusal((acceptTotalCount.value ?: 0.0) + (refusalTotalCount.value ?: 0.0))
        screenNavigator.openExciseAlcoBoxListPGEScreen(
                productInfo = productInfo.value!!,
                selectQualityCode = qualityInfo.value!![spinQualitySelectedPosition.value!!].code
        )
    }

    fun onClickDetails() {
        productInfo.value
                ?.also {
                    screenNavigator.openGoodsDetailsScreen(
                            productInfo = it,
                            isScreenPGEBoxAlcoInfo = true
                    )
                }
    }

    fun onClickAdd(): Boolean {
        return if (processExciseAlcoBoxAccPGEService.overLimit(countValue.value!!)) {
            screenNavigator.openAlertOverLimitAlcoPGEScreen( //ПГЕ https://trello.com/c/TzUSGIH7
                    nextCallbackFunc = {
                        //https://trello.com/c/HiTBZHLJ (12. ПГЕ. Излишки. Превышено кол-во. Режим поиска излишка на списке коробок и карточки товара (КОР учет))
                        processExciseAlcoBoxAccPGEService.setInitialCount(countValue.value ?: 0.0)
                        processExciseAlcoBoxAccPGEService.setCountAcceptRefusal((acceptTotalCount.value ?: 0.0) + (refusalTotalCount.value ?: 0.0))
                        screenNavigator.openExciseAlcoBoxListPGEScreen(
                                productInfo = productInfo.value!!,
                                selectQualityCode = currentQualityInfoCode
                        )
                    }
            )
            false
        } else {
            processExciseAlcoBoxAccPGEService.addProduct(convertEizToBei().toString(), currentQualityInfoCode)
            true
        }
    }

    fun onClickApply() {
        if (onClickAdd()) screenNavigator.goBack()
    }

    fun onScanResult(data: String) {
        when (data.length) {//ПГЕ https://trello.com/c/TzUSGIH7
            68, 150 -> {
                if (processExciseAlcoBoxAccPGEService.getCountBoxOfProductOfDiscrepancies(data) >= ((acceptTotalCount.value ?: 0.0) + (refusalTotalCount.value ?: 0.0)) ) { //это условие добавлено здесь, т.к. на WM оно тоже есть
                    screenNavigator.openAlertRequiredQuantityBoxesAlreadyProcessedScreen() //Необходимое количество коробок уже обработано
                } else {
                    val exciseStampInfo = processExciseAlcoBoxAccPGEService.searchExciseStamp(data)
                    if (exciseStampInfo == null) {
                        screenNavigator.openAlertScannedStampNotFoundTaskPGEScreen() //Отсканированная марка отсутвует в задании. Отсканируйте номер коробки, а затем номер марки для заявления излишка.
                    } else {
                        if (exciseStampInfo.materialNumber != productInfo.value!!.materialNumber) {
                            //Отсканированная марка принадлежит товару <SAP-код> <Название>"
                            screenNavigator.openAlertScannedStampBelongsAnotherProductScreen(exciseStampInfo.materialNumber.orEmpty(), zfmpUtz48V001.getProductInfoByMaterial(exciseStampInfo.materialNumber)?.name
                                    ?: "")
                        } else {
                            processExciseAlcoBoxAccPGEService.setInitialCount(countValue.value ?: 0.0)
                            processExciseAlcoBoxAccPGEService.setCountAcceptRefusal((acceptTotalCount.value ?: 0.0) + (refusalTotalCount.value ?: 0.0))
                            screenNavigator.openExciseAlcoBoxCardPGEScreen(
                                    productInfo = productInfo.value!!,
                                    boxInfo = null,
                                    massProcessingBoxesNumber = null,
                                    exciseStampInfo = exciseStampInfo,
                                    selectQualityCode = qualityInfo.value!![spinQualitySelectedPosition.value!!].code,
                                    isScan = true,
                                    isBoxNotIncludedInNetworkLenta = false
                            )
                        }
                    }
                }
            }
            26 -> {
                if (processExciseAlcoBoxAccPGEService.getCountBoxOfProductOfDiscrepancies(data) >= ((acceptTotalCount.value ?: 0.0) + (refusalTotalCount.value ?: 0.0)) ) {
                    screenNavigator.openAlertRequiredQuantityBoxesAlreadyProcessedScreen() //Необходимое количество коробок уже обработано
                } else {
                    val boxInfo = processExciseAlcoBoxAccPGEService.searchBox(boxNumber = data)
                    if (boxInfo == null) {
                        scannedBoxNumber.value = data
                        scannedBoxNotFound(data)
                    } else {
                        if (boxInfo.materialNumber != productInfo.value!!.materialNumber) {
                            //Отсканированная коробка принадлежит товару <SAP-код> <Название>
                            screenNavigator.openAlertScannedBoxBelongsAnotherProductScreen(materialNumber = boxInfo.materialNumber, materialName = zfmpUtz48V001.getProductInfoByMaterial(boxInfo.materialNumber)?.name
                                    ?: "")
                        } else {
                            processExciseAlcoBoxAccPGEService.setInitialCount(countValue.value ?: 0.0)
                            processExciseAlcoBoxAccPGEService.setCountAcceptRefusal((acceptTotalCount.value ?: 0.0) + (refusalTotalCount.value ?: 0.0))
                            screenNavigator.openExciseAlcoBoxCardPGEScreen(
                                    productInfo = productInfo.value!!,
                                    boxInfo = boxInfo,
                                    massProcessingBoxesNumber = null,
                                    exciseStampInfo = null,
                                    selectQualityCode = qualityInfo.value!![spinQualitySelectedPosition.value!!].code,
                                    isScan = true,
                                    isBoxNotIncludedInNetworkLenta = false
                            )
                        }
                    }
                }
            }
            else -> {
                if (enabledApplyButton.value == true) { //Функция доступна только при условии, что доступна кнопка "Применить". (ПГЕ https://trello.com/c/TzUSGIH7)
                    if (onClickAdd()) searchProductDelegate.searchCode(code = data, fromScan = true, isBarCode = true)
                } else {
                    screenNavigator.openAlertInvalidBarcodeFormatScannedScreen()
                }
            }
        }
    }

    private fun scannedBoxNotFound(boxNumber: String) {
        launchUITryCatch {
            screenNavigator.showProgressLoadingData(::handleFailure)
            taskManager.getReceivingTask()?.let { task ->
                val params = ZmpUtzGrz31V001Params(
                        taskNumber = task.taskHeader.taskNumber,
                        materialNumber = productInfo.value!!.materialNumber,
                        boxNumber = boxNumber,
                        stampCode = ""
                )
                zmpUtzGrz31V001NetRequest(params).either(::handleFailure, ::handleSuccessZmpUtzGrz31)
            }
            screenNavigator.hideProgress()

        }
    }

    private fun handleSuccessZmpUtzGrz31(result: ZmpUtzGrz31V001Result) {
        when (result.indicatorOnePosition) {
            "1" -> {
                screenNavigator.openScannedBoxListedInCargoUnitDialog(
                        cargoUnitNumber = result.cargoUnitNumber,
                        nextCallbackFunc = {
                            processExciseAlcoBoxAccPGEService.addAllAsSurplusForBox(count = convertEizToBei().toString(), boxNumber = scannedBoxNumber.value ?: "", typeDiscrepancies = "2", isScan = true)
                            processExciseAlcoBoxAccPGEService.setInitialCount(countValue.value ?: 0.0)
                            processExciseAlcoBoxAccPGEService.setCountAcceptRefusal((acceptTotalCount.value ?: 0.0) + (refusalTotalCount.value ?: 0.0))
                            screenNavigator.openExciseAlcoBoxListPGEScreen(
                                    productInfo = productInfo.value!!,
                                    selectQualityCode = qualityInfo.value!![spinQualitySelectedPosition.value!!].code
                            )
                        }
                )
            }
            "2" -> {
                screenNavigator.openScannedBoxNotIncludedInDeliveryDialog(
                        nextCallbackFunc = {
                            processExciseAlcoBoxAccPGEService.addAllAsSurplusForBox(count = convertEizToBei().toString(), boxNumber = scannedBoxNumber.value ?: "", typeDiscrepancies = "2", isScan = true)
                            processExciseAlcoBoxAccPGEService.setInitialCount(countValue.value ?: 0.0)
                            processExciseAlcoBoxAccPGEService.setCountAcceptRefusal((acceptTotalCount.value ?: 0.0) + (refusalTotalCount.value ?: 0.0))
                            screenNavigator.openExciseAlcoBoxListPGEScreen(
                                    productInfo = productInfo.value!!,
                                    selectQualityCode = qualityInfo.value!![spinQualitySelectedPosition.value!!].code
                            )
                        }
                )
            }
            "3" -> {
                screenNavigator.openScannedBoxNotIncludedInNetworkLentaDialog(
                        nextCallbackFunc = { //https://trello.com/c/6NyHp2jB 11. ПГЕ. Излишки. Карточка короба-излишка (не числится в ленте)
                            val boxInfo = processExciseAlcoBoxAccPGEService.searchBox(boxNumber = scannedBoxNumber.value ?: "")
                            processExciseAlcoBoxAccPGEService.setInitialCount(countValue.value ?: 0.0)
                            processExciseAlcoBoxAccPGEService.setCountAcceptRefusal((acceptTotalCount.value ?: 0.0) + (refusalTotalCount.value ?: 0.0))
                            screenNavigator.openExciseAlcoBoxCardPGEScreen(
                                    productInfo = productInfo.value!!,
                                    boxInfo = boxInfo,
                                    massProcessingBoxesNumber = null,
                                    exciseStampInfo = null,
                                    selectQualityCode = qualityInfo.value!![spinQualitySelectedPosition.value!!].code,
                                    isScan = true,
                                    isBoxNotIncludedInNetworkLenta = true
                            )
                        }
                )
            }
        }
    }

    override fun handleFailure(failure: Failure) {
        screenNavigator.openAlertScreen(failure, "97")
    }

    override fun onClickPosition(position: Int) {
        spinQualitySelectedPosition.value = position
    }

    private fun convertEizToBei(): Double {
        var addNewCount = countValue.value?.toDouble() ?: 0.0
        if (isEizUnit.value == true) {
            addNewCount *= productInfo.value?.quantityInvest?.toDouble() ?: 1.0
        }
        return addNewCount
    }

}
