package com.lenta.bp9.features.goods_information.excise_alco.task_ppp.alco_boxed.box_list

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.lenta.bp9.R
import com.lenta.bp9.model.processing.ProcessExciseAlcoBoxAccService
import com.lenta.bp9.model.task.*
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.shared.fmp.resources.dao_ext.getProductInfoByMaterial
import com.lenta.shared.fmp.resources.slow.ZfmpUtz48V001
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.Evenable
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toStringFormatted
import com.lenta.shared.utilities.orIfNull
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
    val isScan: MutableLiveData<Boolean> = MutableLiveData(false)

    val visibilityCleanButton: MutableLiveData<Boolean> = selectedPage.map {
        it == 1
    }

    val enabledCleanButton: MutableLiveData<Boolean> = processedSelectionsHelper.selectedPositions.map {
        !it.isNullOrEmpty()
    }

    val enabledHandleGoodsButton: MutableLiveData<Boolean> = notProcessedSelectionsHelper.selectedPositions.map {
        !it.isNullOrEmpty() && selectQualityCode.value != SELECT_QUALITY_CODE
    }

    fun getDescription(): String {
        return if (selectQualityCode.value == SELECT_QUALITY_CODE) {
            normControl()
        } else {
            marriage()
        }
    }

    private fun normControl(): String {
        productInfo.value?.let {
            return if (taskManager.getReceivingTask()?.controlExciseStampsOfProduct(it) == true && taskManager.getReceivingTask()?.controlBoxesOfProduct(it) == true) { //https://trello.com/c/HjxtG4Ca
                context.getString(R.string.norm_control_performed) //Контроль нормы выполнен
            } else {
                "${context.getString(R.string.norm_control)} ${it.numberBoxesControl.toDouble().toStringFormatted()} ${context.getString(R.string.box_abbreviated)}" //Контроль нормы. Z кор.
            }
        }
        return ""
    }

    private fun marriage(): String {
        return if (processExciseAlcoBoxAccService.getCountUntreatedBoxes() == 0) {
            context.getString(R.string.accounting_of_marriage_completed) //Учет брака выполнен
        } else {
            "${context.getString(R.string.accounting_for_marriage)} ${initialCount.value?.toDouble().toStringFormatted()} ${context.getString(R.string.box_abbreviated)}" //Учет брака. Q кор.
        }
    }


    fun onResume() {
        updateData()
    }

    private fun updateData() {
        productInfo.value?.let { productInfoValue ->
            val boxNotProcessed = taskManager.getReceivingTask()?.taskRepository?.getBoxesRepository()?.findBoxesOfProduct(productInfoValue)?.filter { box ->
                taskManager.getReceivingTask()?.taskRepository?.getBoxesDiscrepancies()?.findBoxesDiscrepanciesOfProduct(productInfoValue)?.findLast { it.boxNumber == box.boxNumber }?.boxNumber.isNullOrEmpty()
            }
            val boxProcessed = taskManager.getReceivingTask()?.taskRepository?.getBoxesRepository()?.findBoxesOfProduct(productInfoValue)?.filter { box ->
                !taskManager.getReceivingTask()?.taskRepository?.getBoxesDiscrepancies()?.findBoxesDiscrepanciesOfProduct(productInfoValue)?.findLast { it.boxNumber == box.boxNumber }?.boxNumber.isNullOrEmpty()
            }

            boxNotProcessed?.let { boxInfoList ->
                countNotProcessed.postValue(
                        boxInfoList
                                .mapIndexed { index, boxInfo ->
                                    BoxListItem(
                                            number = index + 1,
                                            name = boxInfo.boxNumber.takeIf { it.length >= BOX_NUMBER_LENGTH }
                                                    ?.run { "${substring(BOX_NUMBER_START, BOX_NUMBER_POSITION_10)} ${substring(BOX_NUMBER_POSITION_10, BOX_NUMBER_POSITION_20)} ${substring(BOX_NUMBER_POSITION_20, BOX_NUMBER_LENGTH)}" }.orEmpty(),
                                            productInfo = productInfoValue,
                                            productDiscrepancies = null,
                                            boxInfo = boxInfo,
                                            typeDiscrepancies = null,
                                            checkBoxControl = false,
                                            checkStampControl = false,
                                            isDefectiveBox = false,
                                            even = index % 2 == 0)
                                }
                                .reversed())
            }
            notProcessedSelectionsHelper.clearPositions()

            boxProcessed?.let { boxInfoList ->
                countProcessed.postValue(
                        boxInfoList
                                .mapIndexed { index, boxInfo ->
                                    val isDefectiveBox = processExciseAlcoBoxAccService.defectiveBox(boxInfo.boxNumber)
                                    val typeDiscrepancies = taskManager.getReceivingTask()?.taskRepository?.getBoxesDiscrepancies()?.findBoxesDiscrepanciesOfProduct(productInfoValue)?.findLast { it.boxNumber == boxInfo.boxNumber }?.typeDiscrepancies.orEmpty()
                                    BoxListItem(
                                            number = index + 1,
                                            name = boxInfo.boxNumber.takeIf { it.length >= BOX_NUMBER_LENGTH }
                                                    ?.run { "${substring(BOX_NUMBER_START, BOX_NUMBER_POSITION_10)} ${substring(BOX_NUMBER_POSITION_10, BOX_NUMBER_POSITION_20)} ${substring(BOX_NUMBER_POSITION_20, BOX_NUMBER_LENGTH)}" }.orEmpty(),
                                            productInfo = productInfo.value,
                                            productDiscrepancies = null,
                                            boxInfo = boxInfo,
                                            typeDiscrepancies = typeDiscrepancies,
                                            checkBoxControl = processExciseAlcoBoxAccService.boxControl(boxInfo),
                                            checkStampControl = processExciseAlcoBoxAccService.stampControlOfBox(boxInfo),
                                            isDefectiveBox = isDefectiveBox,
                                            even = index % 2 == 0)
                                }
                                .reversed())
            }
            processedSelectionsHelper.clearPositions()
        }.orIfNull {
            Logg.e { "productInfo.value  is null" }
        }
    }

    fun onClickSelectAll() {
        isSelectAll.value = true
        updateData()
        notProcessedSelectionsHelper.addAll(countNotProcessed.value ?: emptyList())
        isSelectAll.value = false
    }

    fun onClickClean() {
        processedSelectionsHelper.selectedPositions.value?.map { position ->
            processExciseAlcoBoxAccService.cleanBoxInfo(
                    boxNumber = countProcessed.value?.get(position)?.boxInfo?.boxNumber.orEmpty(),
                    typeDiscrepancies = countProcessed.value?.get(position)?.typeDiscrepancies.orEmpty())
        }

        updateData()
    }

    fun onClickHandleGoods() {
        /** производить сравнение введенного количества в брак на экране карточки товара(X) с выбранным количеством коробов на экране списка коробов на закладке "К обработке"(Y):
        -Если X<Y, то выводить экран с сообщением <Выбрано больше элементов, чем введено>.
        -Если X>=Y, то переходить на экран карточки короба*/
        val countSelectedBoxes = notProcessedSelectionsHelper.selectedPositions.value?.size ?: 0
        if (initialCount.value?.toInt() ?: 0 < countSelectedBoxes) {
            screenNavigator.openAlertMoreBoxesSelectedThanSnteredScreen()
        } else {
            productInfo.value?.let { productInfoValue ->
                countNotProcessed.value?.let { countNotProcessedValue ->
                    screenNavigator.openExciseAlcoBoxCardScreen(
                            productInfo = productInfoValue,
                            boxInfo = if (countSelectedBoxes > 1) null else countNotProcessed.value?.get(notProcessedSelectionsHelper.selectedPositions.value?.last()
                                    ?: 0)?.boxInfo,
                            massProcessingBoxesNumber = if (countSelectedBoxes > 1 && !(countNotProcessed.value.isNullOrEmpty())) notProcessedSelectionsHelper.selectedPositions.value?.map {
                                countNotProcessedValue[it].boxInfo.boxNumber
                            } else null,
                            exciseStampInfo = null,
                            selectQualityCode = selectQualityCode.value.orEmpty(),
                            selectReasonRejectionCode = selectReasonRejectionCode.value,
                            initialCount = initialCount.value.orEmpty(),
                            isScan = false
                    )
                }.orIfNull {
                    Logg.e { "productInfo.value  or countNotProcessed is null" }
                }
            }
        }
    }

    fun onClickApply() {
        screenNavigator.goBack()
    }

    fun onClickItemPosition(position: Int) {
        productInfo.value?.let {
            if (selectedPage.value == 0) {
                screenNavigator.goBack()
                screenNavigator.openExciseAlcoBoxCardScreen(
                        productInfo = it,
                        boxInfo = countNotProcessed.value?.get(position)?.boxInfo,
                        massProcessingBoxesNumber = null,
                        exciseStampInfo = null,
                        selectQualityCode = selectQualityCode.value.orEmpty(),
                        selectReasonRejectionCode = selectReasonRejectionCode.value,
                        initialCount = initialCount.value.orEmpty(),
                        isScan = false
                )
            } else {
                screenNavigator.goBack()
                screenNavigator.openExciseAlcoBoxCardScreen(
                        productInfo = it,
                        boxInfo = countProcessed.value?.get(position)?.boxInfo,
                        massProcessingBoxesNumber = null,
                        exciseStampInfo = null,
                        selectQualityCode = selectQualityCode.value.orEmpty(),
                        selectReasonRejectionCode = selectReasonRejectionCode.value,
                        initialCount = initialCount.value.orEmpty(),
                        isScan = false
                )
            }.orIfNull {
                Logg.e { "productInfo.value is null" }
            }
        }
    }

    override fun onOkInSoftKeyboard(): Boolean {
        scanCode.value?.let {
            isScan.value = false
            onScanResult(it)
        }
        return true
    }

    fun onScanResult(data: String) {
        when (data.length) {
            68, 150 -> checkScannedExciseStamp(data)
            26 -> checkScannedBox(data)
            else -> screenNavigator.openAlertInvalidBarcodeFormatScannedScreen()
        }
    }

    private fun checkScannedExciseStamp(data: String) {
        val exciseStampInfo = processExciseAlcoBoxAccService.searchExciseStamp(data)
        if (exciseStampInfo == null) {
            screenNavigator.openAlertScannedStampNotFoundScreen() //Отсканированная марка не числится в текущей поставке. Перейдите к коробу, в которой находится эта марка и отсканируйте ее снова.
        } else {
            isStampBoxSAP(exciseStampInfo)
        }
    }

    private fun isStampBoxSAP(exciseStampInfo: TaskExciseStampInfo) {
        if (exciseStampInfo.materialNumber != productInfo.value?.materialNumber) {
            //Отсканированная марка принадлежит товару <SAP-код> <Название>"
            screenNavigator.openAlertScannedStampBelongsAnotherProductScreen(exciseStampInfo.materialNumber.orEmpty(), zfmpUtz48V001.getProductInfoByMaterial(exciseStampInfo.materialNumber)?.name.orEmpty())
        } else {
            checkAllBoxesProcessed(exciseStampInfo)
        }
    }

    private fun checkAllBoxesProcessed(exciseStampInfo: TaskExciseStampInfo) {
        if (processExciseAlcoBoxAccService.getCountBoxOfProductOfDiscrepancies(exciseStampInfo.boxNumber.orEmpty(), TYPE_DISCREPANCIES) >= processExciseAlcoBoxAccService.getCountAccept()) {
            screenNavigator.openAlertRequiredQuantityBoxesAlreadyProcessedScreen() //Необходимое количество коробок уже обработано
        } else {
            productInfo.value?.let { productInfoValue ->
                isScan.value?.let { isScanValue ->
                    screenNavigator.openExciseAlcoBoxCardScreen(
                            productInfo = productInfoValue,
                            boxInfo = null,
                            massProcessingBoxesNumber = null,
                            exciseStampInfo = exciseStampInfo,
                            selectQualityCode = SELECT_QUALITY_CODE,
                            selectReasonRejectionCode = null,
                            initialCount = INITIAL_COUNT,
                            isScan = isScanValue
                    )
                            .orIfNull {
                                Logg.e { "productInfo.value  or isScanInfo  is null" }
                            }
                }
            }
        }

    }

    private fun checkScannedBox(data: String) {
        val boxInfo = processExciseAlcoBoxAccService.searchBox(boxNumber = data)
        if (boxInfo == null) {
            screenNavigator.openAlertScannedBoxNotFoundScreen() //Отсканированная коробка не числится в задании. Отдайте коробку поставщику.
        } else {
            checkBoxSAP(boxInfo)
        }
    }

    private fun checkBoxSAP(boxInfo: TaskBoxInfo) {
        if (boxInfo.materialNumber != productInfo.value!!.materialNumber) {
            //Отсканированная коробка принадлежит товару <SAP-код> <Название>
            screenNavigator.openAlertScannedBoxBelongsAnotherProductScreen(materialNumber = boxInfo.materialNumber, materialName = zfmpUtz48V001.getProductInfoByMaterial(boxInfo.materialNumber)?.name.orEmpty())
        } else {
            processBoxInfo(boxInfo)
        }
    }


    private fun processBoxInfo(boxInfo: TaskBoxInfo?) {
        if (selectQualityCode.value == SELECT_QUALITY_CODE) {
            oneQualityCode(boxInfo)
        } else {
            twoQualityCode(boxInfo)
        }
    }

    private fun oneQualityCode(boxInfo: TaskBoxInfo?) {
        if (processExciseAlcoBoxAccService.getCountBoxOfProductOfDiscrepancies(boxInfo?.boxNumber.orEmpty(), TYPE_DISCREPANCIES) >= processExciseAlcoBoxAccService.getCountAccept()) {
            screenNavigator.openAlertRequiredQuantityBoxesAlreadyProcessedScreen() //Необходимое количество коробок уже обработано
        } else {
            productInfo.value?.let { productInfoValue ->
                isScan.value?.let { isScanValue ->
                    screenNavigator.openExciseAlcoBoxCardScreen(
                            productInfo = productInfoValue,
                            boxInfo = boxInfo,
                            massProcessingBoxesNumber = null,
                            exciseStampInfo = null,
                            selectQualityCode = selectQualityCode.value.orEmpty(),
                            selectReasonRejectionCode = null,
                            initialCount = INITIAL_COUNT,
                            isScan = isScanValue
                    )
                }.orIfNull {
                    Logg.e { "productInfo.value  is null" }
                }
            }
        }
    }

    private fun twoQualityCode(boxInfo: TaskBoxInfo?) {
        if (processExciseAlcoBoxAccService.getCountUntreatedBoxes() == 0) { //см. ExciseAlcoBoxAccInfoViewModel сканирование коробок
            screenNavigator.openAlertRequiredQuantityBoxesAlreadyProcessedScreen() //Необходимое количество коробок уже обработано
        } else {
            boxesProgress(boxInfo)
        }
    }

    private fun boxesProgress(boxInfo: TaskBoxInfo?) {
        productInfo.value?.let { productInfoValue ->
            isScan.value?.let { isScanValue ->
                screenNavigator.openExciseAlcoBoxCardScreen(
                        productInfo = productInfoValue,
                        boxInfo = boxInfo,
                        massProcessingBoxesNumber = null,
                        exciseStampInfo = null,
                        selectQualityCode = selectQualityCode.value.orEmpty(),
                        selectReasonRejectionCode = selectReasonRejectionCode.value,
                        initialCount = initialCount.value.orEmpty(),
                        isScan = isScanValue
                )
            }.orIfNull {
                Logg.e { "productInfo.value  is null" }
            }
        }
        if (selectQualityCode.value == SELECT_QUALITY_CODE) {
            boxesQualityIsNorm(boxInfo)
        } else {
            boxesIsNotNormQuality(boxInfo)
        }
    }

    private fun boxesQualityIsNorm(boxInfo: TaskBoxInfo?) {
        if (processExciseAlcoBoxAccService.getCountBoxOfProductOfDiscrepancies(boxInfo?.boxNumber.orEmpty(), TYPE_DISCREPANCIES) >= processExciseAlcoBoxAccService.getCountAccept()) {
            screenNavigator.openAlertRequiredQuantityBoxesAlreadyProcessedScreen() //Необходимое количество коробок уже обработано
        } else {

            productInfo.value?.let { productInfoValue ->
                isScan.value?.let { isScanValue ->
                    screenNavigator.openExciseAlcoBoxCardScreen(
                            productInfo = productInfoValue,
                            boxInfo = boxInfo,
                            massProcessingBoxesNumber = null,
                            exciseStampInfo = null,
                            selectQualityCode = selectQualityCode.value.orEmpty(),
                            selectReasonRejectionCode = null,
                            initialCount = INITIAL_COUNT,
                            isScan = isScanValue
                    )
                }.orIfNull {
                    Logg.e { "productInfo.value  is null" }
                }
            }
        }
    }

    private fun boxesIsNotNormQuality(boxInfo: TaskBoxInfo?) {
        if (processExciseAlcoBoxAccService.getCountUntreatedBoxes() == 0) { //см. ExciseAlcoBoxAccInfoViewModel сканирование коробок
            screenNavigator.openAlertRequiredQuantityBoxesAlreadyProcessedScreen() //Необходимое количество коробок уже обработано
        } else {
            productInfo.value?.let { productInfoValue ->
                isScan.value?.let { isScanValue ->
                    screenNavigator.openExciseAlcoBoxCardScreen(
                            productInfo = productInfoValue,
                            boxInfo = boxInfo,
                            massProcessingBoxesNumber = null,
                            exciseStampInfo = null,
                            selectQualityCode = selectQualityCode.value.orEmpty(),
                            selectReasonRejectionCode = selectReasonRejectionCode.value,
                            initialCount = initialCount.value.orEmpty(),
                            isScan = isScanValue
                    )
                }.orIfNull {
                    Logg.e { "productInfo.value  is null" }
                }
            }
        }
    }


    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    fun onDigitPressed(digit: Int) {
        requestFocusToScanCode.value = true
        scanCode.value = scanCode.value.orEmpty() + digit
    }

    companion object {
        private const val SELECT_QUALITY_CODE = "1"
        private const val INITIAL_COUNT = "1"
        private const val TYPE_DISCREPANCIES = "1"
        private const val BOX_NUMBER_START = 0
        private const val BOX_NUMBER_LENGTH = 26
        private const val BOX_NUMBER_POSITION_10 = 10
        private const val BOX_NUMBER_POSITION_20 = 20
    }

}

data class BoxListItem(
        val number: Int,
        val name: String,
        val productInfo: TaskProductInfo?,
        val productDiscrepancies: TaskProductDiscrepancies?,
        val boxInfo: TaskBoxInfo,
        val typeDiscrepancies: String?,
        val checkBoxControl: Boolean,
        val checkStampControl: Boolean,
        val isDefectiveBox: Boolean,
        val even: Boolean
) : Evenable {
    override fun isEven() = even
}
