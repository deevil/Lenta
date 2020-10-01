package com.lenta.bp9.features.loading.tasks

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.lenta.bp9.model.task.*
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.requests.network.ZmpUtzGrz40V001NetRequest
import com.lenta.bp9.requests.network.ZmpUtzGrz40V001Params
import com.lenta.bp9.requests.network.ZmpUtzGrz40V001Result
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.features.loading.CoreLoadingViewModel
import com.lenta.shared.utilities.extentions.getDeviceIp
import com.lenta.shared.utilities.extentions.launchUITryCatch
import javax.inject.Inject

class LoadingShipmentEndRecountViewModel : CoreLoadingViewModel() {

    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var sessionInfo: ISessionInfo
    @Inject
    lateinit var context: Context
    @Inject
    lateinit var taskManager: IReceivingTaskManager
    @Inject
    lateinit var zmpUtzGrz40V001NetRequest: ZmpUtzGrz40V001NetRequest

    override val title: MutableLiveData<String> = MutableLiveData()
    override val progress: MutableLiveData<Boolean> = MutableLiveData(true)
    override val speedKbInSec: MutableLiveData<Int> = MutableLiveData()
    override val sizeInMb: MutableLiveData<Float> = MutableLiveData()

    val taskDescription: String by lazy {
        "\"" + (taskManager.getReceivingTask()?.taskDescription?.currentStatus?.stringValue() ?: "") + "\" -> \"" + taskManager.getReceivingTask()?.taskDescription?.nextStatusText + "\""
    }

    init {
        launchUITryCatch {
            progress.value = true
            taskManager.getReceivingTask()?.let { _ ->
                val params = ZmpUtzGrz40V001Params(
                        deviceIP = context.getDeviceIp(),
                        taskNumber = taskManager.getReceivingTask()?.taskHeader?.taskNumber.orEmpty(),
                        taskType = taskManager.getReceivingTask()?.taskHeader?.taskType?.taskTypeString.orEmpty(),
                        personalNumber = sessionInfo.personnelNumber.orEmpty(),
                        cargoUnits = taskManager.getReceivingTask()?.getCargoUnits()?.map { TaskCargoUnitInfoRestData.from(it) }.orEmpty()
                )
                zmpUtzGrz40V001NetRequest(params).either(::handleFailure, ::handleSuccess)
            }
            progress.value = false
        }
    }

    override fun handleFailure(failure: Failure) {
        screenNavigator.goBack()
        screenNavigator.openAlertScreen(failure)
    }

    private fun handleSuccess(result: ZmpUtzGrz40V001Result) {
        val notifications = result.notifications.map { TaskNotification.from(it) }
        taskManager.getReceivingTask()?.taskRepository?.getNotifications()?.updateWithNotifications(general = notifications, document = null, product = null, condition = null)

        taskManager.updateTaskDescription(TaskDescription.from(result.taskDescription))

        screenNavigator.openTaskCardScreen(TaskCardMode.Full, taskManager.getReceivingTask()?.taskHeader?.taskType ?: TaskType.None)
    }

    override fun clean() {
        progress.postValue(false)
    }
}