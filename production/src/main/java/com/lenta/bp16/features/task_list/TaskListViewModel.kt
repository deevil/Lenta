package com.lenta.bp16.features.task_list

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp16.model.ITaskManager
import com.lenta.bp16.model.Tabs
import com.lenta.bp16.model.TaskStatus
import com.lenta.bp16.model.TaskType
import com.lenta.bp16.model.pojo.Task
import com.lenta.bp16.platform.navigation.IScreenNavigator
import com.lenta.bp16.request.TaskInfoNetRequest
import com.lenta.bp16.request.TaskInfoParams
import com.lenta.bp16.request.TaskListNetRequest
import com.lenta.bp16.request.TaskListParams
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.isSapTrue
import com.lenta.shared.utilities.extentions.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class TaskListViewModel : CoreViewModel(), PageSelectionListener, OnOkInSoftKeyboardListener {

    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var sessionInfo: ISessionInfo
    @Inject
    lateinit var taskManager: ITaskManager
    @Inject
    lateinit var taskListNetRequest: TaskListNetRequest
    @Inject
    lateinit var taskInfoNetRequest: TaskInfoNetRequest


    val title by lazy {
        "ТК - ${sessionInfo.market}"
    }

    val deviceIp: MutableLiveData<String> = MutableLiveData("")

    val selectedPage = MutableLiveData(0)

    val numberField: MutableLiveData<String> = MutableLiveData("")

    val requestFocusToNumberField = MutableLiveData(true)

    val tasks by lazy {
        taskManager.tasks
    }

    private val toUiFunc = { products: List<Task>? ->
        products?.mapIndexed { index, task ->
            ItemTaskListUi(
                    position = (products.size - index).toString(),
                    number = task.taskInfo.number,
                    text1 = task.taskInfo.text1,
                    text2 = task.taskInfo.text2,
                    taskStatus = task.status,
                    quantity = task.quantity.toString()
            )
        }
    }

    val processing by lazy {
        tasks.map { it?.filter { task -> !task.isProcessed } }.map(toUiFunc)
    }

    val processed by lazy {
        tasks.map { it?.filter { task -> task.isProcessed } }.map(toUiFunc)
    }

    val scanButtonVisibility by lazy {
        taskManager.taskType == TaskType.PROCESSING_UNIT
    }

    // -----------------------------

    init {
        viewModelScope.launch {
            loadTaskList()
        }
    }

    // -----------------------------

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    private fun loadTaskList() {
        viewModelScope.launch {
            navigator.showProgressLoadingData()

            taskListNetRequest(
                    TaskListParams(
                            tkNumber = sessionInfo.market ?: "",
                            taskType = taskManager.getTaskType(),
                            deviceIp = deviceIp.value ?: "Not found!"
                    )
            ).also {
                navigator.hideProgress()
            }.either(::handleFailure) { taskListResult ->
                taskManager.addTasks(taskListResult)
            }
        }
    }

    override fun handleFailure(failure: Failure) {
        super.handleFailure(failure)
        navigator.openAlertScreen(failure)
    }

    fun onClickMenu() {
        navigator.goBack()
    }

    fun onClickRefresh() {
        loadTaskList()
    }

    fun onClickItemPosition(position: Int) {
        when (selectedPage.value) {
            Tabs.PROCESSING.page -> processing
            Tabs.PROCESSED.page -> processed
            else -> throw IllegalArgumentException("$position: Wrong pager position!")
        }.let {
            openTaskByNumber(it.value!![position].number)
        }
    }

    override fun onOkInSoftKeyboard(): Boolean {
        openTaskByNumber(numberField.value ?: "")
        return true
    }

    private fun openTaskByNumber(taskNumber: String) {
        taskManager.tasks.value?.find { it.number == taskNumber }?.let { task ->
            taskManager.currentTask = task

            if (task.isProcessed) {
                navigator.openRawListScreen()
            } else {
                viewModelScope.launch {
                    navigator.showProgressLoadingData()
                    taskInfoNetRequest(
                            TaskInfoParams(
                                    marketNumber = sessionInfo.market ?: "Not found!",
                                    deviceIp = deviceIp.value ?: "Not found!",
                                    taskNumber = taskNumber,
                                    blockingType = taskManager.getBlockType()
                            )
                    ).also {
                        navigator.hideProgress()
                    }.either(::handleFailure) { taskInfoResult ->
                        viewModelScope.launch {
                            taskManager.addTaskInfoToCurrentTask(taskInfoResult)

                            task.taskInfo.apply {
                                when (blockType) {
                                    "2" -> navigator.showAlertBlockedTaskAnotherUser(lockUser)
                                    "1" -> navigator.showAlertBlockedTaskByMe(lockUser) { openTaskByType(task) }
                                    else -> openTaskByType(task)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun openTaskByType(task: Task) {
        if (task.taskInfo.isPack.isSapTrue()) {
            navigator.openPackGoodListScreen()
        } else {
            navigator.openProcessingUnitListScreen()
        }
    }

}

data class ItemTaskListUi(
        val position: String,
        val number: String,
        val text1: String,
        val text2: String,
        val taskStatus: TaskStatus,
        val quantity: String
)