package com.lenta.bp12.features.open_task.task_list

import androidx.lifecycle.MutableLiveData
import com.lenta.bp12.model.BlockType
import com.lenta.bp12.model.IOpenTaskManager
import com.lenta.bp12.model.TaskSearchMode
import com.lenta.bp12.model.pojo.open_task.TaskOpen
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.bp12.platform.resource.IResourceManager
import com.lenta.bp12.request.TaskListNetRequest
import com.lenta.bp12.request.TaskListParams
import com.lenta.bp12.request.TaskListResult
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.settings.IAppSettings
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map
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

    @Inject
    lateinit var resource: IResourceManager


    /**
    Переменные
     */

    val title by lazy {
        resource.tk(sessionInfo.market.orEmpty())
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

    val processing by lazy {
        tasks.combineLatest(numberField).map {
            it?.let {
                val (tasks, number) = it

                if (isEnteredLogin()) {
                    tasks
                } else {
                    tasks?.filter { task -> task.number.contains(number.orEmpty()) }
                }?.let { taskList ->
                    val taskListSize = taskList.size
                    taskList.mapIndexed { index, task ->
                        ItemTaskUi(
                                position = "${taskListSize - index}",
                                number = task.number,
                                name = task.getFormattedName(),
                                provider = task.getProviderCodeWithName(),
                                isFinished = task.isFinished,
                                blockType = task.block.type,
                                quantity = task.numberOfGoods.toString()
                        )
                    }
                }
            }
        }
    }

    val found by lazy {
        foundTasks.combineLatest(numberField).map {
            it?.let {
                val (tasks, number) = it

                if (isEnteredLogin()) {
                    tasks
                } else {
                    tasks?.filter { task -> task.number.contains(number.orEmpty()) }
                }?.let { taskList ->
                    val taskListSize = taskList.size
                    taskList.mapIndexed { index, task ->
                        ItemTaskUi(
                                position = "${taskListSize - index}",
                                number = task.number,
                                name = task.getFormattedName(),
                                provider = task.getProviderCodeWithName(),
                                isFinished = task.isFinished,
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
        launchUITryCatch {
            onClickUpdate()
        }
    }

    /**
    Методы
     */

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    private fun loadTaskList(value: String, userNumber: String = "") {
        launchUITryCatch {
            navigator.showProgressLoadingData(::handleFailure)

            taskListNetRequest(
                    TaskListParams(
                            tkNumber = sessionInfo.market.orEmpty(),
                            value = value,
                            userNumber = userNumber,
                            mode = TaskSearchMode.COMMON.mode
                    )
            ).also {
                navigator.hideProgress()
            }.either(::handleFailure, ::handleTaskListResult)
        }
    }

    private fun handleTaskListResult(result: TaskListResult) {
        launchUITryCatch {
            manager.addTasks(result.tasks)
        }
    }

    private fun loadTaskListWithParams(value: String, userNumber: String = "") {
        manager.searchParams?.let { params ->
            launchUITryCatch {
                navigator.showProgressLoadingData(::handleFailure)

                taskListNetRequest(
                        TaskListParams(
                                tkNumber = sessionInfo.market.orEmpty(),
                                value = value,
                                userNumber = userNumber,
                                mode = TaskSearchMode.WITH_PARAMS.mode,
                                taskSearchParams = params
                        )
                ).also {
                    navigator.hideProgress()
                }.either(::handleFailure, ::handleTaskListResultWithParams)
            }
        }
    }

    private fun handleTaskListResultWithParams(result: TaskListResult) {
        launchUITryCatch {
            manager.addFoundTasks(result.tasks)
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
        with(task) {
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
        onClickUpdate()
        return true
    }

    private fun isEnteredLogin(): Boolean {
        val entered = numberField.value.orEmpty()
        return entered.isNotEmpty() && !entered.all { it.isDigit() }
    }

    private fun isEnteredUnknownTaskNumber(): Boolean {
        val entered = numberField.value.orEmpty()
        val list = processing.value ?: emptyList()
        return entered.isNotEmpty() && entered.all { it.isDigit() } && list.isEmpty()
    }

    fun updateTaskList() {
        if (manager.isNeedUpdateTaskList) {
            manager.isNeedUpdateTaskList = false
            onClickUpdate()
        }
    }

    /**
    Обработка нажатий кнопок
     */

    fun onClickMenu() {
        navigator.goBack()
    }

    fun onClickUpdate() {
        val entered = numberField.value.orEmpty()

        if (isEnteredLogin() || isEnteredUnknownTaskNumber()) {
            loadTaskList(entered)
            loadTaskListWithParams(entered)
        } else {
            if (entered.isEmpty()) {
                val currentUser = sessionInfo.userName.orEmpty()
                val userNumber = sessionInfo.personnelNumber.orEmpty()

                loadTaskList(currentUser, userNumber)
                loadTaskListWithParams(currentUser, userNumber)

                numberField.value = sessionInfo.userName.orEmpty()
            }
        }
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
        val isFinished: Boolean,
        val blockType: BlockType,
        val quantity: String
)