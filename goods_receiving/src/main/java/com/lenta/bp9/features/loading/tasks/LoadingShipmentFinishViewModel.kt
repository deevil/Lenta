package com.lenta.bp9.features.loading.tasks

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskDescription
import com.lenta.bp9.model.task.TaskNotification
import com.lenta.bp9.model.task.TaskType
import com.lenta.bp9.model.task.revise.TransportConditionRestData
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.requests.network.ZmpUtzGrz38V001NetRequest
import com.lenta.bp9.requests.network.ZmpUtzGrz38V001Params
import com.lenta.bp9.requests.network.ZmpUtzGrz38V001Result
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.features.loading.CoreLoadingViewModel
import com.lenta.shared.utilities.extentions.getDeviceIp
import com.lenta.shared.utilities.extentions.launchUITryCatch
import javax.inject.Inject

class LoadingShipmentFinishViewModel : CoreLoadingViewModel() {

    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var sessionInfo: ISessionInfo
    @Inject
    lateinit var context: Context
    @Inject
    lateinit var taskManager: IReceivingTaskManager
    @Inject
    lateinit var zmpUtzGrz38V001NetRequest: ZmpUtzGrz38V001NetRequest

    override val title: MutableLiveData<String> = MutableLiveData()
    override val progress: MutableLiveData<Boolean> = MutableLiveData(true)
    override val speedKbInSec: MutableLiveData<Int> = MutableLiveData()
    override val sizeInMb: MutableLiveData<Float> = MutableLiveData()

    val taskDescription: String by lazy {
        "\"" + (taskManager.getReceivingTask()?.taskDescription?.currentStatus?.stringValue().orEmpty()) + "\" -> \"" + taskManager.getReceivingTask()?.taskDescription?.nextStatusText + "\""
    }

    init {
        launchUITryCatch {
            progress.value = true
            taskManager.getReceivingTask()?.let { _ ->
                val params = ZmpUtzGrz38V001Params(
                        deviceIP = context.getDeviceIp(),
                        taskNumber = taskManager.getReceivingTask()?.taskHeader?.taskNumber.orEmpty(),
                        personalNumber = sessionInfo.personnelNumber.orEmpty(),
                        transportConditions = taskManager.getReceivingTask()?.taskRepository?.getReviseDocuments()?.getTransportConditions()?.map { TransportConditionRestData.from(it) }
                                ?: emptyList()
                )
                zmpUtzGrz38V001NetRequest(params).either(::handleFailure, ::handleSuccess)
            }
            progress.value = false
        }
    }

    override fun handleFailure(failure: Failure) {
        screenNavigator.goBack()
        screenNavigator.openAlertScreen(failure)
    }

    private fun handleSuccess(result: ZmpUtzGrz38V001Result) {
        screenNavigator.openMainMenuScreen()
        screenNavigator.openTaskListScreen()
        taskManager.updateTaskDescription(TaskDescription.from(result.taskDescription))
        val notifications = result.notifications.map { TaskNotification.from(it) }
        taskManager.getReceivingTask()?.taskRepository?.getNotifications()?.updateWithNotifications(general = notifications, document = null, product = null, condition = null)
        screenNavigator.openTaskCardScreen(TaskCardMode.Full, taskManager.getReceivingTask()?.taskHeader?.taskType ?: TaskType.None)
    }

    override fun clean() {
        progress.postValue(false)
    }
}