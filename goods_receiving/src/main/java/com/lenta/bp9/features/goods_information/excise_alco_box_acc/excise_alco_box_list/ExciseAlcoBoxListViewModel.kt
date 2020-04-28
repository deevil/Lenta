package com.lenta.bp9.features.goods_information.excise_alco_box_acc.excise_alco_box_list

import android.content.Context
import com.lenta.shared.platform.viewmodel.CoreViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.R
import com.lenta.bp9.model.processing.ProcessExciseAlcoBoxAccService
import com.lenta.bp9.model.task.*
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.shared.fmp.resources.dao_ext.getProductInfoByMaterial
import com.lenta.shared.fmp.resources.slow.ZfmpUtz48V001
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.Evenable
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toStringFormatted
import com.mobrun.plugin.api.HyperHive
import javax.inject.Inject

class ExciseAlcoBoxListViewModel : CoreViewModel(), PageSelectionListener, OnOkInSoftKeyboardListener {

    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var taskManager: IReceivingTaskManager
    @Inject
    lateinit var processExciseAlcoBoxAccService: ProcessExciseAlcoBoxAccService
    @Inject
    lateinit var context: Context
    @Inject
    lateinit var hyperHive: HyperHive

    private val zfmpUtz48V001: ZfmpUtz48V001 by lazy {
        ZfmpUtz48V001(hyperHive)
    }

    val selectedPage = MutableLiveData(0)
    val productInfo: MutableLiveData<TaskProductInfo> = MutableLiveData()
    val selectQualityCode: MutableLiveData<String> = MutableLiveData()
    val selectReasonRejectionCode: MutableLiveData<String> = MutableLiveData()
    val initialCount: MutableLiveData<String> = MutableLiveData()
    val countNotProcessed: MutableLiveData<List<BoxListItem>> = MutableLiveData()
    val countProcessed: MutableLiveData<List<BoxListItem>> = MutableLiveData()
    val notProcessedSelectionsHelper = SelectionItemsHelper()
    val processedSelectionsHelper = SelectionItemsHelper()
    val scanCode: MutableLiveData<String> = MutableLiveData()
    val requestFocusToScanCode: MutableLiveData<Boolean> = MutableLiveData()
    val isSelectAll: MutableLiveData<Boolean> = MutableLiveData(false)

    val visibilityCleanButton: MutableLiveData<Boolean> = selectedPage.map {
        it == 1
    }

    val enabledCleanButton: MutableLiveData<Boolean> = processedSelectionsHelper.selectedPositions.map {
        !it.isNullOrEmpty()
    }

    val enabledHandleGoodsButton: MutableLiveData<Boolean> = notProcessedSelectionsHelper.selectedPositions.map {
        !it.isNullOrEmpty() && selectQualityCode.value != "1"
    }

    fun getDescription() : String {
        return if (selectQualityCode.value == "1") {
            if (taskManager.getReceivingTask()!!.taskRepository.getBoxesDiscrepancies().findBoxesDiscrepanciesOfProduct(productInfo.value!!).filter {
                        it.isScan
                    }.size >= (productInfo.value?.numberBoxesControl?.toInt() ?: 0)) {
                context.getString(R.string.norm_control_performed) //Контроль нормы выполнен
            } else {
                "${context.getString(R.string.norm_control)} ${productInfo.value?.numberBoxesControl?.toDouble().toStringFormatted()} ${context.getString(R.string.box_abbreviated)}" //Контроль нормы. Z кор.
            }
        } else {
            if (processExciseAlcoBoxAccService.getCountUntreatedBoxes() == 0) {
                context.getString(R.string.accounting_of_marriage_completed) //Учет брака выполнен
            } else {
                "${context.getString(R.string.accounting_for_marriage)} ${initialCount.value?.toDouble().toStringFormatted()} ${context.getString(R.string.box_abbreviated)}" //Учет брака. Q кор.
            }
        }
    }

    fun onResume() {
        updateData()
    }

