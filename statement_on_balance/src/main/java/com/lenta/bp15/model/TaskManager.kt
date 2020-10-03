package com.lenta.bp15.model

import androidx.lifecycle.MutableLiveData
import com.lenta.bp15.model.enum.LoadTaskContent
import com.lenta.bp15.model.enum.LoadTaskList
import com.lenta.bp15.model.pojo.Good
import com.lenta.bp15.model.pojo.Task
import com.lenta.bp15.platform.navigation.IScreenNavigator
import com.lenta.bp15.repository.database.IDatabaseRepository
import com.lenta.bp15.repository.persist.IPersistRepository
import com.lenta.bp15.repository.requests.INetRequestsRepository
import com.lenta.bp15.repository.requests.pojo.*
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.platform.device_info.DeviceInfo
import com.lenta.shared.platform.livedata.SingleLiveEvent
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.toSapBooleanString
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
        private val netRequests: INetRequestsRepository,
        private val persistRepository: IPersistRepository
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

    override fun updateCurrentGood(good: Good) {
        currentGood.value = good
    }

    override fun loadProcessingTaskList() {
        launch {
            navigator.showProgressLoadingData()

            netRequests.getTaskList(TaskListParams(
                    tkNumber = sessionInfo.market.orEmpty(),
                    userName = sessionInfo.userName.orEmpty(),
                    userNumber = sessionInfo.personnelNumber.orEmpty(),
                    deviceIp = deviceInfo.getDeviceIp(),
                    mode = LoadTaskList.COMMON.mode
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
                    mode = LoadTaskList.WITH_PARAMS.mode,
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

    override fun loadContentToCurrentTask() {
        launch {
            currentTask.value?.let { currentTask ->
                navigator.showProgressLoadingData()

                netRequests.getTaskContent(TaskContentParams(
                        taskNumber = currentTask.number,
                        deviceIp = deviceInfo.getDeviceIp(),
                        userNumber = sessionInfo.personnelNumber.orEmpty(),
                        mode = LoadTaskContent.COMMON.mode
                )).either(::handleFailure) { result ->
                    launch {
                        currentTask.goods = result.convertToGoods().map { good ->
                            database.getGoodAdditionalInfo(good.material)?.let { additionalInfo ->
                                good.putAdditionalInfo(additionalInfo)
                            }
                            good
                        }

                        currentTask.saveStartState()
                        updateCurrentTask(currentTask)
                    }
                }

                navigator.hideProgress()
            }
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

    override suspend fun saveTaskDataToServer(handleSaveDataSuccess: () -> Unit) {
        currentTask.value?.let { task ->
            navigator.showProgressLoadingData()

            netRequests.saveData(SaveDataParams(
                    taskNumber = task.number,
                    userNumber = sessionInfo.personnelNumber.orEmpty(),
                    deviceIp = deviceInfo.getDeviceIp(),
                    isFinish = (!task.isExistUnprocessedGoods()).toSapBooleanString(),
                    marks = task.getProcessedMarkListForSave()
            )).either(::handleFailure) {
                handleSaveDataSuccess.invoke()
            }
        }
    }

    override suspend fun getMaterialByEan(ean: String): String? {
        return database.getMaterialByEan(ean)
    }

    override fun backupCurrentTask() {
        currentTask.value?.let { task ->
            persistRepository.saveTaskData(task)
        }
    }

    override fun restoreCurrentTask() {
        persistRepository.getSavedTaskData()?.let { task ->
            currentTask.value = task
        }
    }

    override fun removeCurrentTaskBackup() {
        persistRepository.clearSavedData()
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
    fun updateCurrentGood(good: Good)

    fun loadProcessingTaskList()
    fun loadSearchTaskList(searchParams: TaskSearchParams)
    fun loadContentToCurrentTask()
    suspend fun unlockTask(task: Task)
    suspend fun getMaterialByEan(ean: String): String?
    suspend fun saveTaskDataToServer(handleSaveDataSuccess: () -> Unit)
    fun backupCurrentTask()
    fun restoreCurrentTask()
    fun removeCurrentTaskBackup()

}