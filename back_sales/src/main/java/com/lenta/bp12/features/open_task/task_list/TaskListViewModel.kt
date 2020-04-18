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

    val tasks by lazy {
        manager.tasks
    }

    val processing by lazy {
        tasks.map { list ->
            list?.mapIndexed { index, task ->
                ItemTaskUi(
                        position = "${index + 1}",
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

    val search by lazy {
        MutableLiveData(List(3) {
            ItemTaskUi(
                    position = "${it + 1}",
                    number = "1",
                    name = "Test name ${it + 1}",
                    provider = "Test supplier ${it + 1}",
                    taskStatus = TaskStatus.COMMON,
                    blockType = BlockType.UNLOCK,
                    quantity = (1..15).random().toString()
            )
        })
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

    override fun handleFailure(failure: Failure) {
        super.handleFailure(failure)
        navigator.openAlertScreen(failure)
    }

    fun onClickMenu() {
        navigator.goBack()
    }

    fun onClickUpdate() {
        loadTaskList()
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
                        tasks.find { it.number == search.value!![position].number }?.let { task ->
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