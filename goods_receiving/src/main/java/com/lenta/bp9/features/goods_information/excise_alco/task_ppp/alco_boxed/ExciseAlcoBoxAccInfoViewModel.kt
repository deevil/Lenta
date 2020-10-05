package com.lenta.bp9.features.goods_information.excise_alco.task_ppp.alco_boxed

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.R
import com.lenta.bp9.features.delegates.SearchProductDelegate
import com.lenta.bp9.model.processing.ProcessExciseAlcoBoxAccService
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.platform.TypeDiscrepanciesConstants
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.repos.IDataBaseRepo
import com.lenta.shared.fmp.resources.dao_ext.getProductInfoByMaterial
import com.lenta.shared.fmp.resources.slow.ZfmpUtz48V001
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.combined.scan_info.ScanInfoResult
import com.lenta.shared.requests.combined.scan_info.pojo.QualityInfo
import com.lenta.shared.requests.combined.scan_info.pojo.ReasonRejectionInfo
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toStringFormatted
import com.lenta.shared.utilities.orIfNull
import com.lenta.shared.view.OnPositionClickListener
import com.mobrun.plugin.api.HyperHive
import javax.inject.Inject

class ExciseAlcoBoxAccInfoViewModel : CoreViewModel(), OnPositionClickListener {

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    @Inject
    lateinit var taskManager: IReceivingTaskManager

    @Inject
    lateinit var processExciseAlcoBoxAccService: ProcessExciseAlcoBoxAccService

    @Inject
    lateinit var dataBase: IDataBaseRepo

    @Inject
    lateinit var searchProductDelegate: SearchProductDelegate

    @Inject
    lateinit var context: Context

    @Inject
    lateinit var hyperHive: HyperHive

    private val zfmpUtz48V001: ZfmpUtz48V001 by lazy {
        ZfmpUtz48V001(hyperHive)
    }

    val productInfo: MutableLiveData<TaskProductInfo> = MutableLiveData()
    val tvAccept: MutableLiveData<String> by lazy {
        MutableLiveData(context.getString(R.string.accept, "${productInfo.value?.purchaseOrderUnits?.name}=${productInfo.value?.quantityInvest?.toDouble().toStringFormatted()} ${productInfo.value?.uom?.name}"))
    }
    val spinQuality: MutableLiveData<List<String>> = MutableLiveData()
    val spinQualitySelectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val spinReasonRejection: MutableLiveData<List<String>> = MutableLiveData()
    val spinReasonRejectionSelectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val suffix: MutableLiveData<String> = MutableLiveData()
    val requestFocusToCount: MutableLiveData<Boolean> = MutableLiveData(false)
    val isDefect: MutableLiveData<Boolean> = spinQualitySelectedPosition.map {
        it != 0
    }


    private val qualityInfo: MutableLiveData<List<QualityInfo>> = MutableLiveData()
    private val reasonRejectionInfo: MutableLiveData<List<ReasonRejectionInfo>> = MutableLiveData()

    val count: MutableLiveData<String> = MutableLiveData("0")
    private val countValue: MutableLiveData<Double> = count.map { it?.toDoubleOrNull() ?: 0.0 }

    val acceptTotalCount: MutableLiveData<Double> = countValue.combineLatest(spinQualitySelectedPosition).map {
        val productCountAccept = productInfo
                .value
                ?.let { product ->
                    taskManager
                            .getReceivingTask()
                            ?.taskRepository
                            ?.getProductsDiscrepancies()
                            ?.getCountAcceptOfProduct(product)
                }
                ?: 0.0

        val totalCount = it?.first ?: 0.0
        val spinQualitySelectedPositionValue = it?.second ?: 0

        qualityInfo
                .value
                ?.get(spinQualitySelectedPositionValue)
                ?.code
                ?.takeIf { code ->
                    code == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM
                }
                ?.run {
                    totalCount + productCountAccept
                }
                ?: productCountAccept
    }

