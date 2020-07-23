package com.lenta.bp9.features.transport_marriage

import android.content.Context
import com.lenta.shared.platform.viewmodel.CoreViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.features.loading.tasks.TaskCardMode
import com.lenta.bp9.model.task.*
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.requests.network.*
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.Evenable
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.getDeviceIp
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toStringFormatted
import kotlinx.coroutines.launch
import javax.inject.Inject

class TransportMarriageViewModel : CoreViewModel(), PageSelectionListener,
        OnOkInSoftKeyboardListener {

    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var taskManager: IReceivingTaskManager
    @Inject
    lateinit var context: Context
    @Inject
    lateinit var sessionInfo: ISessionInfo
    @Inject
    lateinit var declareTransportDefectNetRequest: DeclareTransportDefectNetRequest
    @Inject
    lateinit var zmpUtzGrz25V001NetRequest: ZmpUtzGrz25V001NetRequest

    val selectedPage = MutableLiveData(0)

    val cargoUnits: MutableLiveData<List<CargoUnitsItem>> = MutableLiveData()

    val filterSearchCargoUnitNumber: MutableLiveData<String> = MutableLiveData("")
    val listCargoUnits by lazy {
        cargoUnits.combineLatest(filterSearchCargoUnitNumber).map {pair ->
            pair!!.first.filter { it.matchesFilter(pair.second) }.map{ taskCargoUnitInfo ->
                CargoUnitsItem(
                        number = taskCargoUnitInfo.number,
                        cargoUnitNumber = taskCargoUnitInfo.cargoUnitNumber,
                        quantityPositions = taskCargoUnitInfo.quantityPositions
                )
            }.reversed()
        }
    }

    val listAct: MutableLiveData<List<ActItem>> = MutableLiveData()
    val actSelectionsHelper = SelectionItemsHelper()

    val requestFocusToCargoUnit: MutableLiveData<Boolean> = MutableLiveData()

    val deleteButtonEnabled: MutableLiveData<Boolean> = actSelectionsHelper.selectedPositions.map {
        it?.isNotEmpty() ?: false
    }

    fun getTitle(): String {
        return taskManager.getReceivingTask()?.taskHeader?.caption ?: ""
    }

    fun onResume() {
        updateListAct()
    }

    init {
        viewModelScope.launch {
            screenNavigator.showProgressLoadingData(::handleFailure)
            taskManager.getReceivingTask()?.let { task ->
                val params = DeclareTransportDefectParams(
                        taskNumber = task.taskHeader.taskNumber,
                        deviceIP = context.getDeviceIp(),
                        personalNumber = sessionInfo.personnelNumber ?: ""
                )
                declareTransportDefectNetRequest(params).either(::handleFailure, ::handleSuccessDeclareTransportDefect)
            }
            screenNavigator.hideProgress()
        }
    }

    private fun handleSuccessDeclareTransportDefect(result: DeclareTransportDefectRestInfo) {
        taskManager.getReceivingTask()?.taskRepository?.getCargoUnits()?.updateCargoUnits(result.cargoUnits.map { TaskCargoUnitInfo.from(it) })
        updateCargoUnits()
    }

    private fun updateCargoUnits() {
        cargoUnits.postValue(
                taskManager.getReceivingTask()?.taskRepository?.getCargoUnits()?.getCargoUnits()?.mapIndexed { index, taskCargoUnitInfo ->
                    CargoUnitsItem(
                            number = index + 1,
                            cargoUnitNumber = taskCargoUnitInfo.cargoUnitNumber,
                            quantityPositions = taskCargoUnitInfo.quantityPositions
                    )
                }?.reversed()
        )
    }

    private fun updateListAct() {
        listAct.postValue(
                taskManager.getReceivingTask()?.taskRepository?.getTransportMarriage()?.getTransportMarriage()?.mapIndexed { index, transportMarriage ->
                    ActItem(
                            number = index + 1,
                            name = "${transportMarriage.getMaterialLastSix()} ${transportMarriage.materialName}",
                            processingUnitName = "ЕО-${transportMarriage.processingUnitNumber}",
                            quantityWithUom = "- ${transportMarriage.quantity.toStringFormatted()} ${transportMarriage.uom.name}",
                            transportMarriage = transportMarriage,
                            even = index % 2 == 0
                    )
                }?.reversed()
        )
        actSelectionsHelper.clearPositions()
    }

    fun onClickCancellation() {
        viewModelScope.launch {
            screenNavigator.showProgressLoadingData(::handleFailure)
            taskManager.getReceivingTask()?.let { task ->
                val params = ZmpUtzGrz25V001Params(
                        taskNumber = task.taskHeader.taskNumber,
                        deviceIP = context.getDeviceIp(),
                        personalNumber = sessionInfo.personnelNumber ?: "",
                        transportMarriage = emptyList(),
                        taskBoxDiscrepancies = emptyList(),
                        taskExciseStampsDiscrepancies = emptyList(),
                        isSave = "",
                        printerName = ""
                )
                zmpUtzGrz25V001NetRequest(params).either(::handleFailure, ::handleSuccessCancellation)
            }
            taskManager.getReceivingTask()?.taskRepository?.getTransportMarriage()?.clear()
            screenNavigator.hideProgress()
        }
    }

    private fun handleSuccessCancellation(result: ZmpUtzGrz25V001Result) {
        screenNavigator.openMainMenuScreen()
        screenNavigator.openTaskListScreen()
        taskManager.updateTaskDescription(TaskDescription.from(result.taskDescription))
        val notifications = result.notifications.map { TaskNotification.from(it) }
        taskManager.getReceivingTask()?.taskRepository?.getNotifications()?.updateWithNotifications(general = notifications, document = null, product = null, condition = null)
        screenNavigator.openTaskCardScreen(TaskCardMode.Full, taskManager.getReceivingTask()?.taskHeader?.taskType ?: TaskType.None)
    }

    fun onClickDelete() {
        actSelectionsHelper.selectedPositions.value?.map { position ->
            listAct.value?.get(position)?.transportMarriage?.let {
                taskManager.
                        getReceivingTask()?.
                        taskRepository?.
                        getTransportMarriage()?.
                        deleteTransportMarriage(it)
            }
        }
        updateListAct()
    }

    fun onClickProcess() {
        viewModelScope.launch {
            screenNavigator.showProgressLoadingData(::handleFailure)
            taskManager.getReceivingTask()?.let { task ->
                val params = ZmpUtzGrz25V001Params(
                        taskNumber = task.taskHeader.taskNumber,
                        deviceIP = context.getDeviceIp(),
                        personalNumber = sessionInfo.personnelNumber ?: "",
                        transportMarriage = task.taskRepository.getTransportMarriage().getTransportMarriage().map { TaskTransportMarriageInfoRestData.from(it) },
                        taskBoxDiscrepancies = task.taskRepository.getBoxesDiscrepancies().getBoxesDiscrepancies().map { TaskBoxDiscrepanciesRestData.from(it) }, //it_box_diff - пусто (заполнять только для марочного алкоголя, при наличии отсканированных марок) https://trello.com/c/ndvDINaT
                        taskExciseStampsDiscrepancies = task.taskRepository.getExciseStampsDiscrepancies().getExciseStampDiscrepancies().map { TaskExciseStampDiscrepanciesRestData.from(it) }, //it_mark_diff - пусто(заполнять только для марочного алкоголя, при наличии отсканированных марок) https://trello.com/c/ndvDINaT
                        isSave = "X",
                        printerName = sessionInfo.printer ?: ""
                )
                zmpUtzGrz25V001NetRequest(params).either(::handleFailure, ::handleSuccessProcess)
            }
            screenNavigator.hideProgress()
        }
    }

    private fun handleSuccessProcess(result: ZmpUtzGrz25V001Result) {
        val notifications = result.notifications.map { TaskNotification.from(it) }
        taskManager.getReceivingTask()?.taskRepository?.getNotifications()?.updateWithNotifications(general = notifications, document = null, product = null, condition = null)
        taskManager.updateTaskDescription(TaskDescription.from(result.taskDescription))
        screenNavigator.openMainMenuScreen()
        screenNavigator.openTaskListScreen()
        screenNavigator.openSupplyResultsActDisagreementTransportationDialog(transportationNumber = taskManager.getReceivingTask()?.taskDescription?.transportationNumber ?: "",
                docCallbackFunc = {
                    screenNavigator.openTaskCardScreen(TaskCardMode.Full, taskManager.getReceivingTask()?.taskHeader?.taskType ?: TaskType.None)
                    screenNavigator.openFormedDocsScreen()
                },
                nextCallbackFunc = {
                    screenNavigator.openTaskCardScreen(TaskCardMode.Full, taskManager.getReceivingTask()?.taskHeader?.taskType ?: TaskType.None)
                })
    }

    fun onClickItemPosition(position: Int) {
        if (selectedPage.value == 0) {
            screenNavigator.openTransportMarriageCargoUnitScreen(listCargoUnits.value?.get(position)?.cargoUnitNumber ?: "")
        } else {
            listAct.value?.get(position)?.transportMarriage?.let {
                screenNavigator.openTransportMarriageGoodsInfoScreen(transportMarriageInfo = it)
            }
        }
    }

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    override fun onOkInSoftKeyboard(): Boolean {
        searchCargoUnit(filterSearchCargoUnitNumber.value ?: "")
        return true
    }

    fun onScanResult(data: String) {
        if (selectedPage.value == 0) {
            searchCargoUnit(data)
        }
    }

    private fun searchCargoUnit(data: String) {
        if (data.length != 18) {
            screenNavigator.openAlertInvalidBarcodeFormatScreen()
        } else {
            val findCargoUnit = taskManager.getReceivingTask()?.taskRepository?.getCargoUnits()?.findCargoUnits(data)
            if (findCargoUnit == null) {
                screenNavigator.openAlertCargoUnitNotFoundScreen()
            } else {
                screenNavigator.openTransportMarriageCargoUnitScreen(findCargoUnit.cargoUnitNumber)
            }
        }
    }

    fun onDigitPressed(digit: Int) {
        requestFocusToCargoUnit.value = true
        filterSearchCargoUnitNumber.value = filterSearchCargoUnitNumber.value ?: "" + digit
    }

    override fun handleFailure(failure: Failure) {
        screenNavigator.goBack()
        screenNavigator.openAlertScreen(failure)
    }

}

data class CargoUnitsItem(
        val number: Int,
        val cargoUnitNumber: String,
        val quantityPositions: String
)  {
    fun matchesFilter(filter: String): Boolean {
        return cargoUnitNumber.contains(filter, true)
    }
}

data class ActItem(
        val number: Int,
        val name: String,
        val processingUnitName: String,
        val quantityWithUom: String,
        val transportMarriage: TaskTransportMarriageInfo,
        val even: Boolean
) : Evenable {
    override fun isEven() = even
}
