package com.lenta.bp9.features.loading.tasks

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskContents
import com.lenta.bp9.model.task.TaskDescription
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.repos.IRepoInMemoryHolder
import com.lenta.bp9.requests.network.StartRecountPGENetRequest
import com.lenta.bp9.requests.network.StartRecountPGEParams
import com.lenta.bp9.requests.network.StartRecountPGERestInfo
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.features.loading.CoreLoadingViewModel
import com.lenta.shared.utilities.extentions.getDeviceIp
import kotlinx.coroutines.launch
import javax.inject.Inject

class LoadingRecountStartPGEViewModel : CoreLoadingViewModel() {

    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var startRecountPGENetRequest: StartRecountPGENetRequest
    @Inject
    lateinit var sessionInfo: ISessionInfo
    @Inject
    lateinit var context: Context
    @Inject
    lateinit var taskManager: IReceivingTaskManager
    @Inject
    lateinit var taskContents: TaskContents
    @Inject
    lateinit var repoInMemoryHolder: IRepoInMemoryHolder


    override val title: MutableLiveData<String> = MutableLiveData()
    override val progress: MutableLiveData<Boolean> = MutableLiveData(true)
    override val speedKbInSec: MutableLiveData<Int> = MutableLiveData()
    override val sizeInMb: MutableLiveData<Float> = MutableLiveData()

    val toolbarDescription: String by lazy {
        "\"" + (taskManager.getReceivingTask()?.taskDescription?.currentStatus?.stringValue() ?: "") + "\" -> \"" + taskManager.getReceivingTask()?.taskDescription?.nextStatusText + "\""
    }

    init {
        viewModelScope.launch {
            progress.value = true
            taskManager.getReceivingTask()?.let { task ->
                val params = StartRecountPGEParams(
                        taskNumber = task.taskHeader.taskNumber,
                        deviceIP = context.getDeviceIp(),
                        personnelNumber = sessionInfo.personnelNumber ?: "",
                        dateRecount = task.taskDescription.currentStatusDate,
                        timeRecount = task.taskDescription.currentStatusTime,
                        taskType = taskManager.getReceivingTask()?.taskHeader?.taskType?.taskTypeString ?: ""
                )
                startRecountPGENetRequest(params).either(::handleFailure, ::handleSuccess)
            }
            progress.value = false
        }
    }

    override fun handleFailure(failure: Failure) {
        screenNavigator.goBack()
        screenNavigator.openAlertScreen(failure)
    }

    private fun handleSuccess(result: StartRecountPGERestInfo) {
        viewModelScope.launch {
            repoInMemoryHolder.manufacturers.value = result.manufacturers
            taskManager.updateTaskDescription(TaskDescription.from(result.taskDescription))
            taskManager.getReceivingTask()?.updateTaskWithContentsPGE(taskContents.getTaskContentsPGEInfo(result))
            screenNavigator.openGoodsListScreen()
        }
    }

    override fun clean() {
        progress.postValue(false)
    }
}