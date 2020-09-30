package com.lenta.bp15.model

import androidx.lifecycle.MutableLiveData
import com.lenta.bp15.model.pojo.Task
import com.lenta.bp15.platform.navigation.IScreenNavigator
import com.lenta.bp15.repository.database.IDatabaseRepository
import com.lenta.bp15.repository.net_requests.INetRequestsRepository
import com.lenta.bp15.repository.net_requests.pojo.TaskListParams
import com.lenta.bp15.repository.net_requests.pojo.TaskSearchParams
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.platform.device_info.DeviceInfo
import com.lenta.shared.platform.livedata.SingleLiveEvent
import com.lenta.shared.utilities.Logg
import javax.inject.Inject

class TaskManager @Inject constructor(
        private val navigator: IScreenNavigator,
        private val sessionInfo: ISessionInfo,
        private val deviceInfo: DeviceInfo,
        private val database: IDatabaseRepository,
        private val netRequests: INetRequestsRepository
) : ITaskManager {

    private val failure = SingleLiveEvent<Failure>()


    override val tasks = MutableLiveData<List<Task>>(emptyList())

    override val foundTasks = MutableLiveData<List<Task>>(emptyList())

    override val currentTask = MutableLiveData<Task>()

    //override val currentGood = MutableLiveData<Good>()

    override suspend fun updateTaskList() {
        navigator.showProgressLoadingData()

        netRequests.getTaskList(TaskListParams(
                tkNumber = sessionInfo.market.orEmpty(),
                userName = sessionInfo.userName.orEmpty(),
                userNumber = sessionInfo.personnelNumber.orEmpty(),
                deviceIp = deviceInfo.getDeviceIp(),
                mode = TaskSearchMode.COMMON.mode
        )).either(::handleFailure) { result ->
            tasks.postValue(result.convertToTasks())
        }

        navigator.hideProgress()
    }

    override suspend fun updateFoundTaskList(searchParams: TaskSearchParams) {
        navigator.showProgressLoadingData()

        netRequests.getTaskList(TaskListParams(
                tkNumber = sessionInfo.market.orEmpty(),
                userName = sessionInfo.userName.orEmpty(),
                userNumber = sessionInfo.personnelNumber.orEmpty(),
                deviceIp = deviceInfo.getDeviceIp(),
                mode = TaskSearchMode.WITH_PARAMS.mode,
                searchParams = searchParams
        )).either(::handleFailure) { result ->
            tasks.postValue(result.convertToTasks())
        }

        navigator.hideProgress()
    }

    override fun setCurrentTask(task: Task) {
        currentTask.value = task
    }

    private fun handleFailure(failure: Failure) {
        Logg.e { "handleFailure: $failure" }
        this.failure.postValue(failure)

        navigator.openAlertScreen(failure)
    }

}


interface ITaskManager {

    val tasks: MutableLiveData<List<Task>>
    val foundTasks: MutableLiveData<List<Task>>
    val currentTask: MutableLiveData<Task>

    suspend fun updateTaskList()
    suspend fun updateFoundTaskList(searchParams: TaskSearchParams)
    fun setCurrentTask(task: Task)

}