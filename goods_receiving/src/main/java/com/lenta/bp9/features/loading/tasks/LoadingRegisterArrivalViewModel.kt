package com.lenta.bp9.features.loading.tasks

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskDescription
import com.lenta.bp9.model.task.TaskNotification
import com.lenta.bp9.model.task.TaskStatus
import com.lenta.bp9.model.task.revise.*
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.repos.IRepoInMemoryHolder
import com.lenta.bp9.requests.network.*
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.features.loading.CoreLoadingViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.getDeviceIp
import kotlinx.coroutines.launch
import javax.inject.Inject

class LoadingRegisterArrivalViewModel : CoreLoadingViewModel() {
    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var regissterArrivalRequest: RegisterArrivalNetRequest
    @Inject
    lateinit var sessionInfo: ISessionInfo
    @Inject
    lateinit var context: Context
    @Inject
    lateinit var taskManager: IReceivingTaskManager

    override val title: MutableLiveData<String> = MutableLiveData()
    override val progress: MutableLiveData<Boolean> = MutableLiveData(true)
    override val speedKbInSec: MutableLiveData<Int> = MutableLiveData()
    override val sizeInMb: MutableLiveData<Float> = MutableLiveData()

    val taskDescription: String by lazy {
        "\"" + (taskManager.getReceivingTask()?.taskDescription?.currentStatus?.stringValue() ?: "") + "\" -> \"" + TaskStatus.Arrived.stringValue() + "\""
    }

    init {
        viewModelScope.launch {
            progress.value = true
            val params = RegisterArrivalRequestParameters(
                    deviceIP = context.getDeviceIp(),
                    personalNumber = sessionInfo.personnelNumber ?: "",
                    taskNumber = taskManager.getReceivingTask()?.taskHeader?.taskNumber ?: "",
                    dateArrival = taskManager.getReceivingTask()?.taskDescription?.nextStatusDate ?: "",
                    timeArrival = taskManager.getReceivingTask()?.taskDescription?.nextStatusTime ?: "",
                    hasPaperTTN = "",
                    isSelfRegistration = ""
            )
            regissterArrivalRequest(params).either(::handleFailure, ::handleSuccess)
            progress.value = false
        }
    }

    override fun handleFailure(failure: Failure) {
        screenNavigator.goBack()
        screenNavigator.openAlertScreen(failure)
    }

    private fun handleSuccess(result: RegisterArrivalRequestResult) {
        Logg.d { "Register arrival request result $result" }
        screenNavigator.goBack()
        screenNavigator.goBack()
        taskManager.getReceivingTask()?.taskHeader?.let {
            val notifications = result.notifications.map { TaskNotification.from(it) }
            val shipmentExists = result.shipmentExists.isNotEmpty()
            val newTask = taskManager.newReceivingTask(it, TaskDescription.from(result.taskDescription))
            newTask?.taskRepository?.getNotifications()?.updateWithNotifications(notifications, null, null, null)
            taskManager.setTask(newTask)
            screenNavigator.openTaskCardScreen(TaskCardMode.Full)
        }
    }

    override fun clean() {
        progress.postValue(false)
    }
}