package com.lenta.bp9.features.goods_information.excise_alco.task_pge.alco_boxed.box_list

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.lenta.bp9.R
import com.lenta.bp9.features.goods_information.excise_alco.task_ppp.alco_boxed.box_list.BoxListItem
import com.lenta.bp9.model.processing.ProcessExciseAlcoBoxAccPGEService
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskBoxInfo
import com.lenta.bp9.model.task.TaskExciseStampInfo
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.requests.network.ZmpUtzGrz31V001NetRequest
import com.lenta.bp9.requests.network.ZmpUtzGrz31V001Params
import com.lenta.bp9.requests.network.ZmpUtzGrz31V001Result
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.resources.dao_ext.getProductInfoByMaterial
import com.lenta.shared.fmp.resources.slow.ZfmpUtz48V001
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toStringFormatted
import com.lenta.shared.utilities.orIfNull
import com.mobrun.plugin.api.HyperHive
import javax.inject.Inject

class ExciseAlcoBoxListPGEViewModel : CoreViewModel(), PageSelectionListener, OnOkInSoftKeyboardListener {

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    @Inject
    lateinit var taskManager: IReceivingTaskManager

    @Inject
    lateinit var processExciseAlcoBoxAccPGEService: ProcessExciseAlcoBoxAccPGEService

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
    val selectQualityCode: MutableLiveData<String> = MutableLiveData()
    val countNotProcessed: MutableLiveData<List<BoxListItem>> = MutableLiveData()
    val countProcessed: MutableLiveData<List<BoxListItem>> = MutableLiveData()
    val notProcessedSelectionsHelper = SelectionItemsHelper()
    val processedSelectionsHelper = SelectionItemsHelper()
    val scanCode: MutableLiveData<String> = MutableLiveData()
    val requestFocusToScanCode: MutableLiveData<Boolean> = MutableLiveData()
    val isSelectAll: MutableLiveData<Boolean> = MutableLiveData(false)
    val isScan: MutableLiveData<Boolean> = MutableLiveData(false)

