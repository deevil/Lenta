package com.lenta.bp9.features.loading.tasks

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.lenta.bp9.model.task.*
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.repos.IRepoInMemoryHolder
import com.lenta.bp9.requests.network.StartRecountPGENetRequest
import com.lenta.bp9.requests.network.StartRecountPGEParams
import com.lenta.bp9.requests.network.StartRecountPGERestInfo
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.features.loading.CoreLoadingViewModel
import com.lenta.shared.platform.constants.Constants.OPERATING_SYSTEM_ANDROID
import com.lenta.shared.utilities.extentions.getDeviceIp
import com.lenta.shared.utilities.extentions.launchUITryCatch
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
        launchUITryCatch {
            progress.value = true
            taskManager.getReceivingTask()?.let { task ->
                val params = StartRecountPGEParams(
                        taskNumber = task.taskHeader.taskNumber,
                        deviceIP = context.getDeviceIp(),
                        personnelNumber = sessionInfo.personnelNumber.orEmpty(),
                        dateRecount = task.taskDescription.currentStatusDate,
                        timeRecount = task.taskDescription.currentStatusTime,
                        taskType = taskManager.getReceivingTask()
                                ?.taskHeader
                                ?.taskType
                                ?.taskTypeString
                                .orEmpty(),
                        operatingSystem = OPERATING_SYSTEM_ANDROID
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
        launchUITryCatch {
            repoInMemoryHolder.manufacturers.value = result.manufacturers
            repoInMemoryHolder.markingGoodsProperties.value = result.markingGoodsProperties?.map { TaskMarkingGoodsProperties.from(it) }
            taskManager.updateTaskDescription(TaskDescription.from(result.taskDescription))
            taskManager.getReceivingTask()?.updateTaskWithContents(taskContents.getTaskContentsPGEInfo(result))
            screenNavigator.openGoodsListScreen(taskType = taskManager.getReceivingTask()?.taskHeader?.taskType ?: TaskType.None)
        }
    }

    override fun clean() {
        progress.postValue(false)
    }
}