    val acceptTotalCountWithUom: MutableLiveData<String> = acceptTotalCount.map {
        val countAccept = taskManager.getReceivingTask()!!.taskRepository.getProductsDiscrepancies().getCountAcceptOfProduct(productInfo.value!!)
        when {
            (it ?: 0.0) > 0.0 -> {
                "+ ${it.toStringFormatted()} ${productInfo.value?.purchaseOrderUnits?.name}"
            }
            else -> { //если было введено отрицательное значение
                "${if (countAccept > 0.0) "+ " + countAccept.toStringFormatted() else countAccept.toStringFormatted()} ${productInfo.value?.purchaseOrderUnits?.name}"
            }
        }
    }

    val refusalTotalCount: MutableLiveData<Double> = countValue.combineLatest(spinQualitySelectedPosition).map {
        val productCountRefusal = productInfo
                .value
                ?.let { product ->
                    taskManager
                            .getReceivingTask()
                            ?.taskRepository
                            ?.getProductsDiscrepancies()
                            ?.getCountRefusalOfProduct(product)
                }
                ?: 0.0

        val totalCount = it?.first ?: 0.0
        val spinQualitySelectedPositionValue = it?.second ?: 0

        qualityInfo
                .value
                ?.get(spinQualitySelectedPositionValue)
                ?.code
                ?.takeIf { code ->
                    code != TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM
                }
                ?.run {
                    totalCount + productCountRefusal
                }
                ?: productCountRefusal
    }

    val refusalTotalCountWithUom: MutableLiveData<String> = refusalTotalCount.map {
        val countRefusal = taskManager.getReceivingTask()!!.taskRepository.getProductsDiscrepancies().getCountRefusalOfProduct(productInfo.value!!)

        if ((it ?: 0.0) > 0.0) {
            "- ${it.toStringFormatted()} ${productInfo.value?.purchaseOrderUnits?.name}"
        } else { //если было введено отрицательное значение
            "${if (countRefusal > 0.0) "- " + countRefusal.toStringFormatted() else countRefusal.toStringFormatted()} ${productInfo.value?.purchaseOrderUnits?.name}"
        }
    }

    val checkStampControlVisibility: MutableLiveData<Boolean> = MutableLiveData()

    val tvStampControlVal: MutableLiveData<String> = acceptTotalCount.combineLatest(spinQualitySelectedPosition).map {
        //https://trello.com/c/Z1SPfmAJ
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
        //https://trello.com/c/Z1SPfmAJ, Проставлять чекбокс при прохождении контроля всех марок во всех коробах
        productInfo.value?.let {
            taskManager.getReceivingTask()?.controlExciseStampsOfProduct(it)
        } ?: false
    }

    val checkBoxControlVisibility: MutableLiveData<Boolean> = MutableLiveData()

    val tvBoxControlVal: MutableLiveData<String> = acceptTotalCount.combineLatest(spinQualitySelectedPosition).map {
        //https://trello.com/c/Z1SPfmAJ
        val acceptTotalCountValue = it?.first ?: 0.0
        val spinQualitySelectedPositionValue = it?.second ?: 0
        val qualityInfoCode = qualityInfo.value?.get(spinQualitySelectedPositionValue)?.code
        val productNumberBoxesControl = productInfo.value?.numberBoxesControl?.toDouble() ?: 0.0
        val productNumberStampsControl = productInfo.value?.numberStampsControl?.toDouble() ?: 0.0

        if (qualityInfoCode == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM) {
            if ((productNumberBoxesControl == 0.0 && productNumberStampsControl == 0.0) || acceptTotalCountValue <= 0.0) {
                checkBoxControlVisibility.value = false
                context.getString(R.string.not_required)
            } else {
                checkBoxControlVisibility.value = true
                val countBoxesPassedControlOfProductValue = productInfo.value?.let { product ->
                    taskManager
                            .getReceivingTask()
                            ?.countBoxesPassedControlOfProduct(product)
                } ?: 0
                if (acceptTotalCountValue < productNumberBoxesControl) {
                    "$countBoxesPassedControlOfProductValue ${context.getString(R.string.of)} ${acceptTotalCountValue.toStringFormatted()}"
                } else {
                    "$countBoxesPassedControlOfProductValue ${context.getString(R.string.of)} ${productNumberBoxesControl.toStringFormatted()}"
                }
            }
        } else "" //это поле отображается только при выбранной категории "Норма"
    }

