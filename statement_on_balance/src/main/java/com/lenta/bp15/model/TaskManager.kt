package com.lenta.bp15.model

import androidx.lifecycle.MutableLiveData
import com.lenta.bp15.model.pojo.Good
import com.lenta.bp15.model.pojo.Mark
import com.lenta.bp15.model.pojo.Task
import com.lenta.bp15.platform.navigation.IScreenNavigator
import com.lenta.bp15.repository.database.IDatabaseRepository
import com.lenta.bp15.repository.net_requests.INetRequestsRepository
import com.lenta.bp15.repository.net_requests.pojo.*
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.platform.device_info.DeviceInfo
import com.lenta.shared.platform.livedata.SingleLiveEvent
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.isSapTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class TaskManager @Inject constructor(
        private val navigator: IScreenNavigator,
        private val sessionInfo: ISessionInfo,
        private val deviceInfo: DeviceInfo,
        private val database: IDatabaseRepository,
        private val netRequests: INetRequestsRepository
) : ITaskManager, CoroutineScope {

    private val job = SupervisorJob()

    override val coroutineContext: CoroutineContext = Dispatchers.Main + job

    private val failure = SingleLiveEvent<Failure>()

    override val processingTasks = MutableLiveData<List<Task>>(emptyList())

    override val searchTasks = MutableLiveData<List<Task>>(emptyList())

    override val currentTask = MutableLiveData<Task>()

    override val currentGood = MutableLiveData<Good>()


    override fun updateProcessingTasks(tasks: List<Task>) {
        processingTasks.value = tasks
    }

    override fun updateSearchTasks(tasks: List<Task>) {
        searchTasks.value = tasks
    }

    override fun updateCurrentTask(task: Task) {
        currentTask.value = task
    }

    override fun loadProcessingTaskList() {
        launch {
            navigator.showProgressLoadingData()

            netRequests.getTaskList(TaskListParams(
                    tkNumber = sessionInfo.market.orEmpty(),
                    userName = sessionInfo.userName.orEmpty(),
                    userNumber = sessionInfo.personnelNumber.orEmpty(),
                    deviceIp = deviceInfo.getDeviceIp(),
                    mode = TaskSearchMode.COMMON.mode
            )).either(::handleFailure) { result ->
                launch {
                    processingTasks.value = getTaskListFromResult(result)
                }
            }

            navigator.hideProgress()
        }
    }

    override fun loadSearchTaskList(searchParams: TaskSearchParams) {
        launch {
            navigator.showProgressLoadingData()

            netRequests.getTaskList(TaskListParams(
                    tkNumber = sessionInfo.market.orEmpty(),
                    userName = sessionInfo.userName.orEmpty(),
                    userNumber = sessionInfo.personnelNumber.orEmpty(),
                    deviceIp = deviceInfo.getDeviceIp(),
                    mode = TaskSearchMode.WITH_PARAMS.mode,
                    searchParams = searchParams
            )).either(::handleFailure) { result ->
                launch {
                    searchTasks.value = getTaskListFromResult(result)
                }
            }

            navigator.hideProgress()
        }
    }

    private suspend fun getTaskListFromResult(result: TaskListResult): List<Task> {
        return result.convertToTasks()?.map { task ->
            task.type = database.getTaskTypeByCode(task.type.code)
            task
        } ?: emptyList()
    }

    override fun setCurrentTask(task: Task) {
        currentTask.value = task
    }

    override suspend fun loadGoodListToCurrentTask() {
        currentTask.value?.let { currentTask ->
            navigator.showProgressLoadingData()

            netRequests.getTaskContent(TaskContentParams(
                    taskNumber = currentTask.number,
                    deviceIp = deviceInfo.getDeviceIp(),
                    userNumber = sessionInfo.personnelNumber.orEmpty(),
                    mode = 1
            )).either(::handleFailure) { result ->
                currentTask.goods = result.positions?.map { positionRawInfo ->
                    Good(
                            material = positionRawInfo.material,
                            planQuantity = positionRawInfo.planQuantity.toIntOrNull() ?: 0,
                            markType = positionRawInfo.markType,
                            marks = result.marks?.filter { it.material == positionRawInfo.material }?.map { markRawInfo ->
                                Mark(
                                        number = markRawInfo.markNumber,
                                        isScan = markRawInfo.isScan.isSapTrue()
                                )
                            } ?: emptyList()
                    )
                } ?: emptyList()

                currentTask.saveStartState()
                updateCurrentTask(currentTask)
            }

            navigator.hideProgress()
        }
    }

    override suspend fun unlockTask(task: Task) {
        navigator.showProgressLoadingData()

        netRequests.unlockTask(UnlockTaskParams(
                taskNumber = task.number,
                userNumber = sessionInfo.personnelNumber.orEmpty(),
                deviceIp = deviceInfo.getDeviceIp()
        )).either(::handleFailure)

        navigator.hideProgress()
    }

    private fun handleFailure(failure: Failure) {
        Logg.e { "handleFailure: $failure" }
        this.failure.postValue(failure)

        navigator.openAlertScreen(failure)
    }

}


interface ITaskManager {

    val processingTasks: MutableLiveData<List<Task>>
    val searchTasks: MutableLiveData<List<Task>>
    val currentTask: MutableLiveData<Task>
    val currentGood: MutableLiveData<Good>

    fun updateProcessingTasks(tasks: List<Task>)
    fun updateSearchTasks(tasks: List<Task>)
    fun updateCurrentTask(task: Task)

    fun loadProcessingTaskList()
    fun loadSearchTaskList(searchParams: TaskSearchParams)
    fun setCurrentTask(task: Task)
    suspend fun loadGoodListToCurrentTask()
    suspend fun unlockTask(task: Task)


}