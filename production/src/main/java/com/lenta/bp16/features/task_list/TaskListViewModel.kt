package com.lenta.bp16.features.task_list

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp16.model.ITaskManager
import com.lenta.bp16.model.Tabs
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

    val marketIp: MutableLiveData<String> = MutableLiveData("")

    val selectedPage = MutableLiveData(0)

    val numberField: MutableLiveData<String> = MutableLiveData("")

    val requestFocusToNumberField = MutableLiveData(true)

    val tasks = taskManager.tasks.map { it }

    private val toUiFunc = { products: List<Task>? ->
        products?.mapIndexed { index, task ->
            ItemTaskListUi(
                    position = (products.size - index).toString(),
                    puNumber = task.processingUnit.number,
                    taskType = task.type,
                    sku = task.processingUnit.quantity.toString()
            )
        }
    }

    val processing = tasks.map { it?.filter { task -> !task.isProcessed } }.map(toUiFunc)

    val processed = tasks.map { it?.filter { task -> task.isProcessed } }.map(toUiFunc)

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
                            tkNumber = sessionInfo.market ?: ""
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
        when (position) {
            Tabs.PROCESSING.page -> processing
            Tabs.PROCESSED.page -> processed
            else -> throw IllegalArgumentException("Wrong pager position!")
        }.let {
            openTaskByNumber(it.value!![position].puNumber)
        }
    }

    override fun onOkInSoftKeyboard(): Boolean {
        openTaskByNumber(numberField.value ?: "")
        return true
    }

    private fun openTaskByNumber(puNumber: String) {
        taskManager.tasks.value?.find { it.processingUnit.number == puNumber }?.let { task ->
            taskManager.currentTask = task

            if (task.isProcessed) {
                navigator.openRawListScreen()
            } else {
                viewModelScope.launch {
                    navigator.showProgressLoadingData()
                    taskInfoNetRequest(
                            TaskInfoParams(
                                    marketNumber = sessionInfo.market ?: "Not found!",
                                    marketIp = marketIp.value ?: "Not found!",
                                    puNumber = puNumber,
                                    processingMode = "1"
                            )
                    ).also {
                        navigator.hideProgress()
                    }.either(::handleFailure) { taskInfoResult ->
                        taskManager.addTaskInfoToCurrentTask(taskInfoResult)
                        navigator.openRawGoodListScreen()
                    }
                }
            }
        }
    }

}

data class ItemTaskListUi(
        val position: String,
        val puNumber: String,
        val taskType: TaskType,
        val sku: String
)