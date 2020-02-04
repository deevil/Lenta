package com.lenta.bp9.features.loading.tasks

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.R
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskCargoUnitInfo
import com.lenta.bp9.model.task.TaskNotification
import com.lenta.bp9.model.task.TaskStatus
import com.lenta.bp9.model.task.revise.TransportCondition
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.requests.network.*
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.features.loading.CoreLoadingViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.getDeviceIp
import kotlinx.coroutines.launch
import javax.inject.Inject

class LoadingUnloadingStartRDSViewModel : CoreLoadingViewModel() {
    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var unloadingStartRDSNetRequest: UnloadingStartReceptionDistrCenterNetRequest
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
        "\"" + (taskManager.getReceivingTask()?.taskDescription?.currentStatus?.stringValue() ?: "") + "\" -> \"" + taskManager.getReceivingTask()?.taskDescription?.nextStatusText + "\""
    }

    init {
        viewModelScope.launch {
            progress.value = true
            val params = UnloadingStartReceptionDistrCenterParameters(
                    taskNumber = taskManager.getReceivingTask()?.taskHeader?.taskNumber ?: "",
                    deviceIP = context.getDeviceIp(),
                    personalNumber = sessionInfo.personnelNumber ?: "",
                    unloadStartDate = taskManager.getReceivingTask()?.taskDescription?.nextStatusDate ?: "",
                    unloadStartTime = taskManager.getReceivingTask()?.taskDescription?.nextStatusTime ?: "",
                    taskType = taskManager.getReceivingTask()?.taskHeader?.taskType?.taskTypeString ?: ""
            )
            unloadingStartRDSNetRequest(params).either(::handleFailure, ::handleSuccess)
            progress.value = false
        }
    }

    override fun handleFailure(failure: Failure) {
        screenNavigator.goBack()
        screenNavigator.openAlertScreen(failure)
    }

    private fun handleSuccess(result: UnloadingStartReceptionDistrCenterResult) {
        Logg.d { "Start unloading RDS result $result" }
        screenNavigator.goBack()
        screenNavigator.goBack()

        val conditionNotifications = result.conditionNotifications.map { TaskNotification.from(it) }
        taskManager.getReceivingTask()?.taskRepository?.getNotifications()?.updateWithNotifications(null, null, null, conditionNotifications)

        val cargoUnits = result.cargoUnits.map { TaskCargoUnitInfo.from(it) }
        taskManager.getReceivingTask()?.taskRepository?.getCargoUnits()?.updateCargoUnits(cargoUnits)

        val transportConditions = result.transportConditions.map { TransportCondition.from(it) }
        taskManager.getReceivingTask()?.taskRepository?.getReviseDocuments()?.updateTransportCondition(transportConditions)
        screenNavigator.openTransportConditionsScreen()
    }

    override fun clean() {
        progress.postValue(false)
    }
}