    private fun updateData() {
        val boxNotProcessed = taskManager.getReceivingTask()?.taskRepository?.getBoxes()?.findBoxesOfProduct(productInfo.value!!)?.filter {box ->
            taskManager.getReceivingTask()?.taskRepository?.getBoxesDiscrepancies()?.findBoxesDiscrepanciesOfProduct(productInfo.value!!)?.findLast { it.boxNumber == box.boxNumber }?.boxNumber.isNullOrEmpty()
        }
        val boxProcessed = taskManager.getReceivingTask()?.taskRepository?.getBoxes()?.findBoxesOfProduct(productInfo.value!!)?.filter {box ->
            !taskManager.getReceivingTask()?.taskRepository?.getBoxesDiscrepancies()?.findBoxesDiscrepanciesOfProduct(productInfo.value!!)?.findLast { it.boxNumber == box.boxNumber }?.boxNumber.isNullOrEmpty()
        }

        boxNotProcessed?.let {boxInfoList ->
            countNotProcessed.postValue(
                    boxInfoList
                            .mapIndexed { index, boxInfo ->
                                BoxListItem(
                                        number = index + 1,
                                        name = "${boxInfo.boxNumber.substring(0,10)} ${boxInfo.boxNumber.substring(10,20)} ${boxInfo.boxNumber.substring(20,26)}",
                                        productInfo = productInfo.value,
                                        productDiscrepancies = null,
                                        boxInfo = boxInfo,
                                        checkBoxControl = false,
                                        checkStampControl = false,
                                        isDefectiveBox = false,
                                        even = index % 2 == 0)
                            }
                            .reversed())
        }
        notProcessedSelectionsHelper.clearPositions()

        boxProcessed?.let {boxInfoList ->
            countProcessed.postValue(
                    boxInfoList
                            .mapIndexed { index, boxInfo ->
                                val checkBoxControl = taskManager.getReceivingTask()!!.taskRepository.getBoxesDiscrepancies().findBoxesDiscrepanciesOfProduct(productInfo.value!!).filter {
                                    it.isScan
                                }.size >= (productInfo.value!!.numberBoxesControl.toInt())
                                val checkStampControl = taskManager.getReceivingTask()!!.taskRepository.getExciseStampsDiscrepancies().findExciseStampsDiscrepanciesOfProduct(productInfo.value!!).filter {
                                    it.isScan
                                }.size >= (productInfo.value!!.numberStampsControl.toInt())
                                val isDefectiveBox = processExciseAlcoBoxAccService.defectiveBox(boxInfo.boxNumber)
                                BoxListItem(
                                        number = index + 1,
                                        name = "${boxInfo.boxNumber.substring(0,10)} ${boxInfo.boxNumber.substring(10,20)} ${boxInfo.boxNumber.substring(20,26)}",
                                        productInfo = productInfo.value,
                                        productDiscrepancies = null,
                                        boxInfo = boxInfo,
                                        checkBoxControl = checkBoxControl,
                                        checkStampControl = checkStampControl,
                                        isDefectiveBox = isDefectiveBox,
                                        even = index % 2 == 0)
                            }
                            .reversed())
        }
        processedSelectionsHelper.clearPositions()
    }

    fun onClickSelectAll() {
        isSelectAll.value = true
        updateData()
        notProcessedSelectionsHelper.addAll(countNotProcessed.value ?: emptyList())
        isSelectAll.value = false
    }

    fun onClickClean(){
        processedSelectionsHelper.selectedPositions.value?.map { position ->
            taskManager.getReceivingTask()?.
                    taskRepository?.
                    getExciseStampsDiscrepancies()?.
                    deleteExciseStampDiscrepancyOfProductOfBoxOfDiscrepancy(
                            materialNumber = productInfo.value!!.materialNumber,
                            boxNumber = countProcessed.value?.get(position)?.boxInfo?.boxNumber ?: "",
                            typeDiscrepancies = if (selectReasonRejectionCode.value == null) selectQualityCode.value ?: "" else selectReasonRejectionCode.value ?: ""
                    )

            taskManager.getReceivingTask()?.
                    taskRepository?.
                    getBoxesDiscrepancies()?.
                    deleteBoxDiscrepancies(
                            materialNumber = productInfo.value!!.materialNumber,
                            boxNumber = countProcessed.value?.get(position)?.boxInfo?.boxNumber ?: "",
                            typeDiscrepancies = if (selectReasonRejectionCode.value == null) selectQualityCode.value!! else selectReasonRejectionCode.value ?: ""
                    )
        }

        updateData()
    }

    fun onClickHandleGoods(){
        /** производить сравнение введенного количества в брак на экране карточки товара(X) с выбранным количеством коробов на экране списка коробов на закладке "К обработке"(Y):
        -Если X<Y, то выводить экран с сообщением <Выбрано больше элементов, чем введено>.
        -Если X>=Y, то переходить на экран карточки короба*/
        val countSelectedBoxes = notProcessedSelectionsHelper.selectedPositions.value?.size ?: 0
        if ( initialCount.value!!.toInt() < countSelectedBoxes ) {
            screenNavigator.openAlertMoreBoxesSelectedThanSnteredScreen()
        } else {
            screenNavigator.openExciseAlcoBoxCardScreen(
                    productInfo = productInfo.value!!,
                    boxInfo = if (countSelectedBoxes > 1) null else countNotProcessed.value?.get(notProcessedSelectionsHelper.selectedPositions.value?.last() ?: 0)?.boxInfo,
                    massProcessingBoxesNumber = if (countSelectedBoxes > 1) notProcessedSelectionsHelper.selectedPositions.value?.map {
                        countNotProcessed.value!![it].boxInfo.boxNumber
                    } else null,
                    exciseStampInfo = null,
                    selectQualityCode = selectQualityCode.value!!,
                    selectReasonRejectionCode = selectReasonRejectionCode.value,
                    initialCount = initialCount.value!!
            )
        }
    }

    fun onClickApply() {
        screenNavigator.goBack()
    }

