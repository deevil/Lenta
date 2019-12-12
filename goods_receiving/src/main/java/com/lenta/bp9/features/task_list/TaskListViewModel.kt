package com.lenta.bp9.features.task_list

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.R
import com.lenta.bp9.features.loading.tasks.TaskCardMode
import com.lenta.bp9.features.loading.tasks.TaskListLoadingMode
import com.lenta.bp9.model.task.TaskInfo
import com.lenta.bp9.model.task.TaskList
import com.lenta.bp9.model.task.TaskLockStatus
import com.lenta.bp9.model.task.TaskStatus
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.repos.IRepoInMemoryHolder
import com.lenta.bp9.requests.network.TaskListNetRequest
import com.lenta.bp9.requests.network.TaskListParams
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.getDeviceIp
import com.lenta.shared.utilities.extentions.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class TaskListViewModel : CoreViewModel(), OnOkInSoftKeyboardListener {

    @Inject
    lateinit var sessionInfo: ISessionInfo
    @Inject
    lateinit var repoInMemoryHolder: IRepoInMemoryHolder
    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var taskListNetRequest: TaskListNetRequest
    @Inject
    lateinit var context: Context

    var requestFocusToFilter: MutableLiveData<Boolean> = MutableLiveData()

    var selectedPage = MutableLiveData(0)
    var filterSearch = MutableLiveData("")

    val tkNumber: String by lazy {
        sessionInfo.market ?: ""
    }

    val tasksToProcess by lazy {
        repoInMemoryHolder.taskList.combineLatest(filterSearch).map { pair ->
            pair!!.first.tasks.filter { !it.isDelayed && it.matchesFilter(pair.second) }.map {
                TaskItemVm(taskPosition = it.position,
                        taskNumber = it.taskNumber,
                        topText = it.topText,
                        bottomText = it.bottomText,
                        lockStatus = it.lockStatus,
                        postponedStatus = TaskPostponedStatus.postponedStatusOfTask(it),
                        skuCount = it.positionsCount,
                        status = it.status)
            }
        }
    }

    val tasksSearch by lazy {
        repoInMemoryHolder.lastSearchResult.combineLatest(filterSearch).map { pair ->
            pair!!.first.tasks.filter { it.matchesFilter(pair.second) }.map {
                TaskItemVm(taskPosition = it.position,
                        taskNumber = it.taskNumber,
                        topText = it.topText,
                        bottomText = it.bottomText,
                        lockStatus = it.lockStatus,
                        postponedStatus = TaskPostponedStatus.postponedStatusOfTask(it),
                        skuCount = it.positionsCount,
                        status = it.status)
            }
        }
    }

    val tasksPostponed by lazy {
        repoInMemoryHolder.taskList.combineLatest(filterSearch).map { pair ->
            pair!!.first.tasks.filter { it.isDelayed && it.matchesFilter(pair.second) }.map {
                TaskItemVm(taskPosition = it.position,
                        taskNumber = it.taskNumber,
                        topText = it.topText,
                        bottomText = it.bottomText,
                        lockStatus = it.lockStatus,
                        postponedStatus = TaskPostponedStatus.postponedStatusOfTask(it),
                        skuCount = it.positionsCount,
                        status = it.status)
            }
        }
    }

    val tasksCount by lazy {
        repoInMemoryHolder.taskList.map { repoInMemoryHolder.taskList.value?.taskCount ?: 0 }
    }

    fun onClickRight() {
        if (selectedPage.value == 1) {
            screenNavigator.openTaskSearchScreen()
        } else {
            viewModelScope.launch {
                screenNavigator.showProgress(taskListNetRequest)
                taskListNetRequest(
                        TaskListParams(
                                storeNumber = sessionInfo.market ?: "",
                                userNumber = sessionInfo.personnelNumber ?: "",
                                searchParams = null,
                                ip = context.getDeviceIp(),
                                type = TaskListLoadingMode.Receiving.taskListLoadingModeString
                        )
                )
                        .either(::handleFailure, ::handleUpdateSuccess)
                screenNavigator.hideProgress()
            }
        }
    }

    private fun handleUpdateSuccess(taskList: TaskList) {
        Logg.d { "taskList $taskList" }
        repoInMemoryHolder.taskList.value = taskList
    }

    fun onClickMenu() {
        screenNavigator.openMainMenuScreen()
    }

    override fun handleFailure(failure: Failure) {
        super.handleFailure(failure)
        screenNavigator.openAlertScreen(failure)
    }

    fun onDigitPressed(digit: Int) {
        requestFocusToFilter.value = true
        filterSearch.value ?: "" + digit
    }

    override fun onOkInSoftKeyboard(): Boolean {
        return true
    }

    fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    fun onClickItemPosition(position: Int) {
        val task = when (selectedPage.value) {
            0 -> tasksToProcess.value?.get(position)
            1 -> tasksSearch.value?.get(position)
            2 -> tasksPostponed.value?.get(position)
            else -> null
        }
        task?.let {
            val loadFullData = it.status != TaskStatus.Traveling && it.status != TaskStatus.Ordered
            when (it.lockStatus) {
                TaskLockStatus.LockedByMe -> {
                    screenNavigator.openConfirmationUnlock {
                        screenNavigator.openTaskCardLoadingScreen(TaskCardMode.Full, it.taskNumber, loadFullData)
                    }
                }
                TaskLockStatus.LockedByOthers -> {
                    screenNavigator.openConfirmationView {
                        screenNavigator.openTaskCardLoadingScreen(TaskCardMode.Full, it.taskNumber, loadFullData = false)
                    }
                }
                TaskLockStatus.None -> screenNavigator.openTaskCardLoadingScreen(TaskCardMode.Full, it.taskNumber, loadFullData)
            }
        }
    }

    fun getTasksForPage(page: Int) : LiveData<List<TaskItemVm>> {
        return when (page) {
            0 -> tasksToProcess
            1 -> tasksSearch
            2 -> tasksPostponed
            else -> MutableLiveData(emptyList())
        }
    }
}

data class TaskItemVm(
        val taskPosition: String,
        val taskNumber: String,
        val topText: String,
        val bottomText: String,
        val lockStatus: TaskLockStatus,
        val postponedStatus: TaskPostponedStatus,
        val skuCount: Int,
        val status: TaskStatus
)

enum class TaskPostponedStatus {
    None,
    PauseSign,
    PlaySign,
    Breaking;

    companion object {
        fun postponedStatusOfTask(task: TaskInfo): TaskPostponedStatus {
            //последовательность if не менять!!!!
            return if (task.isCracked) {
                Breaking
            } else if (task.isDelayed || task.isPaused) {
                PauseSign
            } else if (task.isStarted) {
                PlaySign
            } else {
                None
            }
        }
    }
}