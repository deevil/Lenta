package com.lenta.bp12.features.open_task.task_list

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp12.model.BlockType
import com.lenta.bp12.model.IOpenTaskManager
import com.lenta.bp12.model.TaskStatus
import com.lenta.bp12.model.pojo.open_task.Task
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


    val title by lazy {
        "TK - ${sessionInfo.market}"
    }

    val selectedPage = MutableLiveData(0)

    val numberField: MutableLiveData<String> = MutableLiveData("")

    val requestFocusToNumberField by lazy {
        MutableLiveData(true)
    }

    private val tasks by lazy {
        manager.tasks
    }

    private val foundTasks by lazy {
        manager.foundTasks
    }

    val processing by lazy {
        tasks.combineLatest(numberField).map { pair ->
            val list = pair?.first
            val number = pair?.second

            if (number.isNullOrEmpty()) {
                list
            } else {
                if (number.all { it.isDigit() }) {
                    list?.filter { it.number.contains(number) }
                } else {
                    list?.filter { it.block.user.contains(number) }
                }
            }?.let {taskList ->
                taskList.mapIndexed { index, task ->
                    ItemTaskUi(
                            position = "${taskList.size - index}",
                            number = task.number,
                            name = task.name,
                            provider = task.getProviderCodeWithName(),
                            taskStatus = task.status,
                            blockType = task.block.type,
                            quantity = task.quantity.toString()
                    )
                }
            }
        }
    }

    val found by lazy {
        foundTasks.combineLatest(numberField).map { pair ->
            val list = pair?.first
            val number = pair?.second

            if (number.isNullOrEmpty()) {
                list
            } else {
                if (number.all { it.isDigit() }) {
                    list?.filter { it.number.contains(number) }
                } else {
                    list?.filter { it.block.user.contains(number) }
                }
            }?.let {taskList ->
                taskList.mapIndexed { index, task ->
                    ItemTaskUi(
                            position = "${taskList.size - index}",
                            number = task.number,
                            name = task.name,
                            provider = task.getProviderCodeWithName(),
                            taskStatus = task.status,
                            blockType = task.block.type,
                            quantity = task.quantity.toString()
                    )
                }
            }
        }
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

    private fun loadTaskList() {
        viewModelScope.launch {
            navigator.showProgressLoadingData()

            taskListNetRequest(
                    TaskListParams(
                            tkNumber = sessionInfo.market ?: "",
                            user = sessionInfo.userName!!,
                            userNumber = appSettings.lastPersonnelNumber ?: "",
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

    private fun loadFoundTaskList() {
        manager.searchParams.value?.let { params ->
            viewModelScope.launch {
                navigator.showProgressLoadingData()

                taskListNetRequest(
                        TaskListParams(
                                tkNumber = sessionInfo.market ?: "",
                                user = sessionInfo.userName!!,
                                userNumber = appSettings.lastPersonnelNumber ?: "",
                                mode = 2,
                                taskSearchParams = params
                        )
                ).also {
                    navigator.hideProgress()
                }.either(::handleLoadFoundTaskListFailure) { taskListResult ->
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

    private fun handleLoadFoundTaskListFailure(failure: Failure) {
        manager.searchParams.value = null
        navigator.openAlertScreen(failure)
    }

    fun onClickMenu() {
        navigator.goBack()
    }

    fun onClickUpdate() {
        loadTaskList()
        loadFoundTaskList()
    }

    fun onClickFilter() {
        navigator.openTaskSearchScreen()
    }

    fun onClickItemPosition(position: Int) {
        selectedPage.value?.let { page ->
            when (page) {
                0 -> {
                    tasks.value?.let { tasks ->
                        tasks.find { it.number == processing.value!![position].number }?.let { task ->
                            task.apply {
                                when (block.type) {
                                    BlockType.LOCK -> navigator.showAlertBlockedTaskAnotherUser(block.user, block.ip)
                                    BlockType.SELF_LOCK -> navigator.showAlertBlockedTaskByMe(block.user) { openTask(task) }
                                    else -> openTask(task)
                                }
                            }
                        }
                    }
                }
                1 -> {
                    tasks.value?.let { tasks ->
                        tasks.find { it.number == found.value!![position].number }?.let { task ->
                            task.apply {
                                when (block.type) {
                                    BlockType.LOCK -> navigator.showAlertBlockedTaskAnotherUser(block.user, block.ip)
                                    BlockType.SELF_LOCK -> navigator.showAlertBlockedTaskByMe(block.user) { openTask(task) }
                                    else -> openTask(task)
                                }
                            }
                        }
                    }
                }
                else -> throw IllegalArgumentException("Wrong pager position!")
            }
        }
    }

    private fun openTask(task: Task) {
        manager.updateCurrentTask(task)
        navigator.openTaskCardOpenScreen()
    }

    override fun onOkInSoftKeyboard(): Boolean {
        // todo Что-то сделать при вводе номера/пользователя?
        // ...

        return true
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