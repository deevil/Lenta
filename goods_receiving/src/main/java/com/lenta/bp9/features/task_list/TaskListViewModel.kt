package com.lenta.bp9.features.task_list

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
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
import com.lenta.bp9.requests.network.TaskListSearchParams
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.getDeviceIp
import com.lenta.shared.utilities.extentions.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class TaskListViewModel : CoreViewModel(),
        OnOkInSoftKeyboardListener,
        PageSelectionListener {

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

    var requestFocusPageToProcess: MutableLiveData<Boolean> = MutableLiveData()
    var requestFocusPageSearch: MutableLiveData<Boolean> = MutableLiveData()
    var requestFocusPagePostponed: MutableLiveData<Boolean> = MutableLiveData()

    var selectedPage = MutableLiveData(0)
    var orderNumberPageToProcess = MutableLiveData("")
    var orderNumberPageSearch = MutableLiveData("")
    var orderNumberPagePostponed = MutableLiveData("")
    var enteredOrderNumber = MutableLiveData("")

    val tkNumber: String by lazy {
        sessionInfo.market ?: ""
    }

    val tasksToProcess by lazy {
        repoInMemoryHolder.taskList.combineLatest(enteredOrderNumber).map { pair ->
            pair!!.first.tasks.filter { !it.isDelayed && !it.isPaused && it.matchesFilter(pair.second) }.map {
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
        repoInMemoryHolder.lastSearchResult.combineLatest(enteredOrderNumber).map { pair ->
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
        repoInMemoryHolder.taskList.combineLatest(enteredOrderNumber).map { pair ->
            pair!!.first.tasks.filter { (it.isDelayed || it.isPaused) && it.matchesFilter(pair.second) }.map {
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

    val taskListLoadingMode by lazy {
        repoInMemoryHolder.taskList.value?.taskListLoadingMode ?: TaskListLoadingMode.None
    }


    fun onClickRight() {
        if (selectedPage.value == TaskListViewPages.TASK_LIST_VIEW_PAGE_SEARCH) {
            screenNavigator.openTaskSearchScreen(taskListLoadingMode)
        } else {
            viewModelScope.launch {
                screenNavigator.showProgress(taskListNetRequest)
                taskListNetRequest(
                        TaskListParams(
                                storeNumber = sessionInfo.market ?: "",
                                userNumber = sessionInfo.personnelNumber ?: "",
                                searchParams = null,
                                ip = context.getDeviceIp(),
                                type = repoInMemoryHolder.taskList.value?.taskListLoadingMode?.taskListLoadingModeString ?: ""
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

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
        setRequestFocus()
        setEnteredOrderNumber()
    }

    override fun onOkInSoftKeyboard(): Boolean {
        setRequestFocus()
        setEnteredOrderNumber()
        if (enteredOrderNumber.value?.length!! >= 18
                && (taskListLoadingMode == TaskListLoadingMode.Receiving || taskListLoadingMode == TaskListLoadingMode.PGE)) { //https://trello.com/c/zM0vlI9H - приемка, https://trello.com/c/KlGPXY74 - ПГЕ
            selectedPage.value = TaskListViewPages.TASK_LIST_VIEW_PAGE_SEARCH
            screenNavigator.openTaskListLoadingScreen(taskListLoadingMode,
                    TaskListSearchParams(taskNumber = null,
                            supplierNumber = null,
                            documentNumber = null,
                            invoiceNumber = null,
                            transportNumber = null,
                            numberGE = null,
                            numberEO = enteredOrderNumber.value
                    ),
                    numberEO = enteredOrderNumber.value
            )
            enteredOrderNumber.value = ""
        }
        return true
    }

    private fun setEnteredOrderNumber() {
        enteredOrderNumber.value = when (selectedPage.value) {
            TaskListViewPages.TASK_LIST_VIEW_PAGE_TO_PROCESS -> orderNumberPageToProcess.value
            TaskListViewPages.TASK_LIST_VIEW_PAGE_SEARCH -> orderNumberPageSearch.value
            TaskListViewPages.TASK_LIST_VIEW_PAGE_POSTPONED -> orderNumberPagePostponed.value
            else -> null
        }
    }

    private fun setRequestFocus() {
        when (selectedPage.value) {
            TaskListViewPages.TASK_LIST_VIEW_PAGE_TO_PROCESS -> {
                //не менять последовательность, а иначе фокус будет устанавливаться не на нужном EditText
                requestFocusPageSearch.value = false
                requestFocusPagePostponed.value = false
                requestFocusPageToProcess.value = true
            }
            TaskListViewPages.TASK_LIST_VIEW_PAGE_SEARCH -> {
                //не менять последовательность, а иначе фокус будет устанавливаться не на нужном EditText
                requestFocusPagePostponed.value = false
                requestFocusPageToProcess.value = false
                requestFocusPageSearch.value = true
            }
            TaskListViewPages.TASK_LIST_VIEW_PAGE_POSTPONED -> {
                //не менять последовательность, а иначе фокус будет устанавливаться не на нужном EditText
                requestFocusPageToProcess.value = false
                requestFocusPageSearch.value = false
                requestFocusPagePostponed.value = true
            }
        }
    }

    fun onClickItemPosition(position: Int) {
        val task = when (selectedPage.value) {
            TaskListViewPages.TASK_LIST_VIEW_PAGE_TO_PROCESS -> tasksToProcess.value?.get(position)
            TaskListViewPages.TASK_LIST_VIEW_PAGE_SEARCH -> tasksSearch.value?.get(position)
            TaskListViewPages.TASK_LIST_VIEW_PAGE_POSTPONED -> tasksPostponed.value?.get(position)
            else -> null
        }
        task?.let {
            val loadFullData = it.status != TaskStatus.Traveling
                    && it.status != TaskStatus.Ordered
                    && it.status != TaskStatus.ReadyToShipment //ReadyToShipment этот статус добавлен для ОПП, п.п. 5.5.2 из ТП
            when (it.lockStatus) {
                TaskLockStatus.LockedByMe -> {
                    screenNavigator.openConfirmationUnlock {
                        screenNavigator.openTaskCardLoadingScreen(
                                mode = TaskCardMode.Full,
                                taskNumber = it.taskNumber,
                                loadFullData = loadFullData
                        )
                    }
                }
                TaskLockStatus.LockedByOthers -> {
                    screenNavigator.openConfirmationView {
                        screenNavigator.openTaskCardLoadingScreen(
                                mode = TaskCardMode.ReadOnly,
                                taskNumber = it.taskNumber,
                                loadFullData = false
                        )
                    }
                }
                TaskLockStatus.None -> {
                    screenNavigator.openTaskCardLoadingScreen(
                            mode = TaskCardMode.Full,
                            taskNumber = it.taskNumber,
                            loadFullData = loadFullData
                    )
                }
            }
        }
    }

    fun getTasksForPage(page: Int) : LiveData<List<TaskItemVm>> {
        return when (page) {
            TaskListViewPages.TASK_LIST_VIEW_PAGE_TO_PROCESS -> tasksToProcess
            TaskListViewPages.TASK_LIST_VIEW_PAGE_SEARCH -> tasksSearch
            TaskListViewPages.TASK_LIST_VIEW_PAGE_POSTPONED -> tasksPostponed
            else -> MutableLiveData(emptyList())
        }
    }

    fun onScanResult(data: String) {
        if (taskListLoadingMode == TaskListLoadingMode.Receiving || taskListLoadingMode == TaskListLoadingMode.PGE) { //ППП - https://trello.com/c/zM0vlI9H и ПГЕ - https://trello.com/c/KlGPXY74
            when (selectedPage.value) {
                TaskListViewPages.TASK_LIST_VIEW_PAGE_TO_PROCESS -> orderNumberPageToProcess.value = data
                TaskListViewPages.TASK_LIST_VIEW_PAGE_SEARCH -> orderNumberPageSearch.value = data
                TaskListViewPages.TASK_LIST_VIEW_PAGE_POSTPONED -> orderNumberPagePostponed.value = data
            }
            onOkInSoftKeyboard()
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