package com.lenta.bp9.features.goods_information.excise_alco_pge.excise_alco_box_acc_pge.excise_alco_box_list

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.R
import com.lenta.bp9.features.goods_information.excise_alco_receiving.excise_alco_box_acc.excise_alco_box_list.BoxListItem
import com.lenta.bp9.model.processing.ProcessExciseAlcoBoxAccPGEService
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.requests.network.ZmpUtzGrz31V001NetRequest
import com.lenta.bp9.requests.network.ZmpUtzGrz31V001Params
import com.lenta.bp9.requests.network.ZmpUtzGrz31V001Result
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.resources.dao_ext.getProductInfoByMaterial
import com.lenta.shared.fmp.resources.slow.ZfmpUtz48V001
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toStringFormatted
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.launch
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

    val selectedPage = MutableLiveData(0)
    val productInfo: MutableLiveData<TaskProductInfo> = MutableLiveData()
    val selectQualityCode: MutableLiveData<String> = MutableLiveData()
    val initialCount: MutableLiveData<String> = MutableLiveData()
    val countAcceptRefusal: MutableLiveData<Double> = MutableLiveData()
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
        !it.isNullOrEmpty() && selectQualityCode.value != "1"
    }

    fun getDescription() : String {
        return if (selectQualityCode.value == "1") {
            if (taskManager.getReceivingTask()?.controlExciseStampsOfProduct(productInfo.value!!) == true && taskManager.getReceivingTask()?.controlBoxesOfProduct(productInfo.value!!) == true) { //https://trello.com/c/HjxtG4Ca
                context.getString(R.string.norm_control_performed) //Контроль нормы выполнен
            } else {
                "${context.getString(R.string.norm_control)} ${productInfo.value?.numberBoxesControl?.toDouble().toStringFormatted()} ${context.getString(R.string.box_abbreviated)}" //Контроль нормы. Z кор.
            }
        } else {
            if (processExciseAlcoBoxAccPGEService.getCountUntreatedBoxes() == 0) {
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
                                        typeDiscrepancies = null,
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
                                val isDefectiveBox = processExciseAlcoBoxAccPGEService.defectiveBox(boxInfo.boxNumber)
                                val typeDiscrepancies = taskManager.getReceivingTask()?.taskRepository?.getBoxesDiscrepancies()?.findBoxesDiscrepanciesOfProduct(productInfo.value!!)?.findLast { it.boxNumber == boxInfo.boxNumber }?.typeDiscrepancies ?: ""
                                BoxListItem(
                                        number = index + 1,
                                        name = "${boxInfo.boxNumber.substring(0,10)} ${boxInfo.boxNumber.substring(10,20)} ${boxInfo.boxNumber.substring(20,26)}",
                                        productInfo = productInfo.value,
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
    }

    fun onClickSelectAll() {
        isSelectAll.value = true
        updateData()
        notProcessedSelectionsHelper.addAll(countNotProcessed.value ?: emptyList())
        isSelectAll.value = false
    }

    fun onClickClean(){
        processedSelectionsHelper.selectedPositions.value?.map { position ->
            processExciseAlcoBoxAccPGEService.cleanBoxInfo(
                    boxNumber = countProcessed.value?.get(position)?.boxInfo?.boxNumber ?: "",
                    typeDiscrepancies = countProcessed.value?.get(position)?.typeDiscrepancies ?: "")
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
            screenNavigator.openExciseAlcoBoxCardPGEScreen(
                    productInfo = productInfo.value!!,
                    boxInfo = if (countSelectedBoxes > 1) null else countNotProcessed.value?.get(notProcessedSelectionsHelper.selectedPositions.value?.last() ?: 0)?.boxInfo,
                    massProcessingBoxesNumber = if (countSelectedBoxes > 1) notProcessedSelectionsHelper.selectedPositions.value?.map {
                        countNotProcessed.value!![it].boxInfo.boxNumber
                    } else null,
                    exciseStampInfo = null,
                    selectQualityCode = selectQualityCode.value!!,
                    initialCount = initialCount.value!!,
                    isScan = false,
                    countAcceptRefusal = countAcceptRefusal.value!!
            )
        }
    }

    fun onClickApply() {
        screenNavigator.goBack()
    }

    fun onClickItemPosition(position: Int) {
        if (selectedPage.value == 0) {
            if (processExciseAlcoBoxAccPGEService.getCountBoxOfProductOfDiscrepancies(countNotProcessed.value?.get(position)?.boxInfo?.boxNumber ?: "") >= countAcceptRefusal.value!! ) {
                screenNavigator.openAlertRequiredQuantityBoxesAlreadyProcessedScreen() //Необходимое количество коробок уже обработано
            } else {
                screenNavigator.goBack()
                screenNavigator.openExciseAlcoBoxCardPGEScreen(
                        productInfo = productInfo.value!!,
                        boxInfo = countNotProcessed.value?.get(position)?.boxInfo,
                        massProcessingBoxesNumber = null,
                        exciseStampInfo = null,
                        selectQualityCode = selectQualityCode.value!!,
                        initialCount = "1",
                        isScan = false,
                        countAcceptRefusal = countAcceptRefusal.value!!
                )
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
            68, 150 -> {
                if (processExciseAlcoBoxAccPGEService.getCountBoxOfProductOfDiscrepancies(data) >= (countAcceptRefusal.value ?: 0.0) ) { //это условие добавлено здесь, т.к. на WM оно тоже есть
                    screenNavigator.openAlertRequiredQuantityBoxesAlreadyProcessedScreen() //Необходимое количество коробок уже обработано
                } else {
                    val exciseStampInfo = processExciseAlcoBoxAccPGEService.searchExciseStamp(data)
                    if (exciseStampInfo == null) {
                        screenNavigator.openAlertScannedStampNotFoundTaskPGEScreen() //Отсканированная марка отсутвует в задании. Отсканируйте номер коробки, а затем номер марки для заявления излишка.
                    } else {
                        if (exciseStampInfo.materialNumber != productInfo.value!!.materialNumber) {
                            //Отсканированная марка принадлежит товару <SAP-код> <Название>"
                            screenNavigator.openAlertScannedStampBelongsAnotherProductScreen(exciseStampInfo.materialNumber, zfmpUtz48V001.getProductInfoByMaterial(exciseStampInfo.materialNumber)?.name
                                    ?: "")
                        } else {
                            screenNavigator.openExciseAlcoBoxCardPGEScreen(
                                    productInfo = productInfo.value!!,
                                    boxInfo = null,
                                    massProcessingBoxesNumber = null,
                                    exciseStampInfo = exciseStampInfo,
                                    selectQualityCode = selectQualityCode.value!!,
                                    initialCount = "1",
                                    isScan = true,
                                    countAcceptRefusal = countAcceptRefusal.value ?: 0.0
                            )
                        }
                    }
                }
            }
            26 -> {
                if (processExciseAlcoBoxAccPGEService.getCountBoxOfProductOfDiscrepancies(data) >= (countAcceptRefusal.value ?: 0.0) ) {
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
                            screenNavigator.openExciseAlcoBoxCardPGEScreen(
                                    productInfo = productInfo.value!!,
                                    boxInfo = boxInfo,
                                    massProcessingBoxesNumber = null,
                                    exciseStampInfo = null,
                                    selectQualityCode = selectQualityCode.value!!,
                                    initialCount = "1",
                                    isScan = true,
                                    countAcceptRefusal = countAcceptRefusal.value ?: 0.0
                            )
                        }
                    }
                }
            }
            else -> screenNavigator.openAlertInvalidBarcodeFormatScannedScreen()
        }
    }

    private fun scannedBoxNotFound(boxNumber: String) {
        viewModelScope.launch {
            screenNavigator.showProgressLoadingData()
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
                            screenNavigator.openExciseAlcoBoxListPGEScreen(
                                    productInfo = productInfo.value!!,
                                    selectQualityCode = selectQualityCode.value!!,
                                    initialCount = initialCount.value!!,
                                    countAcceptRefusal = countAcceptRefusal.value ?: 0.0
                            )
                        }
                )
            }
            "2" -> {
                screenNavigator.openScannedBoxNotIncludedInDeliveryDialog(
                        nextCallbackFunc = {
                            processExciseAlcoBoxAccPGEService.addAllAsSurplusForBox(count = convertEizToBei().toString(), boxNumber = scannedBoxNumber.value ?: "", typeDiscrepancies = "2", isScan = true)
                            screenNavigator.openExciseAlcoBoxListPGEScreen(
                                    productInfo = productInfo.value!!,
                                    selectQualityCode = selectQualityCode.value!!,
                                    initialCount = initialCount.value!!,
                                    countAcceptRefusal = countAcceptRefusal.value ?: 0.0
                            )
                        }
                )
            }
            "3" -> {
                screenNavigator.openScannedBoxNotIncludedInNetworkLentaDialog(
                        nextCallbackFunc = { //todo доработать https://trello.com/c/6NyHp2jB ПГЕ. Карточка короба-излишка
                            val boxInfo = processExciseAlcoBoxAccPGEService.searchBox(boxNumber = scannedBoxNumber.value ?: "")
                            screenNavigator.openExciseAlcoBoxCardPGEScreen(
                                    productInfo = productInfo.value!!,
                                    boxInfo = boxInfo,
                                    massProcessingBoxesNumber = null,
                                    exciseStampInfo = null,
                                    selectQualityCode = selectQualityCode.value!!,
                                    initialCount = initialCount.value!!,
                                    isScan = true,
                                    countAcceptRefusal = countAcceptRefusal.value ?: 0.0
                            )
                        }
                )
            }
        }
    }

    override fun handleFailure(failure: Failure) {
        screenNavigator.openAlertScreen(failure, "97")
    }

    private fun convertEizToBei(): Double {
        var addNewCount = initialCount.value!!.toDouble()
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
        scanCode.value = scanCode.value ?: "" + digit
    }
}
