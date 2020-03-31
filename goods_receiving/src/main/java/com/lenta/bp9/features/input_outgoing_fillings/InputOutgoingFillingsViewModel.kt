package com.lenta.bp9.features.input_outgoing_fillings

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.features.loading.tasks.TaskCardMode
import com.lenta.bp9.model.task.*
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.requests.network.FixationDepartureReceptionDistrCenterNetRequest
import com.lenta.bp9.requests.network.FixationDepartureReceptionDistrCenterParameters
import com.lenta.bp9.requests.network.FixationDepartureReceptionDistrCenterResult
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.databinding.Evenable
import com.lenta.shared.utilities.extentions.getDeviceIp
import kotlinx.coroutines.launch
import javax.inject.Inject

class InputOutgoingFillingsViewModel : CoreViewModel() {

    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var taskManager: IReceivingTaskManager
    @Inject
    lateinit var fixationDepartureReceptionDistrCenterNetRequest: FixationDepartureReceptionDistrCenterNetRequest
    @Inject
    lateinit var sessionInfo: ISessionInfo
    @Inject
    lateinit var context: Context

    val taskDescription: String by lazy {
        "\"" + (taskManager.getReceivingTask()?.taskDescription?.currentStatus?.stringValue() ?: "") + "\" -> \"" + TaskStatus.Departure.stringValue() + "\""
    }

    val listInputOutgoingFillings: MutableLiveData<List<InputOutgoingFillingsItem>> = MutableLiveData()



    fun getTitle(): String {
        return taskManager.getReceivingTask()?.taskHeader?.caption ?: ""
    }

    init {
        viewModelScope.launch {
            val quantityOutgoingFillings = if (taskManager.getReceivingTask()?.taskHeader?.taskType == TaskType.ShipmentRC) {
                3
            } else {
                taskManager.getReceivingTask()?.taskDescription?.quantityOutgoingFillings ?: 0
            }
            val listOutgoingFillings: ArrayList<InputOutgoingFillingsItem> = ArrayList()
            for (i in 1..quantityOutgoingFillings) {
                listOutgoingFillings.add(
                        InputOutgoingFillingsItem(
                                number = i,
                                outgoingFillingNumber = MutableLiveData("")
                        )
                )
            }
            listInputOutgoingFillings.value = listOutgoingFillings

        }
    }

    fun onClickSave() {
        viewModelScope.launch {
            screenNavigator.showProgressLoadingData()
            val params = FixationDepartureReceptionDistrCenterParameters(
                    taskNumber = taskManager.getReceivingTask()?.taskHeader?.taskNumber ?: "",
                    deviceIP = context.getDeviceIp(),
                    personalNumber = sessionInfo.personnelNumber ?: "",
                    fillings = listInputOutgoingFillings.value?.map { it.outgoingFillingNumber.toString() } ?: emptyList()
            )
            fixationDepartureReceptionDistrCenterNetRequest(params).either(::handleFailure, ::handleSuccess)
            screenNavigator.hideProgress()
        }
    }

    override fun handleFailure(failure: Failure) {
        screenNavigator.openAlertScreen(failure)
    }

    private fun handleSuccess(result: FixationDepartureReceptionDistrCenterResult) {
        val notifications = result.notifications.map { TaskNotification.from(it) }
        taskManager.getReceivingTask()?.taskRepository?.getNotifications()?.updateWithNotifications(general = notifications, document = null, product = null, condition = null)

        taskManager.updateTaskDescription(TaskDescription.from(result.taskDescription))

        screenNavigator.openTaskCardScreen(TaskCardMode.Full, taskManager.getReceivingTask()?.taskHeader?.taskType ?: TaskType.None)
    }
}

data class InputOutgoingFillingsItem(
        val number: Int,
        val outgoingFillingNumber: MutableLiveData<String>
)