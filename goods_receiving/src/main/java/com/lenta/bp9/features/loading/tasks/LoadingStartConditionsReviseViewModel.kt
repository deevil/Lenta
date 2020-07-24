package com.lenta.bp9.features.loading.tasks

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.lenta.bp9.R
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskNotification
import com.lenta.bp9.model.task.TaskStatus
import com.lenta.bp9.model.task.revise.TransportCondition
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.requests.network.StartConditionsReviseNetRequest
import com.lenta.bp9.requests.network.StartConditionsReviseRequestParameters
import com.lenta.bp9.requests.network.StartConditionsReviseRequestResult
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.features.loading.CoreLoadingViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.getDeviceIp
import com.lenta.shared.utilities.extentions.launchUITryCatch
import javax.inject.Inject

class LoadingStartConditionsReviseViewModel : CoreLoadingViewModel() {
    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var startConditionsReviseRequest: StartConditionsReviseNetRequest
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
        "\"" + (taskManager.getReceivingTask()?.taskDescription?.currentStatus?.stringValue() ?: "") + "\" -> \"" + TaskStatus.Unloading.stringValue() + "\""
    }

    init {
        launchUITryCatch {
            progress.value = true
            val params = StartConditionsReviseRequestParameters(
                    deviceIP = context.getDeviceIp(),
                    personalNumber = sessionInfo.personnelNumber ?: "",
                    taskNumber = taskManager.getReceivingTask()?.taskHeader?.taskNumber ?: "",
                    unloadStartDate = taskManager.getReceivingTask()?.taskDescription?.nextStatusDate ?: "",
                    unloadStartTime = taskManager.getReceivingTask()?.taskDescription?.nextStatusTime ?: ""
            )
            startConditionsReviseRequest(params).either(::handleFailure, ::handleSuccess)
            progress.value = false
        }
    }

    override fun handleFailure(failure: Failure) {
        screenNavigator.goBack()
        screenNavigator.openAlertScreen(failure)
    }

    private fun handleSuccess(result: StartConditionsReviseRequestResult) {
        Logg.d { "Start unloading request result $result" }
        screenNavigator.goBack()
        screenNavigator.goBack()

        val conditionNotifications = result.notifications.map { TaskNotification.from(it) }
        taskManager.getReceivingTask()?.taskRepository?.getNotifications()?.updateWithNotifications(null, null, null, conditionNotifications)

        val conditions = result.conditions.map { TransportCondition.from(it) }
        taskManager.getReceivingTask()?.taskRepository?.getReviseDocuments()?.updateTransportCondition(conditions)
        taskManager.getReceivingTask()?.let { task ->
            if (task.taskRepository.getReviseDocuments().getTransportConditions().isNotEmpty()) {
                screenNavigator.openTransportConditionsScreen()
            } else {
                screenNavigator.openTaskListScreen()
                screenNavigator.openCheckingNotNeededAlert(context.getString(R.string.revise_not_needed_unloading)) {
                    screenNavigator.openFinishConditionsReviseLoadingScreen()
                }
            }
        }
    }

    override fun clean() {
        progress.postValue(false)
    }
}