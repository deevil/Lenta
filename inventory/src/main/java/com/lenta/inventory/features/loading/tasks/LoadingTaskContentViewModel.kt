package com.lenta.inventory.features.loading.tasks

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.inventory.features.task_list.TaskItemVm
import com.lenta.inventory.models.RecountType
import com.lenta.inventory.models.task.IInventoryTaskManager
import com.lenta.inventory.models.task.InventoryTaskManager
import com.lenta.inventory.models.task.TaskContents
import com.lenta.inventory.models.task.TaskDescription
import com.lenta.inventory.platform.navigation.IScreenNavigator
import com.lenta.inventory.repos.IRepoInMemoryHolder
import com.lenta.inventory.requests.network.TaskContentNetRequest
import com.lenta.inventory.requests.network.TaskContentParams
import com.lenta.inventory.requests.network.TaskContentRestInfo
import com.lenta.inventory.requests.network.TasksItem
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.features.loading.CoreLoadingViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.getDeviceIp
import kotlinx.coroutines.launch
import javax.inject.Inject

class LoadingTaskContentViewModel: CoreLoadingViewModel() {
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

    var taskInfo: TasksItem? = null
    var recountType: RecountType? = null

    override val title: MutableLiveData<String> = MutableLiveData()
    override val progress: MutableLiveData<Boolean> = MutableLiveData(true)
    override val speedKbInSec: MutableLiveData<Int> = MutableLiveData()
    override val sizeInMb: MutableLiveData<Float> = MutableLiveData()

    init {
        viewModelScope.launch {
            progress.value = true
            taskContentRequest(TaskContentParams(ip = context.getDeviceIp(),
                    taskNumber = taskInfo?.taskNumber ?: "",
                    userNumber = sessionInfo.personnelNumber ?: "",
                    additionalDataFlag = "",
                    newProductNumbers = emptyList(),
                    numberRelock = "",
                    mode = recountType?.recountType ?: "")).either(::handleFailure, ::handleSuccess)
            progress.value = false
        }
    }

    override fun handleFailure(failure: Failure) {
        screenNavigator.goBack()
        screenNavigator.openAlertScreen(failure)
    }

    private fun handleSuccess(taskContents: TaskContents) {
        Logg.d { "taskContents $taskContents" }
        screenNavigator.goBack()
        taskInfo?.let {
            val taskDescription = TaskDescription.from(it, recountType ?: RecountType.None, taskContents.deadline)
            taskManager.newInventoryTask(taskDescription)
            taskManager.getInventoryTask()?.updateTaskWithContents(taskContents)
            when (recountType) {
                RecountType.Simple, RecountType.ParallelByPerNo -> screenNavigator.openGoodsListScreen()
                RecountType.ParallelByStorePlaces -> screenNavigator.openStoragesList()
            }
        }
    }

    override fun clean() {
        progress.postValue(false)
    }

}