    val checkBoxControl: MutableLiveData<Boolean> = checkBoxControlVisibility.map {
        //https://trello.com/c/Z1SPfmAJ, 2.4. Устанавливать чекбокс, когда F=Z;
        productInfo.value?.let {
            taskManager
                    .getReceivingTask()
                    ?.controlBoxesOfProduct(it)
        } ?: false
    }

    val checkBoxListVisibility: MutableLiveData<Boolean> = MutableLiveData()

    val tvBoxListVal: MutableLiveData<String> = refusalTotalCount.combineLatest(spinQualitySelectedPosition).map {
        val refusalTotalCountValue = it?.first ?: 0.0
        val spinQualitySelectedPositionValue = it?.second ?: 0
        val qualityInfoCode = qualityInfo.value?.get(spinQualitySelectedPositionValue)?.code
        if (qualityInfoCode != TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM) {
            if (refusalTotalCountValue == 0.0 || refusalTotalCountValue.toInt() == processExciseAlcoBoxAccService.getCountUntreatedBoxes()) {
                checkBoxListVisibility.value = false
                context.getString(R.string.not_required)
            } else {
                checkBoxListVisibility.value = true
                "${processExciseAlcoBoxAccService.getCountDefectBoxes()} ${context.getString(R.string.of)} ${refusalTotalCountValue.toStringFormatted()}"
            }
        } else "" //это поле отображается только при выбранной категории брака
    }

    val checkBoxList: MutableLiveData<Boolean> = checkBoxListVisibility.map {
        //https://trello.com/c/lqyZlYQu, Устанавливать чекбокс, когда F (кол-во в Отказать) = Q (свободные/необработанные короба);
        refusalTotalCount.value == processExciseAlcoBoxAccService.getCountDefectBoxes().toDouble()
    }

    val enabledApplyButton: MutableLiveData<Boolean> = countValue.combineLatest(checkBoxList).map {
        val totalCount = countValue.value ?: 0.0
        val spinQualitySelectedPositionValue = spinQualitySelectedPosition.value ?: 0
        val qualityInfoCode = qualityInfo.value?.get(spinQualitySelectedPositionValue)?.code
        if (qualityInfoCode == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM) {
            totalCount != 0.0
        } else {
            val someMinusValue = processExciseAlcoBoxAccService.getCountUntreatedBoxes() - totalCount.toInt()
            (someMinusValue == 0 || it!!.second) && (totalCount > 0.0)
        }
    }

    private val isShowScreenNoBoxSelectionRequired: MutableLiveData<Boolean> = MutableLiveData(false)

    init {
        launchUITryCatch {
            productInfo.value
                    ?.let {
                        if (processExciseAlcoBoxAccService.newProcessExciseAlcoBoxService(it) == null) {
                            screenNavigator.goBackAndShowAlertWrongProductType()
                            return@launchUITryCatch
                        }
                    }.orIfNull {
                        screenNavigator.goBackAndShowAlertWrongProductType()
                        return@launchUITryCatch
                    }

            searchProductDelegate.init(scanResultHandler = this@ExciseAlcoBoxAccInfoViewModel::handleProductSearchResult)

            suffix.value = productInfo.value?.purchaseOrderUnits?.name
            qualityInfo.value = dataBase.getQualityInfo()
            spinQuality.value = qualityInfo.value?.map { it.name }.orEmpty()
        }
    }

    private fun handleProductSearchResult(@Suppress("UNUSED_PARAMETER") scanInfoResult: ScanInfoResult?): Boolean {
        screenNavigator.goBack()
        return false
    }

    fun onClickBoxes() {
        if ((countValue.value ?: 0.0) <= 0.0) {
            screenNavigator.openAlertMustEnterQuantityScreen()
            return
        }

        processExciseAlcoBoxAccService.setCountAccept((acceptTotalCount.value ?: 0.0))
        screenNavigator.openExciseAlcoBoxListScreen(
                productInfo = productInfo.value!!,
                selectQualityCode = qualityInfo.value!![spinQualitySelectedPosition.value!!].code,
                selectReasonRejectionCode = reasonRejectionInfo.value!![spinReasonRejectionSelectedPosition.value!!].code,
                initialCount = countValue.value.toStringFormatted()
        )
    }

    fun onClickDetails() {
        screenNavigator.openGoodsDetailsScreen(productInfo.value!!)
    }