    fun onClickItemPosition(position: Int) {
        if (selectedPage.value == 0) {
            screenNavigator.goBack()
            screenNavigator.openExciseAlcoBoxCardScreen(
                    productInfo = productInfo.value!!,
                    boxInfo = countNotProcessed.value?.get(position)?.boxInfo,
                    massProcessingBoxesNumber = null,
                    exciseStampInfo = null,
                    selectQualityCode = selectQualityCode.value!!,
                    selectReasonRejectionCode = selectReasonRejectionCode.value,
                    initialCount = initialCount.value!!
            )
        }
    }

    override fun onOkInSoftKeyboard(): Boolean {
        scanCode.value?.let {
            onScanResult(it)
        }
        return true
    }

    fun onScanResult(data: String) {
        when (data.length) {
            68, 150 -> {
                val exciseStampInfo = processExciseAlcoBoxAccService.searchExciseStamp(data)
                if (exciseStampInfo == null) {
                    screenNavigator.openAlertScannedStampNotFoundScreen() //Отсканированная марка не числится в текущей поставке. Перейдите к коробу, в которой находится эта марка и отсканируйте ее снова.
                } else {
                    if (exciseStampInfo.materialNumber != productInfo.value!!.materialNumber) {
                        //Отсканированная марка принадлежит товару <SAP-код> <Название>"
                        screenNavigator.openAlertScannedStampBelongsAnotherProductScreen(exciseStampInfo.materialNumber, zfmpUtz48V001.getProductInfoByMaterial(exciseStampInfo.materialNumber)?.name ?: "")
                    } else {
                        if (processExciseAlcoBoxAccService.getCountBoxOfProductOfDiscrepancies(exciseStampInfo.boxNumber, "1") >= taskManager.getReceivingTask()!!.taskRepository.getProductsDiscrepancies().getCountAcceptOfProduct(productInfo.value!!)) {
                            screenNavigator.openAlertRequiredQuantityBoxesAlreadyProcessedScreen() //Необходимое количество коробок уже обработано
                        } else {
                            screenNavigator.openExciseAlcoBoxCardScreen(
                                    productInfo = productInfo.value!!,
                                    boxInfo = null,
                                    massProcessingBoxesNumber = null,
                                    exciseStampInfo = exciseStampInfo,
                                    selectQualityCode = "1",
                                    selectReasonRejectionCode = null,
                                    initialCount = "1"
                            )
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
                        screenNavigator.openAlertScannedBoxBelongsAnotherProductScreen(materialNumber = boxInfo.materialNumber, materialName = zfmpUtz48V001.getProductInfoByMaterial(boxInfo.materialNumber)?.name ?: "")
                    } else {
                        if (selectQualityCode.value == "1") {
                            if (processExciseAlcoBoxAccService.getCountBoxOfProductOfDiscrepancies(boxInfo.boxNumber, "1") >= taskManager.getReceivingTask()!!.taskRepository.getProductsDiscrepancies().getCountAcceptOfProduct(productInfo.value!!)) {
                                screenNavigator.openAlertRequiredQuantityBoxesAlreadyProcessedScreen() //Необходимое количество коробок уже обработано
                            } else {
                                screenNavigator.openExciseAlcoBoxCardScreen(
                                        productInfo = productInfo.value!!,
                                        boxInfo = boxInfo,
                                        massProcessingBoxesNumber = null,
                                        exciseStampInfo = null,
                                        selectQualityCode = selectQualityCode.value!!,
                                        selectReasonRejectionCode = null,
                                        initialCount = "1"
                                )
                            }
                        } else {
                            if (processExciseAlcoBoxAccService.getCountUntreatedBoxes() == 0) { //см. ExciseAlcoBoxAccInfoViewModel сканирование коробок
                                screenNavigator.openAlertRequiredQuantityBoxesAlreadyProcessedScreen() //Необходимое количество коробок уже обработано
                            } else {
                                screenNavigator.openExciseAlcoBoxCardScreen(
                                        productInfo = productInfo.value!!,
                                        boxInfo = boxInfo,
                                        massProcessingBoxesNumber = null,
                                        exciseStampInfo = null,
                                        selectQualityCode = selectQualityCode.value!!,
                                        selectReasonRejectionCode = selectReasonRejectionCode.value,
                                        initialCount = initialCount.value!!
                                )
                            }
                        }
                    }
                }
            }
            else -> screenNavigator.openAlertInvalidBarcodeFormatScannedScreen()
        }
    }

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    fun onDigitPressed(digit: Int) {
        requestFocusToScanCode.value = true
        scanCode.value = scanCode.value ?: "" + digit
    }

}

data class BoxListItem(
        val number: Int,
        val name: String,
        val productInfo: TaskProductInfo?,
        val productDiscrepancies: TaskProductDiscrepancies?,
        val boxInfo: TaskBoxInfo,
        val checkBoxControl: Boolean,
        val checkStampControl: Boolean,
        val isDefectiveBox: Boolean,
        val even: Boolean
) : Evenable {
    override fun isEven() = even
}
