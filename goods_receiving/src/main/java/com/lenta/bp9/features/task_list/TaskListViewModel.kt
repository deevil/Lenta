package com.lenta.bp9.features.task_list

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.features.loading.tasks.TaskListLoadingMode
import com.lenta.bp9.models.task.TaskInfo
import com.lenta.bp9.models.task.TaskList
import com.lenta.bp9.models.task.TaskLockStatus
import com.lenta.bp9.models.task.TaskType
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.repos.IRepoInMemoryHolder
import com.lenta.bp9.requests.TaskListNetRequest
import com.lenta.bp9.requests.TaskListParams
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.SelectionItemsHelper
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
    var filterToProcess = MutableLiveData("")
    var filterSearch = MutableLiveData("")
    var filterPostponed = MutableLiveData("")

    val tkNumber: String by lazy {
        sessionInfo.market ?: ""
    }

    val tasksToProcess by lazy {
        repoInMemoryHolder.taskList.combineLatest(filterToProcess).map { pair ->
            pair!!.first.tasks.filter { !it.isDelayed && it.matchesFilter(pair.second) }.map {
                TaskItemVm(taskPosition = it.position,
                        taskNumber = it.taskNumber,
                        topText = it.topText,
                        bottomText = it.bottomText,
                        lockStatus = it.lockStatus,
                        postponedStatus = TaskPostponedStatus.postponedStatusOfTask(it),
                        skuCount = it.positionsCount)
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
                        skuCount = it.positionsCount)
            }
        }
    }

    val tasksPostponed by lazy {
        repoInMemoryHolder.taskList.combineLatest(filterPostponed).map { pair ->
            pair!!.first.tasks.filter { it.matchesFilter(pair.second) && it.isDelayed}.map {
                TaskItemVm(taskPosition = it.position,
                        taskNumber = it.taskNumber,
                        topText = it.topText,
                        bottomText = it.bottomText,
                        lockStatus = it.lockStatus,
                        postponedStatus = TaskPostponedStatus.postponedStatusOfTask(it),
                        skuCount = it.positionsCount)
            }
        }
    }

    val tasksCount by lazy {
        repoInMemoryHolder.taskList.map { repoInMemoryHolder.taskList.value?.taskCount ?: 0 }
    }

    init {

    }

    fun onClickUpdate() {
        viewModelScope.launch {
            screenNavigator.showProgress(taskListNetRequest)
            taskListNetRequest(
                    TaskListParams(
                            storeNumber = "0020",
                            userNumber = "271296",
                            searchParams = null,
                            ip = context.getDeviceIp(),
                            type = TaskListLoadingMode.Receiving.taskListLoadingModeString
                    )
            )
                    .either(::handleFailure, ::handleUpdateSuccess)
            screenNavigator.hideProgress()
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
        when (selectedPage.value) {
            0 -> filterToProcess.value ?: "" + digit
            1 -> filterSearch.value ?: "" + digit
            2 -> filterPostponed.value ?: "" + digit
        }
    }

    override fun onOkInSoftKeyboard(): Boolean {
        return true
    }

    fun onPageSelected(position: Int) {

    }

    fun onClickItemPosition(position: Int) {

    }

    fun onResume() {

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
        val skuCount: Int
)

enum class TaskPostponedStatus {
    None,
    PauseSign,
    PlaySign;

    companion object {
        fun postponedStatusOfTask(task: TaskInfo): TaskPostponedStatus {
            if (task.isStarted) {
                return PlaySign
            } else if (task.isDelayed || task.isPaused) {
                return  PauseSign
            } else {
                return None
            }
        }
    }
}