    fun onClickAdd(): Boolean {
        val totalCount = countValue.value ?: 0.0
        val spinQualitySelectedPositionValue = spinQualitySelectedPosition.value
        val qualityInfoCode = spinQualitySelectedPositionValue?.let {
            qualityInfo.value?.get(it)?.code
        }
        val acceptTotalCountValue = acceptTotalCount.value ?: 0.0
        val spinReasonRejectionSelectedPositionValue = spinReasonRejectionSelectedPosition.value
        val reasonRejectionInfoCode = spinReasonRejectionSelectedPositionValue?.let {
            reasonRejectionInfo.value?.get(it)?.code
        }
        return productInfo.value?.let { product ->
            reasonRejectionInfoCode?.let {typeDiscrepancies ->
                if (processExciseAlcoBoxAccService.overLimit(totalCount)) {
                    screenNavigator.openAlertOverLimit()
                    count.value = "0"
                    false
                } else {
                    if (qualityInfoCode == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM) {
                        processExciseAlcoBoxAccService.addProduct(acceptTotalCountValue.toString(), TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM)
                    } else {
                        if (processExciseAlcoBoxAccService.getCountUntreatedBoxes() - totalCount.toInt() == 0) { //https://trello.com/c/lqyZlYQu, Массовая обработка брака
                            screenNavigator.openAlertNoBoxSelectionRequiredScreen(
                                    materialNumber = product.getMaterialLastSix(),
                                    materialName = product.description,
                                    callbackFunc = {
                                        processExciseAlcoBoxAccService.massProcessingRejectBoxes(typeDiscrepancies)
                                        processExciseAlcoBoxAccService.addProduct(totalCount.toString(), typeDiscrepancies)
                                        screenNavigator.goBack()
                                    }
                            )
                            isShowScreenNoBoxSelectionRequired.value = true
                        } else {
                            processExciseAlcoBoxAccService.addProduct(totalCount.toString(), typeDiscrepancies)
                        }
                    }
                    count.value = "0"
                    spinQualitySelectedPosition.value = 0
                    true
                }
            }
        }
                ?: false
    }

    fun onClickApply() {
        if (onClickAdd() && isShowScreenNoBoxSelectionRequired.value == false) screenNavigator.goBack()
    }

