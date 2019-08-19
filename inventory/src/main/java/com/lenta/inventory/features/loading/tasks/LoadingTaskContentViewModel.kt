package com.lenta.inventory.features.loading.tasks

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.inventory.features.task_list.StatusTask
import com.lenta.inventory.models.RecountType
import com.lenta.inventory.models.StorePlaceLockMode
import com.lenta.inventory.models.task.IInventoryTaskManager
import com.lenta.inventory.models.task.TaskContents
import com.lenta.inventory.models.task.TaskDescription
import com.lenta.inventory.platform.navigation.IScreenNavigator
import com.lenta.inventory.repos.IRepoInMemoryHolder
import com.lenta.inventory.requests.network.*
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.features.loading.CoreLoadingViewModel
import com.lenta.shared.platform.device_info.DeviceInfo
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.getDeviceIp
import kotlinx.coroutines.launch
import javax.inject.Inject

class LoadingTaskContentViewModel : CoreLoadingViewModel() {
    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var taskContentRequest: TaskContentNetRequest
    @Inject
    lateinit var sessionInfo: ISessionInfo
    @Inject
    lateinit var context: Context
    @Inject
    lateinit var repoInMemoryHolder: IRepoInMemoryHolder
    @Inject
    lateinit var taskManager: IInventoryTaskManager
    @Inject
    lateinit var storePlaceLockNetRequest: StorePlaceLockNetRequest
    @Inject
    lateinit var deviceInfo: DeviceInfo

    lateinit var taskInfo: TasksItem
    lateinit var recountType: RecountType

    override val title: MutableLiveData<String> = MutableLiveData()
    override val progress: MutableLiveData<Boolean> = MutableLiveData(true)
    override val speedKbInSec: MutableLiveData<Int> = MutableLiveData()
    override val sizeInMb: MutableLiveData<Float> = MutableLiveData()

    init {
        viewModelScope.launch {
            progress.value = true
            val userNumber = if (recountType == RecountType.ParallelByStorePlaces || sessionInfo.personnelNumber == null) "" else sessionInfo.personnelNumber
            var needsRelock = false
            if (recountType == RecountType.Simple) {
                val status = StatusTask.from(taskInfo, sessionInfo.userName ?: "")
                needsRelock = status == StatusTask.BlockedMe
            }
            taskContentRequest(TaskContentParams(ip = context.getDeviceIp(),
                    taskNumber = taskInfo.taskNumber,
                    userNumber = userNumber ?: "",
                    additionalDataFlag = "",
                    newProductNumbers = emptyList(),
                    numberRelock = if (needsRelock) "X" else "",
                    mode = recountType.recountType)).either(::handleFailure, ::handleSuccess)
            progress.value = false
        }
    }

    override fun handleFailure(failure: Failure) {
        screenNavigator.goBack()
        screenNavigator.openAlertScreen(failure)
    }

    private fun handleSuccess(taskContents: TaskContents) {
        Logg.d { "taskContents $taskContents" }

        if (taskContents.minUpdSales != null) {
            screenNavigator.openMinUpdateSalesDialogScreen(
                    minUpdSales = taskContents.minUpdSales,
                    functionForLeft = {
                        unlockTaskAndGoBack()
                    },
                    functionForRight = {
                        openTakenToWorkScreen(taskContents)
                    }
            )
        } else {
            openTakenToWorkScreen(taskContents)
        }

    }

    private fun unlockTaskAndGoBack() {
        viewModelScope.launch {
            storePlaceLockNetRequest(StorePlaceLockParams(
                    ip = deviceInfo.getDeviceIp(),
                    taskNumber = taskInfo.taskNumber,
                    storePlaceCode = "00",
                    mode = StorePlaceLockMode.Unlock.mode,
                    userNumber = ""
            )).either(fnL = ::handleFailure) {
                Logg.d { "restInfo: $it" }
                taskManager.clearTask()
                screenNavigator.goBack()
                screenNavigator.goBack()
                screenNavigator.hideProgress()
            }
        }

    }

    private fun openTakenToWorkScreen(taskContents: TaskContents) {
        screenNavigator.goBack()
        val taskDescription = TaskDescription.from(
                taskInfo = taskInfo,
                recountType = recountType,
                deadline = taskContents.deadline,
                tkNumber = sessionInfo.market!!,
                linkOldStamp = taskContents.linkOldStamp
        )
        taskManager.newInventoryTask(taskDescription)
        taskManager.getInventoryTask()?.updateTaskWithContents(taskContents)
        screenNavigator.openTakenToWorkFragment()
    }

    override fun clean() {
        progress.postValue(false)
    }

}