    private val scannedBoxNumber: MutableLiveData<String> = MutableLiveData("")

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
        val countBoxesOfProductForSearchSurplus = processExciseAlcoBoxAccPGEService.getCountBoxesOfProductForSearchSurplus(processExciseAlcoBoxAccPGEService.getInitialCount())
        val descriptionSurplus = if (countBoxesOfProductForSearchSurplus > 0) "${context.getString(R.string.surplus_dot)} ${countBoxesOfProductForSearchSurplus.toStringFormatted()} ${context.getString(R.string.box_abbreviated)}" else ""
        return if (selectQualityCode.value == SELECT_QUALITY_CODE) {
            normAndSurplus(descriptionSurplus)
        } else {
            marriage()
        }
    }

    private fun normAndSurplus(descriptionSurplus: String): String {
        productInfo.value?.let {
            return if (taskManager.getReceivingTask()?.controlExciseStampsOfProduct(it) == true && taskManager.getReceivingTask()?.controlBoxesOfProduct(it) == true) { //https://trello.com/c/HjxtG4Ca
                "${context.getString(R.string.norm_control_performed)} $descriptionSurplus" //Контроль нормы выполнен. Излишек. Y кор.
            } else {
                "${context.getString(R.string.norm_control)} ${productInfo.value?.numberBoxesControl?.toDouble().toStringFormatted()} ${context.getString(R.string.box_abbreviated)} $descriptionSurplus" //Контроль нормы. Z кор. Излишек. Y кор.
            }
        }
        return ""
    }

    private fun marriage(): String {
        return if (processExciseAlcoBoxAccPGEService.getCountUntreatedBoxes() == 0) {
            context.getString(R.string.accounting_of_marriage_completed) //Учет брака выполнен
        } else {
            "${context.getString(R.string.accounting_for_marriage)} ${processExciseAlcoBoxAccPGEService.getInitialCount().toStringFormatted()} ${context.getString(R.string.box_abbreviated)}" //Учет брака. Q кор.
        }
    }

    fun onResume() {
        updateData()
    }

    private fun updateData() {
        productInfo.value?.let { productInfoValue ->
            val boxNotProcessed = taskManager.getReceivingTask()?.taskRepository?.getBoxes()?.findBoxesOfProduct(productInfoValue)?.filter { box ->
                taskManager.getReceivingTask()?.taskRepository?.getBoxesDiscrepancies()?.findBoxesDiscrepanciesOfProduct(productInfoValue)?.findLast { it.boxNumber == box.boxNumber }?.boxNumber.isNullOrEmpty()
            }
            val boxProcessed = taskManager.getReceivingTask()?.taskRepository?.getBoxes()?.findBoxesOfProduct(productInfoValue)?.filter { box ->
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
                                    val isDefectiveBox = processExciseAlcoBoxAccPGEService.defectiveBox(boxInfo.boxNumber)
                                    val typeDiscrepancies = taskManager.getReceivingTask()?.taskRepository?.getBoxesDiscrepancies()?.findBoxesDiscrepanciesOfProduct(productInfo.value!!)?.findLast { it.boxNumber == boxInfo.boxNumber }?.typeDiscrepancies
                                            ?: ""
                                    BoxListItem(
                                            number = index + 1,
                                            name = boxInfo.boxNumber.takeIf { it.length >= BOX_NUMBER_LENGTH }
                                                    ?.run { "${substring(BOX_NUMBER_START, BOX_NUMBER_POSITION_10)} ${substring(BOX_NUMBER_POSITION_10, BOX_NUMBER_POSITION_20)} ${substring(BOX_NUMBER_POSITION_20, BOX_NUMBER_LENGTH)}" }.orEmpty(),
                                            productInfo = productInfoValue,
                                            productDiscrepancies = null,
                                            boxInfo = boxInfo,
                                            typeDiscrepancies = typeDiscrepancies,
                                            checkBoxControl = processExciseAlcoBoxAccPGEService.boxControl(boxInfo),
                                            checkStampControl = processExciseAlcoBoxAccPGEService.stampControlOfBox(boxInfo),
                                            isDefectiveBox = isDefectiveBox,
                                            even = index % 2 == 0)
                                }
                                .reversed())
            }
            processedSelectionsHelper.clearPositions()
        }.orIfNull {
            Logg.e { "productInfo.value or countNotProcessedValue is null" }
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
            processExciseAlcoBoxAccPGEService.cleanBoxInfo(
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
        if (processExciseAlcoBoxAccPGEService.getInitialCount() < countSelectedBoxes) {
            screenNavigator.openAlertMoreBoxesSelectedThanSnteredScreen()
        } else {
            productInfo.value?.let { productInfoValue ->
                countNotProcessed.value?.let { countNotProcessedValue ->
                    screenNavigator.openExciseAlcoBoxCardPGEScreen(
                            productInfo = productInfoValue,
                            boxInfo = if (countSelectedBoxes > 1) null else countNotProcessed.value?.get(notProcessedSelectionsHelper.selectedPositions.value?.last()
                                    ?: 0)?.boxInfo,
                            massProcessingBoxesNumber = if (countSelectedBoxes > 1) notProcessedSelectionsHelper.selectedPositions.value?.map {
                                countNotProcessedValue[it].boxInfo.boxNumber
                            } else null,
                            exciseStampInfo = null,
                            selectQualityCode = selectQualityCode.value.orEmpty(),
                            isScan = false,
                            isBoxNotIncludedInNetworkLenta = false
                    )
                }.orIfNull {
                    Logg.e { "productInfo.value or countNotProcessedValue is null" }
                }
            }
        }
    }

    fun onClickApply() {
        //https://trello.com/c/3WNRaO2C По кнопкам "Назад" и "Применить" переходить на карточку товара.
        screenNavigator.goBack()
    }

    fun onClickItemPosition(position: Int) {
        if (selectedPage.value == 0) {
            procesingBoxes(position)
        } else {
            productInfo.value?.let {
                screenNavigator.goBack()
                if (productInfo.value != null && selectQualityCode.value != null) {
                    screenNavigator.openExciseAlcoBoxCardPGEScreen(
                            productInfo = it,
                            boxInfo = countProcessed.value?.get(position)?.boxInfo,
                            massProcessingBoxesNumber = null,
                            exciseStampInfo = null,
                            selectQualityCode = selectQualityCode.value.orEmpty(),
                            isScan = false,
                            isBoxNotIncludedInNetworkLenta = false
                    )
                }
            }
        }.orIfNull {
            Logg.e { "productInfo.value  is null" }
        }
    }

    private fun procesingBoxes(position: Int) {
        productInfo.value?.let {
            if (processExciseAlcoBoxAccPGEService.getCountBoxOfProductOfDiscrepancies(countNotProcessed.value?.get(position)?.boxInfo?.boxNumber
                            ?: "") >= processExciseAlcoBoxAccPGEService.getCountAcceptRefusal()) {
                screenNavigator.openAlertRequiredQuantityBoxesAlreadyProcessedScreen() //Необходимое количество коробок уже обработано
            } else {
                screenNavigator.goBack()
                screenNavigator.openExciseAlcoBoxCardPGEScreen(
                        productInfo = it,
                        boxInfo = countNotProcessed.value?.get(position)?.boxInfo,
                        massProcessingBoxesNumber = null,
                        exciseStampInfo = null,
                        selectQualityCode = selectQualityCode.value.orEmpty(),
                        isScan = false,
                        isBoxNotIncludedInNetworkLenta = false
                )
            }.orIfNull {
                Logg.e { "productInfo.value  is null" }
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
        productInfo.value?.let {
            when (data.length) {
                DATA_LENGTH_68, DATA_LENGTH_150 -> {
                    if (processExciseAlcoBoxAccPGEService.getCountBoxOfProductOfDiscrepancies(data) >= processExciseAlcoBoxAccPGEService.getCountAcceptRefusal()) { //это условие добавлено здесь, т.к. на WM оно тоже есть
                        screenNavigator.openAlertRequiredQuantityBoxesAlreadyProcessedScreen() //Необходимое количество коробок уже обработано
                    } else {
                        val exciseStampInfo = processExciseAlcoBoxAccPGEService.searchExciseStamp(data)
                        checksStampInTask(exciseStampInfo)
                    }
                }
                DATA_LENGTH_26 -> {
                    if (processExciseAlcoBoxAccPGEService.getCountBoxOfProductOfDiscrepancies(data) >= processExciseAlcoBoxAccPGEService.getCountAcceptRefusal()) {
                        screenNavigator.openAlertRequiredQuantityBoxesAlreadyProcessedScreen() //Необходимое количество коробок уже обработано
                    } else {
                        val boxInfo = processExciseAlcoBoxAccPGEService.searchBox(boxNumber = data)
                        checkBoxInTask(boxInfo, data)
                    }
                }
                else -> screenNavigator.openAlertInvalidBarcodeFormatScannedScreen()
            }
        }.orIfNull {
            Logg.e { "productInfo.value  is null" }
        }
    }

    private fun checksStampInTask(exciseStampInfo: TaskExciseStampInfo?) {
        if (exciseStampInfo == null) {
            screenNavigator.openAlertScannedStampNotFoundTaskPGEScreen() //Отсканированная марка отсутвует в задании. Отсканируйте номер коробки, а затем номер марки для заявления излишка.
        } else {
            checkStampSap(exciseStampInfo)
        }
    }

    private fun checkBoxInTask(boxInfo: TaskBoxInfo?, data: String) {
        if (boxInfo == null) {
            scannedBoxNumber.value = data
            scannedBoxNotFound(data)
        } else {
            checkBoxSap(boxInfo)
        }
    }

    private fun checkStampSap(exciseStampInfo: TaskExciseStampInfo) {
        productInfo.value?.let {
            if (exciseStampInfo.materialNumber != it.materialNumber) {
                //Отсканированная марка принадлежит товару <SAP-код> <Название>"
                screenNavigator.openAlertScannedStampBelongsAnotherProductScreen(
                        materialNumber = exciseStampInfo.materialNumber.orEmpty(),
                        materialName = zfmpUtz48V001.getProductInfoByMaterial(exciseStampInfo.materialNumber)?.name.orEmpty()
                )
            } else {
                screenNavigator.openExciseAlcoBoxCardPGEScreen(
                        productInfo = it,
                        boxInfo = null,
                        massProcessingBoxesNumber = null,
                        exciseStampInfo = exciseStampInfo,
                        selectQualityCode = selectQualityCode.value.orEmpty(),
                        isScan = true,
                        isBoxNotIncludedInNetworkLenta = false
                )
            }.orIfNull {
                Logg.e { "productInfo.value  is null" }
            }
        }
    }

    private fun checkBoxSap(boxInfo: TaskBoxInfo) {
        productInfo.value?.let {
            if (boxInfo.materialNumber != it.materialNumber) {
                //Отсканированная коробка принадлежит товару <SAP-код> <Название>
                screenNavigator.openAlertScannedBoxBelongsAnotherProductScreen(materialNumber = boxInfo.materialNumber, materialName = zfmpUtz48V001.getProductInfoByMaterial(boxInfo.materialNumber)?.name
                        ?: "")
            } else {
                screenNavigator.openExciseAlcoBoxCardPGEScreen(
                        productInfo = it,
                        boxInfo = boxInfo,
                        massProcessingBoxesNumber = null,
                        exciseStampInfo = null,
                        selectQualityCode = selectQualityCode.value.orEmpty(),
                        isScan = true,
                        isBoxNotIncludedInNetworkLenta = false
                )
            }.orIfNull {
                Logg.e { "productInfo.value  is null" }
            }
        }
    }

    private fun scannedBoxNotFound(boxNumber: String) {
        launchUITryCatch {
            productInfo.value?.let {
                screenNavigator.showProgressLoadingData(::handleFailure)
                taskManager.getReceivingTask()?.let { task ->
                    val params = ZmpUtzGrz31V001Params(
                            taskNumber = task.taskHeader.taskNumber,
                            materialNumber = it.materialNumber,
                            boxNumber = boxNumber,
                            stampCode = ""
                    )
                    zmpUtzGrz31V001NetRequest(params).either(::handleFailure, ::handleSuccessZmpUtzGrz31)
                }
                screenNavigator.hideProgress()
            }
        }.orIfNull {
            Logg.e { "productInfo.value  is null" }
        }
    }

    private fun handleSuccessZmpUtzGrz31(result: ZmpUtzGrz31V001Result) {
        productInfo.value?.let {
            when (result.indicatorOnePosition) {
                INDICATOR_POSITION_1 -> {
                    screenNavigator.openScannedBoxListedInCargoUnitDialog(
                            cargoUnitNumber = result.cargoUnitNumber,
                            nextCallbackFunc = {
                                processExciseAlcoBoxAccPGEService.addAllAsSurplusForBox(count = convertEizToBei().toString(), boxNumber = scannedBoxNumber.value.orEmpty()
                                        , typeDiscrepancies = TYPE_DISCREPANCIES, isScan = true)
                                screenNavigator.openExciseAlcoBoxListPGEScreen(
                                        productInfo = it,
                                        selectQualityCode = selectQualityCode.value.orEmpty()
                                )
                            }
                    )
                }
                INDICATOR_POSITION_2 -> {
                    screenNavigator.openScannedBoxNotIncludedInDeliveryDialog(
                            nextCallbackFunc = {
                                processExciseAlcoBoxAccPGEService.addAllAsSurplusForBox(count = convertEizToBei().toString(), boxNumber = scannedBoxNumber.value.orEmpty()
                                        , typeDiscrepancies = TYPE_DISCREPANCIES, isScan = true)
                                screenNavigator.openExciseAlcoBoxListPGEScreen(
                                        productInfo = it,
                                        selectQualityCode = selectQualityCode.value.orEmpty()
                                )
                            }
                    )
                }
                INDICATOR_POSITION_3 -> {
                    screenNavigator.openScannedBoxNotIncludedInNetworkLentaDialog(
                            nextCallbackFunc = { //https://trello.com/c/6NyHp2jB 11. ПГЕ. Излишки. Карточка короба-излишка (не числится в ленте)
                                val boxInfo = processExciseAlcoBoxAccPGEService.searchBox(boxNumber = scannedBoxNumber.value.orEmpty())
                                screenNavigator.openExciseAlcoBoxCardPGEScreen(
                                        productInfo = it,
                                        boxInfo = boxInfo,
                                        massProcessingBoxesNumber = null,
                                        exciseStampInfo = null,
                                        selectQualityCode = selectQualityCode.value.orEmpty(),
                                        isScan = true,
                                        isBoxNotIncludedInNetworkLenta = true
                                )
                            }
                    )
                }
            }
        }.orIfNull {
            Logg.e { "productInfo.value  is null" }
        }
    }

    override fun handleFailure(failure: Failure) {
        screenNavigator.openAlertScreen(failure, PAGE_NUMBER_97)
    }

    private fun convertEizToBei(): Double {
        var addNewCount = processExciseAlcoBoxAccPGEService.getInitialCount()
        if (productInfo.value?.purchaseOrderUnits?.code != productInfo.value?.uom?.code) { //так как у нас может с карточки товара прийти и в ЕИЗ и в БЕИ, то здесь делаем эту проверку (см. isEizUnit на карточке товара ExciseAlcoBoxAccInfoPGEViewModel, на карточке товара мы не можем изменить единицу измерения, она там формируется по условию, которое здесь и прописали)
            addNewCount *= productInfo.value?.quantityInvest?.toDouble() ?: 1.0
        }
        return addNewCount
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
        private const val TYPE_DISCREPANCIES = "2"
        private const val PAGE_NUMBER_97 = "97"
        private const val INDICATOR_POSITION_1 = "1"
        private const val INDICATOR_POSITION_2 = "2"
        private const val INDICATOR_POSITION_3 = "3"
        private const val BOX_NUMBER_START = 0
        private const val BOX_NUMBER_LENGTH = 26
        private const val BOX_NUMBER_POSITION_10 = 10
        private const val BOX_NUMBER_POSITION_20 = 20
        private const val DATA_LENGTH_68 = 68
        private const val DATA_LENGTH_150 = 150
        private const val DATA_LENGTH_26 = 26
    }
}