    fun onScanResult(data: String) {
        if ((countValue.value ?: 0.0) <= 0.0) {
            screenNavigator.openAlertMustEnterQuantityScreen()
            return
        }

        when (data.length) {
            68, 150 -> {
                if (isDefect.value == false) {//сканирование марок доступно только при категории Норма https://trello.com/c/Wr4xe6L8
                    val exciseStampInfo = processExciseAlcoBoxAccService.searchExciseStamp(data)
                    if (exciseStampInfo == null) {
                        screenNavigator.openScannedStampNotFoundDialog( //Марка не найдена в поставке. Верните товар поставщику. Отсканированная марка будет помечена как проблемная
                                yesCallbackFunc = {
                                    processExciseAlcoBoxAccService.addExciseStampBad(data)
                                }
                        )
                    } else {
                        if (exciseStampInfo.materialNumber != productInfo.value!!.materialNumber) {
                            //Отсканированная марка принадлежит товару <SAP-код> <Название>"
                            screenNavigator.openAlertScannedStampBelongsAnotherProductScreen(
                                    materialNumber = exciseStampInfo.materialNumber.orEmpty(),
                                    materialName = zfmpUtz48V001.getProductInfoByMaterial(exciseStampInfo.materialNumber)?.name.orEmpty())
                        } else {
                            if (processExciseAlcoBoxAccService.getCountBoxOfProductOfDiscrepancies(exciseStampInfo.boxNumber.orEmpty(), "1") >= acceptTotalCount.value!!.toInt()) {
                                screenNavigator.openAlertRequiredQuantityBoxesAlreadyProcessedScreen() //Необходимое количество коробок уже обработано
                            } else {
                                processExciseAlcoBoxAccService.setCountAccept((acceptTotalCount.value
                                        ?: 0.0))
                                screenNavigator.openExciseAlcoBoxCardScreen(
                                        productInfo = productInfo.value!!,
                                        boxInfo = null,
                                        massProcessingBoxesNumber = null,
                                        exciseStampInfo = exciseStampInfo,
                                        selectQualityCode = qualityInfo.value!![spinQualitySelectedPosition.value!!].code,
                                        selectReasonRejectionCode = null,
                                        initialCount = "1",
                                        isScan = true
                                )
                            }
                        }
                    }
                }
            }
            26 -> {
                val boxInfo = processExciseAlcoBoxAccService.searchBox(boxNumber = data)
                if (boxInfo == null) {
                    screenNavigator.openAlertScannedBoxNotFoundScreen() //Отсканированная коробка не числится в задании. Отдайте коробку поставщику.
                } else {
                    if (boxInfo.materialNumber != productInfo.value!!.materialNumber) {
                        //Отсканированная коробка принадлежит товару <SAP-код> <Название>
                        screenNavigator.openAlertScannedBoxBelongsAnotherProductScreen(materialNumber = boxInfo.materialNumber, materialName = zfmpUtz48V001.getProductInfoByMaterial(boxInfo.materialNumber)?.name
                                ?: "")
                    } else {
                        if (qualityInfo.value?.get(spinQualitySelectedPosition.value
                                        ?: 0)?.code == "1") {
                            if (processExciseAlcoBoxAccService.getCountBoxOfProductOfDiscrepancies(boxInfo.boxNumber, "1") >= acceptTotalCount.value!!.toInt()) {
                                screenNavigator.openAlertRequiredQuantityBoxesAlreadyProcessedScreen() //Необходимое количество коробок уже обработано
                            } else {
                                processExciseAlcoBoxAccService.setCountAccept((acceptTotalCount.value
                                        ?: 0.0))
                                screenNavigator.openExciseAlcoBoxCardScreen(
                                        productInfo = productInfo.value!!,
                                        boxInfo = boxInfo,
                                        massProcessingBoxesNumber = null,
                                        exciseStampInfo = null,
                                        selectQualityCode = qualityInfo.value!![spinQualitySelectedPosition.value!!].code,
                                        selectReasonRejectionCode = null,
                                        initialCount = "1",
                                        isScan = true
                                )
                            }
                        } else {
                            if (checkBoxList.value == true) {
                                screenNavigator.openAlertRequiredQuantityBoxesAlreadyProcessedScreen() //Необходимое количество коробок уже обработано
                            } else {
                                processExciseAlcoBoxAccService.setCountAccept((acceptTotalCount.value
                                        ?: 0.0))
                                screenNavigator.openExciseAlcoBoxCardScreen(
                                        productInfo = productInfo.value!!,
                                        boxInfo = boxInfo,
                                        massProcessingBoxesNumber = null,
                                        exciseStampInfo = null,
                                        selectQualityCode = qualityInfo.value!![spinQualitySelectedPosition.value!!].code,
                                        selectReasonRejectionCode = reasonRejectionInfo.value!![spinReasonRejectionSelectedPosition.value!!].code,
                                        initialCount = countValue.value.toStringFormatted(),
                                        isScan = true
                                )
                            }
                        }
                    }
                }
            }
            else -> {
                if (enabledApplyButton.value == true) { //Функция доступна только при условии, что доступна кнопка "Применить". https://trello.com/c/KbBbXj2t
                    if (onClickAdd()) searchProductDelegate.searchCode(code = data, fromScan = true, isBarCode = true)
                } else {
                    screenNavigator.openAlertInvalidBarcodeFormatScannedScreen()
                }
            }
        }
    }

    override fun onClickPosition(position: Int) {
        spinReasonRejectionSelectedPosition.value = position
    }

    fun onClickPositionSpinQuality(position: Int) {
        launchUITryCatch {
            spinQualitySelectedPosition.value = position
            updateDataSpinReasonRejection(qualityInfo.value!![position].code)
        }
    }

    private suspend fun updateDataSpinReasonRejection(selectedQuality: String) {
        launchUITryCatch {
            screenNavigator.showProgressLoadingData(::handleFailure)
            spinReasonRejectionSelectedPosition.value = 0
            reasonRejectionInfo.value = dataBase.getReasonRejectionInfoOfQuality(selectedQuality)
            spinReasonRejection.value = reasonRejectionInfo.value?.map {
                it.name
            }
            count.value = count.value
            screenNavigator.hideProgress()
        }
    }
}
