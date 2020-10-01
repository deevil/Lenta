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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
        actionWithNotProcessedBoxes()
        actionWithProcessedBoxes()
    }

    private fun actionWithNotProcessedBoxes() = launchUITryCatch {
        productInfo.value?.let { productInfoValue ->
            taskManager.getReceivingTask()?.taskRepository?.let { taskRepository ->
                val boxNotProcessed = withContext(Dispatchers.IO) {
                    taskRepository.getBoxesRepository().findBoxesOfProduct(productInfoValue)?.filter { box ->
                        taskRepository.getBoxesDiscrepancies()
                                .findBoxesDiscrepanciesOfProduct(productInfoValue)
                                .findLast { it.boxNumber == box.boxNumber }?.boxNumber.isNullOrEmpty()
                    }.orEmpty()
                }

                val mappedBoxNotProcessedInfo = withContext(Dispatchers.IO) {
                    boxNotProcessed
                            .mapIndexed { index, boxInfo ->
                                BoxListItem(
                                        number = index + 1,
                                        name = boxInfo.boxNumber.getNameFromBoxNumber(),
                                        productInfo = productInfoValue,
                                        productDiscrepancies = null,
                                        boxInfo = boxInfo,
                                        typeDiscrepancies = null,
                                        checkBoxControl = false,
                                        checkStampControl = false,
                                        isDefectiveBox = false,
                                        even = index % 2 == 0)
                            }
                            .reversed()
                }
                countNotProcessed.value = mappedBoxNotProcessedInfo
                notProcessedSelectionsHelper.clearPositions()
            }.orIfNull {
                Logg.e { "taskManager.getReceivingTask()?.taskRepository is null" }
            }

        }.orIfNull {
            Logg.e { "productInfo.value or countNotProcessedValue is null" }
        }
    }

    private fun actionWithProcessedBoxes() = launchUITryCatch {
        productInfo.value?.let { productInfoValue ->
            taskManager.getReceivingTask()?.taskRepository?.let { taskRepository ->
                val boxProcessed = withContext(Dispatchers.IO) {
                    taskRepository.getBoxesRepository().findBoxesOfProduct(productInfoValue)?.filter { box ->
                        !taskRepository.getBoxesDiscrepancies()
                                .findBoxesDiscrepanciesOfProduct(productInfoValue)
                                .findLast { it.boxNumber == box.boxNumber }
                                ?.boxNumber.isNullOrEmpty()
                    }.orEmpty()
                }

                val mappedProcessedBoxInfo = withContext(Dispatchers.IO) {
                    boxProcessed.mapIndexed { index, boxInfo ->
                        val isDefectiveBox = processExciseAlcoBoxAccPGEService.defectiveBox(boxInfo.boxNumber)
                        val typeDiscrepancies = getTypeDiscrepanciesFromTaskManager(boxInfo.boxNumber)
                        BoxListItem(
                                number = index + 1,
                                name = boxInfo.boxNumber.getNameFromBoxNumber(),
                                productInfo = productInfoValue,
                                productDiscrepancies = null,
                                boxInfo = boxInfo,
                                typeDiscrepancies = typeDiscrepancies,
                                checkBoxControl = processExciseAlcoBoxAccPGEService.boxControl(boxInfo),
                                checkStampControl = processExciseAlcoBoxAccPGEService.stampControlOfBox(boxInfo),
                                isDefectiveBox = isDefectiveBox,
                                even = index % 2 == 0)
                    }.reversed()
                }
                countProcessed.value = mappedProcessedBoxInfo
                processedSelectionsHelper.clearPositions()
            }.orIfNull {
                Logg.e { "taskManager.getReceivingTask()?.taskRepository is null" }
            }
        }.orIfNull {
            Logg.e { "productInfo.value or countProcessedValue is null" }
        }
    }

    private fun String.getNameFromBoxNumber(): String {
        return takeIf { it.length >= BOX_NUMBER_LENGTH }
                ?.run { "${substring(BOX_NUMBER_START, BOX_NUMBER_POSITION_10)} ${substring(BOX_NUMBER_POSITION_10, BOX_NUMBER_POSITION_20)} ${substring(BOX_NUMBER_POSITION_20, BOX_NUMBER_LENGTH)}" }.orEmpty()
    }

    private fun getTypeDiscrepanciesFromTaskManager(boxNumber: String): String {
        return productInfo.value?.let { productInfoValue ->
            taskManager.getReceivingTask()
                    ?.taskRepository?.getBoxesDiscrepancies()
                    ?.findBoxesDiscrepanciesOfProduct(productInfoValue)
                    ?.findLast { it.boxNumber == boxNumber }
                    ?.typeDiscrepancies.orEmpty()
        }.orEmpty()
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
        val initialCount = processExciseAlcoBoxAccPGEService.getInitialCount()
        if (initialCount < countSelectedBoxes) {
            screenNavigator.openAlertMoreBoxesSelectedThanSnteredScreen()
        } else {
            actionWithExciseAlcoBoxCardPGE(countSelectedBoxes)
        }
    }

    private fun actionWithExciseAlcoBoxCardPGE(countSelectedBoxes: Int) {
        productInfo.value?.let { productInfoValue ->
            countNotProcessed.value?.let { countNotProcessedValue ->
                val notProcessedLastSelectedPosition = notProcessedSelectionsHelper.selectedPositions.value?.last()
                        ?: 0
                val boxInfoValue = countNotProcessedValue.takeIf { countSelectedBoxes <= MIN_COUNT_OF_BOXES }?.get(notProcessedLastSelectedPosition)?.boxInfo
                val massProcessingBoxesNumbers = notProcessedSelectionsHelper.selectedPositions.value?.takeIf { countSelectedBoxes > MIN_COUNT_OF_BOXES }?.mapNotNull {
                    countNotProcessedValue.getOrNull(it)?.boxInfo?.boxNumber
                }

                screenNavigator.openExciseAlcoBoxCardPGEScreen(
                        productInfo = productInfoValue,
                        boxInfo = boxInfoValue,
                        massProcessingBoxesNumber = massProcessingBoxesNumbers,
                        exciseStampInfo = null,
                        selectQualityCode = selectQualityCode.value.orEmpty(),
                        isScan = false,
                        isBoxNotIncludedInNetworkLenta = false
                )
            }.orIfNull {
                Logg.e { "productInfo.value or countNotProcessedValue is null" }
            }
        }.orIfNull {
            Logg.e { "productInfo.value  is null" }
        }
    }

    fun onClickApply() {
        //https://trello.com/c/3WNRaO2C По кнопкам "Назад" и "Применить" переходить на карточку товара.
        screenNavigator.goBack()
    }

    fun onClickItemPosition(position: Int) {
        if (selectedPage.value == 0) {
            processingBoxes(position)
        } else {
            productInfo.value?.let { productInfoValue ->
                screenNavigator.goBack()
                selectQualityCode.value?.let { selectQualityCodeValue ->
                    screenNavigator.openExciseAlcoBoxCardPGEScreen(
                            productInfo = productInfoValue,
                            boxInfo = countProcessed.value?.getOrNull(position)?.boxInfo,
                            massProcessingBoxesNumber = null,
                            exciseStampInfo = null,
                            selectQualityCode = selectQualityCodeValue,
                            isScan = false,
                            isBoxNotIncludedInNetworkLenta = false
                    )
                }.orIfNull {
                    Logg.e { "selectQualityCode.value  is null" }
                }
            }.orIfNull {
                Logg.e { "productInfo.value  is null" }
            }
        }
    }

    private fun processingBoxes(position: Int) {
        productInfo.value?.let {
            val boxNumber = countNotProcessed.value?.getOrNull(position)?.boxInfo?.boxNumber.orEmpty()

            if (isCountOfBoxMoreOrEqualCountOfAccepted(boxNumber)) {
                screenNavigator.openAlertRequiredQuantityBoxesAlreadyProcessedScreen() // Необходимое количество коробок уже обработано
            } else {
                screenNavigator.goBack()
                screenNavigator.openExciseAlcoBoxCardPGEScreen(
                        productInfo = it,
                        boxInfo = countNotProcessed.value?.getOrNull(position)?.boxInfo,
                        massProcessingBoxesNumber = null,
                        exciseStampInfo = null,
                        selectQualityCode = selectQualityCode.value.orEmpty(),
                        isScan = false,
                        isBoxNotIncludedInNetworkLenta = false
                )
            }
        }.orIfNull {
            Logg.e { "productInfo.value  is null" }
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
            DATA_LENGTH_68, DATA_LENGTH_150 -> processDataLengthFor68And150(data)
            DATA_LENGTH_26 -> processDataLengthFor26(data)
            else -> screenNavigator.openAlertInvalidBarcodeFormatScannedScreen()
        }
    }

    private fun processDataLengthFor68And150(data: String) {
        if (isCountOfBoxMoreOrEqualCountOfAccepted(data)) { //это условие добавлено здесь, т.к. на WM оно тоже есть
            screenNavigator.openAlertRequiredQuantityBoxesAlreadyProcessedScreen() //Необходимое количество коробок уже обработано
        } else {
            val exciseStampInfo = processExciseAlcoBoxAccPGEService.searchExciseStamp(data)
            checksStampInTask(exciseStampInfo)
        }
    }

    private fun processDataLengthFor26(data: String) {
        if (isCountOfBoxMoreOrEqualCountOfAccepted(data)) {
            screenNavigator.openAlertRequiredQuantityBoxesAlreadyProcessedScreen() //Необходимое количество коробок уже обработано
        } else {
            val boxInfo = processExciseAlcoBoxAccPGEService.searchBox(boxNumber = data)
            checkBoxInTask(boxInfo, data)
        }
    }

    private fun isCountOfBoxMoreOrEqualCountOfAccepted(boxNumber: String): Boolean {
        val countOfBoxProduct = processExciseAlcoBoxAccPGEService.getCountBoxOfProductOfDiscrepancies(boxNumber)
        val countOfAcceptedRefusal = processExciseAlcoBoxAccPGEService.getCountAcceptRefusal()
        return countOfBoxProduct >= countOfAcceptedRefusal
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
            }
        }.orIfNull {
            Logg.e { "productInfo.value  is null" }
        }
    }

    private fun checkBoxSap(boxInfo: TaskBoxInfo) {
        productInfo.value?.let {
            if (boxInfo.materialNumber != it.materialNumber) {
                val materialNumber = zfmpUtz48V001.getProductInfoByMaterial(boxInfo.materialNumber)?.name.orEmpty()
                //Отсканированная коробка принадлежит товару <SAP-код> <Название>
                screenNavigator.openAlertScannedBoxBelongsAnotherProductScreen(
                        materialNumber = boxInfo.materialNumber,
                        materialName = materialNumber
                )
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
            }
        }.orIfNull {
            Logg.e { "productInfo.value  is null" }
        }
    }

    private fun scannedBoxNotFound(boxNumber: String) {
        launchUITryCatch {
            productInfo.value?.let { productInfoValue ->
                screenNavigator.showProgressLoadingData(::handleFailure)
                taskManager.getReceivingTask()?.let { task ->
                    val params = ZmpUtzGrz31V001Params(
                            taskNumber = task.taskHeader.taskNumber,
                            materialNumber = productInfoValue.materialNumber,
                            boxNumber = boxNumber,
                            stampCode = ""
                    )

                    zmpUtzGrz31V001NetRequest(params).also {
                        screenNavigator.hideProgress()
                    }.either(::handleFailure, ::handleSuccessZmpUtzGrz31)

                }.orIfNull {
                    Logg.e { "taskManager.getReceivingTask() is null" }
                }
            }.orIfNull {
                Logg.e { "productInfo.value  is null" }
            }
        }
    }

    private fun handleSuccessZmpUtzGrz31(result: ZmpUtzGrz31V001Result) {
        productInfo.value?.let { productInfoValue ->
            when (result.indicatorOnePosition) {
                INDICATOR_POSITION_1 -> onOpenScannedBoxListedInCargoUnit(result.cargoUnitNumber, productInfoValue)
                INDICATOR_POSITION_2 -> onOpenScannedBoxNotIncludedInDelivery(productInfoValue)
                INDICATOR_POSITION_3 -> onOpenScannedBoxNotIncludedInNetwork(productInfoValue)
            }
        }.orIfNull {
            Logg.e { "productInfo.value  is null" }
        }
    }

    private fun onOpenScannedBoxListedInCargoUnit(cargoUnitNumber: String, productInfoValue: TaskProductInfo) {
        screenNavigator.openScannedBoxListedInCargoUnitDialog(
                cargoUnitNumber = cargoUnitNumber,
                nextCallbackFunc = {
                    processExciseAlcoBoxAccPGEService.addAllAsSurplusForBox(
                            count = convertEizToBei().toString(),
                            boxNumber = scannedBoxNumber.value.orEmpty(),
                            typeDiscrepancies = TYPE_DISCREPANCIES,
                            isScan = true
                    )
                    screenNavigator.openExciseAlcoBoxListPGEScreen(
                            productInfo = productInfoValue,
                            selectQualityCode = selectQualityCode.value.orEmpty()
                    )
                }
        )
    }

    private fun onOpenScannedBoxNotIncludedInDelivery(productInfoValue: TaskProductInfo) {
        screenNavigator.openScannedBoxNotIncludedInDeliveryDialog(
                nextCallbackFunc = {
                    processExciseAlcoBoxAccPGEService.addAllAsSurplusForBox(
                            count = convertEizToBei().toString(),
                            boxNumber = scannedBoxNumber.value.orEmpty(),
                            typeDiscrepancies = TYPE_DISCREPANCIES,
                            isScan = true
                    )
                    screenNavigator.openExciseAlcoBoxListPGEScreen(
                            productInfo = productInfoValue,
                            selectQualityCode = selectQualityCode.value.orEmpty()
                    )
                }
        )
    }

    private fun onOpenScannedBoxNotIncludedInNetwork(productInfoValue: TaskProductInfo) {
        screenNavigator.openScannedBoxNotIncludedInNetworkLentaDialog(
                nextCallbackFunc = { //https://trello.com/c/6NyHp2jB 11. ПГЕ. Излишки. Карточка короба-излишка (не числится в ленте)
                    val boxInfo = processExciseAlcoBoxAccPGEService.searchBox(
                            boxNumber = scannedBoxNumber.value.orEmpty()
                    )
                    screenNavigator.openExciseAlcoBoxCardPGEScreen(
                            productInfo = productInfoValue,
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

    override fun handleFailure(failure: Failure) {
        screenNavigator.openAlertScreen(failure, PAGE_NUMBER_97)
    }

    private fun convertEizToBei(): Double {
        var addNewCount = processExciseAlcoBoxAccPGEService.getInitialCount()
        if (productInfo.value?.purchaseOrderUnits?.code != productInfo.value?.uom?.code) { //так как у нас может с карточки товара прийти и в ЕИЗ и в БЕИ, то здесь делаем эту проверку (см. isEizUnit на карточке товара ExciseAlcoBoxAccInfoPGEViewModel, на карточке товара мы не можем изменить единицу измерения, она там формируется по условию, которое здесь и прописали)
            addNewCount *= productInfo.value?.quantityInvest?.toDouble() ?: DEFAULT_ADDED_COUNT
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
        private const val MIN_COUNT_OF_BOXES = 1
        private const val DEFAULT_ADDED_COUNT = 1.0
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
