package com.lenta.bp9.features.loading.tasks

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskDescription
import com.lenta.bp9.model.task.TaskNotification
import com.lenta.bp9.model.task.TaskSectionRestData
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.requests.network.SubmittedNetRequest
import com.lenta.bp9.requests.network.SubmittedParams
import com.lenta.bp9.requests.network.SubmittedRestInfo
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.features.loading.CoreLoadingViewModel
import com.lenta.shared.utilities.extentions.getDeviceIp
import com.lenta.shared.utilities.extentions.launchUITryCatch
import javax.inject.Inject

class LoadingSubmittedViewModel : CoreLoadingViewModel() {

    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var submittedNetRequest: SubmittedNetRequest
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

    init {
        launchUITryCatch {
            progress.value = true
            taskManager.getReceivingTask()?.let { task ->
                val params = SubmittedParams(
                        taskNumber = task.taskHeader.taskNumber,
                        deviceIP = context.getDeviceIp(),
                        personnelNumber = sessionInfo.personnelNumber ?: "",
                        sectionsInfo = task.taskRepository.getSections().getSections().map { TaskSectionRestData.from(it) }
                )
                submittedNetRequest(params).either(::handleFailure, ::handleSuccess)
            }
            progress.value = false
        }
    }

    override fun handleFailure(failure: Failure) {
        screenNavigator.goBack()
        screenNavigator.openAlertScreen(failure)
    }

    private fun handleSuccess(result: SubmittedRestInfo) {
        val notifications = result.notifications.map { TaskNotification.from(it) }
        taskManager.getReceivingTask()?.taskRepository?.getNotifications()?.updateWithNotifications(general = notifications, document = null, product = null, condition = null)
        taskManager.updateTaskDescription(TaskDescription.from(result.taskDescription))
        screenNavigator.openMainMenuScreen()
        screenNavigator.openTaskListScreen()
    }

    override fun clean() {
        progress.postValue(false)
    }
}