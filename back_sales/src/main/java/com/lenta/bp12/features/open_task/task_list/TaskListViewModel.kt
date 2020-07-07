package com.lenta.bp12.features.open_task.task_list

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp12.model.BlockType
import com.lenta.bp12.model.IOpenTaskManager
import com.lenta.bp12.model.TaskStatus
import com.lenta.bp12.model.pojo.open_task.TaskOpen
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.bp12.request.TaskListNetRequest
import com.lenta.bp12.request.TaskListParams
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.settings.IAppSettings
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class TaskListViewModel : CoreViewModel(), PageSelectionListener, OnOkInSoftKeyboardListener {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var taskListNetRequest: TaskListNetRequest

    @Inject
    lateinit var appSettings: IAppSettings

    @Inject
    lateinit var manager: IOpenTaskManager


    /**
    Переменные
     */

    val title by lazy {
        "TK - ${sessionInfo.market}"
    }

    val selectedPage = MutableLiveData(0)

    val numberField by lazy {
        MutableLiveData(sessionInfo.userName)
    }

    val requestFocusToNumberField by lazy {
        MutableLiveData(true)
    }

    private val tasks by lazy {
        manager.tasks
    }

    private val foundTasks by lazy {
        manager.foundTasks
    }

    val taskNumber = MutableLiveData("")

    val processing by lazy {
        tasks.combineLatest(taskNumber).map {
            it?.let {
                val tasks = it.first
                val number = it.second

                if (number.isNullOrEmpty()) {
                    tasks
                } else {
                    tasks?.filter { task -> task.number.contains(number) }
                }?.let { taskList ->
                    taskList.mapIndexed { index, task ->
                        ItemTaskUi(
                                position = "${taskList.size - index}",
                                number = task.number,
                                name = task.getFormattedName(),
                                provider = task.getProviderCodeWithName(),
                                taskStatus = task.status,
                                blockType = task.block.type,
                                quantity = task.numberOfGoods.toString()
                        )
                    }
                }
            }
        }
    }

    val found by lazy {
        foundTasks.combineLatest(taskNumber).map {
            it?.let {
                val tasks = it.first
                val number = it.second

                if (number.isNullOrEmpty()) {
                    tasks
                } else {
                    tasks?.filter { task -> task.number.contains(number) }
                }?.let { taskList ->
                    taskList.mapIndexed { index, task ->
                        ItemTaskUi(
                                position = "${taskList.size - index}",
                                number = task.number,
                                name = task.name,
                                provider = task.getProviderCodeWithName(),
                                taskStatus = task.status,
                                blockType = task.block.type,
                                quantity = task.numberOfGoods.toString()
                        )
                    }
                }
            }
        }
    }

    /**
    Блок инициализации
     */

    init {
        viewModelScope.launch {
            onClickUpdate()
        }
    }

    /**
    Методы
     */

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    private fun loadTaskList(user: String, userNumber: String) {
        viewModelScope.launch {
            navigator.showProgressLoadingData()

            taskListNetRequest(
                    TaskListParams(
                            tkNumber = sessionInfo.market ?: "",
                            user = user,
                            userNumber = userNumber,
                            mode = 1
                    )
            ).also {
                navigator.hideProgress()
            }.either(::handleFailure) { taskListResult ->
                viewModelScope.launch {
                    manager.addTasks(taskListResult.tasks)
                }
            }
        }
    }

    private fun loadTaskListWithParams(user: String, userNumber: String) {
        manager.searchParams.value?.let { params ->
            viewModelScope.launch {
                navigator.showProgressLoadingData()

                taskListNetRequest(
                        TaskListParams(
                                tkNumber = sessionInfo.market ?: "",
                                user = user,
                                userNumber = userNumber,
                                mode = 2,
                                taskSearchParams = params
                        )
                ).also {
                    navigator.hideProgress()
                }.either(::handleFailure) { taskListResult ->
                    viewModelScope.launch {
                        manager.addFoundTasks(taskListResult.tasks)
                    }
                }
            }
        }
    }

    override fun handleFailure(failure: Failure) {
        super.handleFailure(failure)
        navigator.openAlertScreen(failure)
    }

    fun onClickItemPosition(position: Int) {
        selectedPage.value?.let { page ->
            when (page) {
                0 -> {
                    tasks.value?.let { tasks ->
                        tasks.find { it.number == processing.value!![position].number }?.let { task ->
                            prepareToOpenTask(task)
                        }
                    }
                }
                1 -> {
                    tasks.value?.let { tasks ->
                        tasks.find { it.number == found.value!![position].number }?.let { task ->
                            prepareToOpenTask(task)
                        }
                    }
                }
                else -> throw IllegalArgumentException("Wrong pager position!")
            }
        }
    }

    private fun prepareToOpenTask(task: TaskOpen) {
        task.apply {
            when (block.type) {
                BlockType.LOCK -> navigator.showAlertBlockedTaskAnotherUser(block.user, block.ip)
                BlockType.SELF_LOCK -> navigator.showAlertBlockedTaskByMe(block.user) { openTask(task) }
                else -> openTask(task)
            }
        }
    }

    private fun openTask(task: TaskOpen) {
        manager.updateCurrentTask(task)
        navigator.openTaskCardOpenScreen()
    }

    override fun onOkInSoftKeyboard(): Boolean {
        if (isEnteredLogin()) {
            taskNumber.value = ""
            onClickUpdate()
        } else {
            taskNumber.value = numberField.value
        }

        return true
    }

    private fun isEnteredLogin(): Boolean {
        val entered = numberField.value ?: ""
        return entered.isNotEmpty() && !entered.all { it.isDigit() }
    }

    /**
    Обработка нажатий кнопок
     */

    fun onClickMenu() {
        navigator.goBack()
    }

    fun onClickUpdate() {
        val user = if (isEnteredLogin()) numberField.value ?: "" else sessionInfo.userName ?: ""
        val userNumber = if (isEnteredLogin()) "" else sessionInfo.personnelNumber ?: ""

        loadTaskList(user, userNumber)
        loadTaskListWithParams(user, userNumber)
    }

    fun onClickFilter() {
        navigator.openTaskSearchScreen()
    }

}

data class ItemTaskUi(
        val position: String,
        val number: String,
        val name: String,
        val provider: String,
        val taskStatus: TaskStatus,
        val blockType: BlockType,
        val quantity: String
)