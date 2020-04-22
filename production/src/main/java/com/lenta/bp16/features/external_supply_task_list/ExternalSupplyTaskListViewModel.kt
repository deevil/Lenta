package com.lenta.bp16.features.external_supply_task_list

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp16.model.ITaskManager
import com.lenta.bp16.model.Tabs
import com.lenta.bp16.model.TaskStatus
import com.lenta.bp16.model.TaskType
import com.lenta.bp16.model.pojo.Task
import com.lenta.bp16.platform.navigation.IScreenNavigator
import com.lenta.bp16.platform.resource.IResourceManager
import com.lenta.bp16.request.TaskInfoNetRequest
import com.lenta.bp16.request.TaskInfoParams
import com.lenta.bp16.request.TaskListNetRequest
import com.lenta.bp16.request.TaskListParams
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.dropZeros
import com.lenta.shared.utilities.extentions.isSapTrue
import com.lenta.shared.utilities.extentions.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class ExternalSupplyTaskListViewModel : CoreViewModel(), PageSelectionListener, OnOkInSoftKeyboardListener {

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
    @Inject
    lateinit var resourceManager: IResourceManager


    private val tasks by lazy {
        taskManager.tasks.map {
            it?.filter { task -> task.type == TaskType.EXTERNAL_SUPPLY }
        }
    }

    val title by lazy {
        "ТК - ${sessionInfo.market}"
    }

    val description by lazy {
        tasks.map {
            resourceManager.workWith(taskManager.taskType.abbreviation, it?.size ?: 0)
        }
    }

    val deviceIp = MutableLiveData("")

    val selectedPage = MutableLiveData(0)

    val numberField = MutableLiveData("")

    val requestFocusToNumberField = MutableLiveData(true)

    private val toUiFunc = { products: List<Task>? ->
        products?.mapIndexed { index, task ->
            ItemExternalSupplyTaskUi(
                    position = (products.size - index).toString(),
                    number = task.taskInfo.number,
                    text1 = task.taskInfo.text1,
                    text2 = task.taskInfo.text2,
                    taskStatus = task.status,
                    quantity = task.quantity.dropZeros(),
                    isPack = task.isPack
            )
        }
    }

    val processing by lazy {
        tasks.map { it?.filter { task -> !task.isProcessed } }.map(toUiFunc)
    }

    val processed by lazy {
        tasks.map { it?.filter { task -> task.isProcessed } }.map(toUiFunc)
    }

    // -----------------------------

    init {
        viewModelScope.launch {
            //loadTaskList()
        }
    }

    // -----------------------------

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    fun loadTaskList() {
        viewModelScope.launch {
            navigator.showProgressLoadingData()

            taskListNetRequest(
                    TaskListParams(
                            tkNumber = sessionInfo.market ?: "",
                            taskType = taskManager.getTaskTypeCode(),
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
        openTaskByNumber(formatNumberForSearch(numberField.value ?: ""))
        return true
    }

    private fun formatNumberForSearch(number: String): String {
        var formattedNumber = number
        while (formattedNumber.length < taskManager.taskType.numberLength) {
            formattedNumber = "0$formattedNumber"
        }

        return formattedNumber
    }

    private fun openTaskByNumber(taskNumber: String) {
        taskManager.tasks.value?.find { it.number == taskNumber }?.let { task ->
            taskManager.currentTask.value = task
            numberField.value = ""

            task.taskInfo.apply {
                when (blockType) {
                    "2" -> navigator.showAlertBlockedTaskAnotherUser(lockUser, lockIp)
                    "1" -> navigator.showAlertBlockedTaskByMe(lockUser) { openTask(task) }
                    else -> openTask(task)
                }
            }
        }
    }

    private fun openTask(task: Task) {
        if (task.isProcessed) {
            openTaskByType(task)
        } else {
            viewModelScope.launch {
                navigator.showProgressLoadingData()
                taskInfoNetRequest(
                        TaskInfoParams(
                                marketNumber = sessionInfo.market ?: "Not found!",
                                deviceIp = deviceIp.value ?: "Not found!",
                                taskNumber = task.number,
                                blockingType = taskManager.getBlockType()
                        )
                ).also {
                    navigator.hideProgress()
                }.either(::handleFailure) { taskInfoResult ->
                    viewModelScope.launch {
                        taskManager.addTaskInfoToCurrentTask(taskInfoResult)
                        openTaskByType(task)
                    }
                }
            }
        }
    }

    private fun openTaskByType(task: Task) {
        if (task.taskInfo.isPack.isSapTrue()) {
            navigator.openPackGoodListScreen()
        } else {
            navigator.openExternalSupplyListScreen()
        }
    }

    fun onClickLabel() {
        navigator.openReprintLabelScreen()
    }

}

data class ItemExternalSupplyTaskUi(
        val position: String,
        val number: String,
        val text1: String,
        val text2: String,
        val taskStatus: TaskStatus,
        val quantity: String,
        val isPack: Boolean
)