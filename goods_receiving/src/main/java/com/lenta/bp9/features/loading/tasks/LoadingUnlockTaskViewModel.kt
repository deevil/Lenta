package com.lenta.bp9.features.loading.tasks

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskType
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.requests.network.UnlockTaskNetRequest
import com.lenta.bp9.requests.network.UnlockTaskRequestParameters
import com.lenta.bp9.requests.network.UnlockTaskRequestResult
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.features.loading.CoreLoadingViewModel
import com.lenta.shared.utilities.extentions.getDeviceIp
import com.lenta.shared.utilities.extentions.launchUITryCatch
import javax.inject.Inject

class LoadingUnlockTaskViewModel : CoreLoadingViewModel() {
    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var unlockTaskRequest: UnlockTaskNetRequest
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
            val params = UnlockTaskRequestParameters(
                    deviceIP = context.getDeviceIp(),
                    personalNumber = sessionInfo.personnelNumber.orEmpty(),
                    taskNumber = taskManager.getReceivingTask()?.taskHeader?.taskNumber.orEmpty()
            )
            unlockTaskRequest(params).either(::handleFailure, ::handleSuccess)
            progress.value = false
        }
    }

    override fun handleFailure(failure: Failure) {
        screenNavigator.goBack()
        screenNavigator.openAlertScreen(failure)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun handleSuccess(result: UnlockTaskRequestResult) {
        when (taskManager.getTaskType()) {
            TaskType.DirectSupplier, TaskType.ReceptionDistributionCenter, TaskType.OwnProduction, TaskType.ShoppingMall -> screenNavigator.openTaskListLoadingScreen(TaskListLoadingMode.Receiving) // ППП\ПРЦ\ПСП\ПТК
            TaskType.RecalculationCargoUnit -> screenNavigator.openTaskListLoadingScreen(TaskListLoadingMode.PGE) //ПГЕ
            TaskType.ShipmentPP, TaskType.ShipmentRC -> screenNavigator.openTaskListLoadingScreen(TaskListLoadingMode.Shipment) //Отгрузка ПП или Отгрузка РЦ
            else -> screenNavigator.openTaskListLoadingScreen(TaskListLoadingMode.None)
        }
    }

    override fun clean() {
        progress.postValue(false)
    }
}