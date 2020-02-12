package com.lenta.bp9.features.transport_marriage

import android.content.Context
import com.lenta.shared.platform.viewmodel.CoreViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.features.loading.tasks.TaskCardMode
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskCargoUnitInfo
import com.lenta.bp9.model.task.TaskDescription
import com.lenta.bp9.model.task.TaskNotification
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.requests.network.*
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.Evenable
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.getDeviceIp
import com.lenta.shared.utilities.extentions.map
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
    val listCargoUnits: MutableLiveData<List<ListCargoUnitsItem>> = MutableLiveData()
    val listAct: MutableLiveData<List<ListAct>> = MutableLiveData()
    val actSelectionsHelper = SelectionItemsHelper()
    val cargoUnitNumber: MutableLiveData<String> = MutableLiveData("")
    val requestFocusToCargoUnit: MutableLiveData<Boolean> = MutableLiveData()

    val deleteButtonEnabled: MutableLiveData<Boolean> = actSelectionsHelper.selectedPositions.map {
        it?.isNotEmpty() ?: false
    }

    fun getTitle(): String {
        return taskManager.getReceivingTask()?.taskHeader?.caption ?: ""
    }

    init {
        viewModelScope.launch {
            screenNavigator.showProgressLoadingData()
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
        updateData()
    }

    private fun updateData() {
        listCargoUnits.postValue(
                taskManager.getReceivingTask()?.taskRepository?.getCargoUnits()?.getCargoUnits()?.mapIndexed { index, taskCargoUnitInfo ->
                    ListCargoUnitsItem(
                            number = index + 1,
                            cargoUnitNumber = taskCargoUnitInfo.cargoUnitNumber,
                            quantityPositions = taskCargoUnitInfo.quantityPositions,
                            even = index % 2 == 0
                    )
                }?.reversed()
        )
    }

    fun onClickCancellation() {
        viewModelScope.launch {
            screenNavigator.showProgressLoadingData()
            taskManager.getReceivingTask()?.let { task ->
                val params = ZmpUtzGrz25V001Params(
                        taskNumber = task.taskHeader.taskNumber,
                        deviceIP = context.getDeviceIp(),
                        personalNumber = sessionInfo.personnelNumber ?: "",
                        transportMarriage = emptyList(),
                        processedBoxInfo = emptyList(),
                        processedExciseStamp = emptyList(),
                        isSave = "",
                        printerName = ""
                )
                zmpUtzGrz25V001NetRequest(params).either(::handleFailure, ::handleSuccessCancellation)
            }
            screenNavigator.hideProgress()
        }
    }

    private fun handleSuccessCancellation(result: ZmpUtzGrz25V001Result) {
        screenNavigator.openMainMenuScreen()
        screenNavigator.openTaskListScreen()
        taskManager.updateTaskDescription(TaskDescription.from(result.taskDescription))
        val notifications = result.notifications.map { TaskNotification.from(it) }
        taskManager.getReceivingTask()?.taskRepository?.getNotifications()?.updateWithNotifications(general = notifications, document = null, product = null, condition = null)
        screenNavigator.openTaskCardScreen(TaskCardMode.Full)
    }

    fun onClickProcess() {
        viewModelScope.launch {
            screenNavigator.showProgressLoadingData()
            taskManager.getReceivingTask()?.let { task ->
                val params = ZmpUtzGrz25V001Params(
                        taskNumber = task.taskHeader.taskNumber,
                        deviceIP = context.getDeviceIp(),
                        personalNumber = sessionInfo.personnelNumber ?: "",
                        transportMarriage = emptyList(),
                        processedBoxInfo = emptyList(), //it_box_diff - пусто (заполнять только для марочного алкоголя, при наличии отсканированных марок) https://trello.com/c/ndvDINaT
                        processedExciseStamp = emptyList(), //it_mark_diff - пусто(заполнять только для марочного алкоголя, при наличии отсканированных марок) https://trello.com/c/ndvDINaT
                        isSave = "X",
                        printerName = ""
                )
                zmpUtzGrz25V001NetRequest(params).either(::handleFailure, ::handleSuccessProcess)
            }
            screenNavigator.hideProgress()
        }
    }

    private fun handleSuccessProcess(result: ZmpUtzGrz25V001Result) {
        screenNavigator.openMainMenuScreen()
        screenNavigator.openTaskListScreen()
        taskManager.updateTaskDescription(TaskDescription.from(result.taskDescription))
        val notifications = result.notifications.map { TaskNotification.from(it) }
        taskManager.getReceivingTask()?.taskRepository?.getNotifications()?.updateWithNotifications(general = notifications, document = null, product = null, condition = null)
        screenNavigator.openTaskCardScreen(TaskCardMode.Full)
    }

    fun onClickItemPosition(position: Int) {
        /**val matnr: String? = if (selectedPage.value == 0) {
            listCounted.value?.get(position)?.productInfo?.materialNumber
        } else {
            listWithoutBarcode.value?.get(position)?.productInfo?.materialNumber
        }
        searchProductDelegate.searchCode(code = matnr ?: "", fromScan = false)*/
    }

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    override fun onOkInSoftKeyboard(): Boolean {
        searchCargoUnit(cargoUnitNumber.value ?: "")
        return true
    }

    fun onScanResult(data: String) {
        searchCargoUnit(data)
    }

    private fun searchCargoUnit(data: String) {
        /**viewModelScope.launch {
            searchCargoUnitNumber.value = data
            val findCargoUnit = processCargoUnitsService.findCargoUnit(data)
            if (findCargoUnit == null) {
                screenNavigator.showProgressLoadingData()
                val params = GettingDataNewCargoUnitParameters(
                        taskNumber = taskManager.getReceivingTask()?.taskHeader?.taskNumber ?: "",
                        cargoUnitNumber = data
                )
                gettingDataNewCargoUnit(params).either(::handleFailure, ::handleSuccessNewCargoUnit)
                screenNavigator.hideProgress()
            } else {
                screenNavigator.openCargoUnitCardScreen(findCargoUnit)
            }
        }*/
    }

    fun onDigitPressed(digit: Int) {
        requestFocusToCargoUnit.value = true
        cargoUnitNumber.value = cargoUnitNumber.value ?: "" + digit
    }

    override fun handleFailure(failure: Failure) {
        screenNavigator.goBack()
        screenNavigator.openAlertScreen(failure)
    }

}

data class ListCargoUnitsItem(
        val number: Int,
        val cargoUnitNumber: String,
        val quantityPositions: String,
        val even: Boolean
) : Evenable {
    override fun isEven() = even
}

data class ListAct(
        val number: Int,
        val name: String,
        val quantityWithUom: String,
        val even: Boolean
) : Evenable {
    override fun isEven() = even
}
