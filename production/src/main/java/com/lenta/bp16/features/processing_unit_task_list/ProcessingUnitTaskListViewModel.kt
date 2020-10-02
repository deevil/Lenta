package com.lenta.bp16.features.processing_unit_task_list

import androidx.lifecycle.MutableLiveData
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
import com.lenta.shared.utilities.extentions.*
import javax.inject.Inject

class ProcessingUnitTaskListViewModel : CoreViewModel(), PageSelectionListener, OnOkInSoftKeyboardListener {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var manager: ITaskManager

    @Inject
    lateinit var taskListNetRequest: TaskListNetRequest

    @Inject
    lateinit var taskInfoNetRequest: TaskInfoNetRequest

    @Inject
    lateinit var resource: IResourceManager


    private val tasks by lazy {
        manager.tasks.map {
            it?.filter { task -> task.type == TaskType.PROCESSING_UNIT }
        }
    }

    val title by lazy {
        "ТК - ${sessionInfo.market}"
    }

    val description by lazy {
        tasks.map {
            resource.workWith(manager.taskType.abbreviation, it?.size ?: 0)
        }
    }

    val deviceIp = MutableLiveData("")

    val numberField = MutableLiveData("")

    val requestFocusToNumberField = MutableLiveData(true)

    private val toUiFunc = { products: List<Task>? ->
        products?.filter {
            it.number.contains(numberField.value.orEmpty())
        }?.mapIndexed { index, task ->
            ItemProcessingUnitTaskUi(
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
        tasks.combineLatest(numberField).map {
            it?.let {
                val (list, number) = it
                list.filter { task -> !task.isProcessed && task.number.contains(number) }
            }
        }.map(toUiFunc)
    }

    val processed by lazy {
        tasks.combineLatest(numberField).map {
            it?.let {
                val (list, number) = it
                list.filter { task -> task.isProcessed && task.number.contains(number) }
            }
        }.map(toUiFunc)
    }

    // -----------------------------

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    fun loadTaskList() {
        launchUITryCatch {
            navigator.showProgressLoadingData(::handleFailure)

            taskListNetRequest(
                    TaskListParams(
                            tkNumber = sessionInfo.market.orEmpty(),
                            taskType = manager.getTaskTypeCode(),
                            deviceIp = deviceIp.value.orEmpty()
                    )
            ).also {
                navigator.hideProgress()
            }.either(::handleFailure) { taskListResult ->
                manager.addTasks(taskListResult)
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
        openTaskByNumber(formatNumberForSearch(numberField.value.orEmpty()))
        return true
    }

    fun onScanResult(data: String) {
        openTaskByNumber(formatNumberForSearch(data))
    }

    private fun formatNumberForSearch(number: String): String {
        var formattedNumber = number
        while (formattedNumber.length < manager.taskType.numberLength) {
            formattedNumber = "0$formattedNumber"
        }

        return formattedNumber
    }

    private fun openTaskByNumber(taskNumber: String) {
        manager.tasks.value?.find { it.number == taskNumber }?.let { task ->
            manager.updateCurrentTask(task)
            numberField.value = ""

            task.taskInfo.apply {
                when (blockType) {
                    "2" -> navigator.showAlertBlockedTaskAnotherUser(lockUser, lockIp)
                    "1" -> navigator.showAlertBlockedTaskByMe { openTask(task) }
                    else -> openTask(task)
                }
            }
        }
    }

    private fun openTask(task: Task) {
        if (task.isProcessed) {
            openTaskByType(task)
        } else {
            launchUITryCatch {
                navigator.showProgressLoadingData(::handleFailure)
                taskInfoNetRequest(
                        TaskInfoParams(
                                marketNumber = sessionInfo.market.orEmpty(),
                                deviceIp = deviceIp.value.orEmpty(),
                                taskNumber = task.number,
                                blockingType = manager.getBlockType()
                        )
                ).also {
                    navigator.hideProgress()
                }.either(::handleFailure) { taskInfoResult ->
                    launchUITryCatch {
                        manager.addTaskInfoToCurrentTask(taskInfoResult)
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
            navigator.openProcessingUnitListScreen()
        }
    }

    fun onClickLabel() {
        navigator.openReprintLabelScreen()
    }

}

data class ItemProcessingUnitTaskUi(
        val position: String,
        val number: String,
        val text1: String,
        val text2: String,
        val taskStatus: TaskStatus,
        val quantity: String,
        val isPack: Boolean
)