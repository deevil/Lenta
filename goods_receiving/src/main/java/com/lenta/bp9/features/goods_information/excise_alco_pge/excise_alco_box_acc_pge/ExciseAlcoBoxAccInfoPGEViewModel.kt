package com.lenta.bp9.features.goods_information.excise_alco_pge.excise_alco_box_acc_pge

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.R
import com.lenta.bp9.features.goods_list.SearchProductDelegate
import com.lenta.bp9.model.processing.ProcessExciseAlcoBoxAccPGEService
import com.lenta.bp9.model.processing.ProcessExciseAlcoBoxAccService
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.repos.IDataBaseRepo
import com.lenta.shared.fmp.resources.dao_ext.getProductInfoByMaterial
import com.lenta.shared.fmp.resources.slow.ZfmpUtz48V001
import com.lenta.shared.models.core.Uom
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.combined.scan_info.ScanInfoResult
import com.lenta.shared.requests.combined.scan_info.pojo.QualityInfo
import com.lenta.shared.requests.combined.scan_info.pojo.ReasonRejectionInfo
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toStringFormatted
import com.lenta.shared.view.OnPositionClickListener
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.launch
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
    val spinProcessingUnits: MutableLiveData<List<String>> = MutableLiveData()
    val spinProcessingUnitsSelectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val suffix: MutableLiveData<String> = MutableLiveData()
    val isDefect: MutableLiveData<Boolean> = spinQualitySelectedPosition.map {
        it != 0
    }

    private val qualityInfo: MutableLiveData<List<QualityInfo>> = MutableLiveData()
    private val reasonRejectionInfo: MutableLiveData<List<ReasonRejectionInfo>> = MutableLiveData()

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
        val countRefusal = taskManager.getReceivingTask()!!.taskRepository.getProductsDiscrepancies().getCountAcceptOfProductPGE(productInfo.value!!)
        if (qualityInfo.value?.get(it!!.second)?.code == "3" || qualityInfo.value?.get(it!!.second)?.code == "4" || qualityInfo.value?.get(it!!.second)?.code == "5") {
            convertEizToBei() + countRefusal
        } else {
            countRefusal
        }
    }

    val refusalTotalCountWithUom: MutableLiveData<String> = refusalTotalCount.map {
        val countRefusal = taskManager.getReceivingTask()!!.taskRepository.getProductsDiscrepancies().getCountAcceptOfProductPGE(productInfo.value!!)

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

    val visibilityAddButton: MutableLiveData<Boolean> = qualityInfo.map {
        qualityInfo.value?.get(0)?.code != "1"
    }

    init {
        viewModelScope.launch {
            searchProductDelegate.init(viewModelScope = this@ExciseAlcoBoxAccInfoPGEViewModel::viewModelScope,
                    scanResultHandler = this@ExciseAlcoBoxAccInfoPGEViewModel::handleProductSearchResult)

            suffix.value = productInfo.value?.purchaseOrderUnits?.name
            qualityInfo.value = dataBase.getQualityInfoNormPGE()
            spinQuality.value = qualityInfo.value?.map {
                it.name
            }
            if (processExciseAlcoBoxAccPGEService.newProcessExciseAlcoBoxPGEService(productInfo.value!!) == null) {
                screenNavigator.goBack()
                screenNavigator.openAlertWrongProductType()
            }
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

        screenNavigator.openExciseAlcoBoxListPGEScreen(
                productInfo = productInfo.value!!,
                selectQualityCode = qualityInfo.value!![spinQualitySelectedPosition.value!!].code,
                processingUnit = reasonRejectionInfo.value!![spinProcessingUnitsSelectedPosition.value!!].code,
                initialCount = countValue.value.toStringFormatted()
        )
    }

    fun onClickDetails() {
        screenNavigator.openGoodsDetailsScreen(productInfo.value!!)
    }

    fun onClickAdd(): Boolean {
        if (processExciseAlcoBoxAccPGEService.overLimit(countValue.value!!)) {
            screenNavigator.openAlertOverLimitAlcoPGEScreen(
                    nextCallbackFunc = {
                        screenNavigator.openExciseAlcoBoxListPGEScreen(
                                productInfo = productInfo.value!!,
                                selectQualityCode = qualityInfo.value!![spinQualitySelectedPosition.value!!].code,
                                processingUnit = reasonRejectionInfo.value!![spinProcessingUnitsSelectedPosition.value!!].code,
                                initialCount = countValue.value.toStringFormatted()
                        )
                    }
            )
            return false
        } else {
            processExciseAlcoBoxAccPGEService.addProduct(convertEizToBei().toString(), qualityInfo.value!![spinQualitySelectedPosition.value!!].code)
            return true
        }
    }

    fun onClickApply() {
        if (onClickAdd()) screenNavigator.goBack()
    }

    fun onScanResult(data: String) {
        if ((countValue.value ?: 0.0) <= 0.0) {
            screenNavigator.openAlertMustEnterQuantityScreen()
            return
        }

        when (data.length) {
            68, 150 -> {
                if (isDefect.value == false) {//todo это от ППП сканирование марок доступно только при категории Норма https://trello.com/c/Wr4xe6L8
                    val exciseStampInfo = processExciseAlcoBoxAccPGEService.searchExciseStamp(data)
                    if (exciseStampInfo == null) {
                        screenNavigator.openAlertScannedStampNotFoundScreen() //Отсканированная марка не числится в текущей поставке. Перейдите к коробу, в которой находится эта марка и отсканируйте ее снова.
                    } else {
                        if (exciseStampInfo.materialNumber != productInfo.value!!.materialNumber) {
                            //Отсканированная марка принадлежит товару <SAP-код> <Название>"
                            screenNavigator.openAlertScannedStampBelongsAnotherProductScreen(exciseStampInfo.materialNumber, zfmpUtz48V001.getProductInfoByMaterial(exciseStampInfo.materialNumber)?.name
                                    ?: "")
                        } else {
                            if (processExciseAlcoBoxAccPGEService.getCountBoxOfProductOfDiscrepancies(exciseStampInfo.boxNumber, "1") >= acceptTotalCount.value!!.toInt()) {
                                screenNavigator.openAlertRequiredQuantityBoxesAlreadyProcessedScreen() //Необходимое количество коробок уже обработано
                            } else {
                                screenNavigator.openExciseAlcoBoxCardPGEScreen(
                                        productInfo = productInfo.value!!,
                                        boxInfo = null,
                                        massProcessingBoxesNumber = null,
                                        exciseStampInfo = exciseStampInfo,
                                        selectQualityCode = qualityInfo.value!![spinQualitySelectedPosition.value!!].code,
                                        processingUnit = null,
                                        initialCount = "1",
                                        isScan = true
                                )
                            }
                        }
                    }
                }
            }
            26 -> {
                val boxInfo = processExciseAlcoBoxAccPGEService.searchBox(boxNumber = data)
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
                            if (processExciseAlcoBoxAccPGEService.getCountBoxOfProductOfDiscrepancies(boxInfo.boxNumber, "1") >= acceptTotalCount.value!!.toInt()) {
                                screenNavigator.openAlertRequiredQuantityBoxesAlreadyProcessedScreen() //Необходимое количество коробок уже обработано
                            } else {
                                screenNavigator.openExciseAlcoBoxCardPGEScreen(
                                        productInfo = productInfo.value!!,
                                        boxInfo = boxInfo,
                                        massProcessingBoxesNumber = null,
                                        exciseStampInfo = null,
                                        selectQualityCode = qualityInfo.value!![spinQualitySelectedPosition.value!!].code,
                                        processingUnit = null,
                                        initialCount = "1",
                                        isScan = true
                                )
                            }
                        } else {
                            screenNavigator.openExciseAlcoBoxCardPGEScreen(
                                    productInfo = productInfo.value!!,
                                    boxInfo = boxInfo,
                                    massProcessingBoxesNumber = null,
                                    exciseStampInfo = null,
                                    selectQualityCode = qualityInfo.value!![spinQualitySelectedPosition.value!!].code,
                                    processingUnit = reasonRejectionInfo.value!![spinProcessingUnitsSelectedPosition.value!!].code,
                                    initialCount = countValue.value.toStringFormatted(),
                                    isScan = true
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

    override fun onClickPosition(position: Int) {
        spinProcessingUnitsSelectedPosition.value = position
    }

    fun onClickPositionSpinQuality(position: Int) {
        viewModelScope.launch {
            spinQualitySelectedPosition.value = position
            updateDataSpinReasonRejection(qualityInfo.value!![position].code)
        }
    }

    private suspend fun updateDataSpinReasonRejection(selectedQuality: String) {
        viewModelScope.launch {
            screenNavigator.showProgressLoadingData()
            spinProcessingUnitsSelectedPosition.value = 0
            reasonRejectionInfo.value = dataBase.getReasonRejectionInfoOfQuality(selectedQuality)
            spinProcessingUnits.value = reasonRejectionInfo.value?.map {
                it.name
            }
            count.value = count.value
            screenNavigator.hideProgress()
        }
    }

    private fun convertEizToBei(): Double {
        var addNewCount = countValue.value!!.toDouble()
        if (isEizUnit.value!!) {
            addNewCount *= productInfo.value?.quantityInvest?.toDouble() ?: 1.0
        }
        return addNewCount
    }
}
