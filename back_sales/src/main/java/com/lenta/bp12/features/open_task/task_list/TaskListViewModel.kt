package com.lenta.bp12.features.open_task.task_list

import androidx.lifecycle.MutableLiveData
import com.lenta.bp12.managers.interfaces.IOpenTaskManager
import com.lenta.bp12.model.BlockType
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
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.orIfNull
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

    val processingNumberField by lazy {
        MutableLiveData(sessionInfo.userName)
    }

    val searchNumberField = MutableLiveData("")

    val requestFocusToProcessingNumberField by lazy {
        MutableLiveData(true)
    }

    val requestFocusToSearchNumberField by lazy {
        MutableLiveData(true)
    }

    private val tasks by lazy {
        manager.tasks
    }

    private val foundTasks by lazy {
        manager.foundTasks
    }

    val processing by lazy {
        tasks.combineLatest(processingNumberField).map {
            it?.let {
                val (tasks, number) = it

                if (isEnteredLogin(number.orEmpty())) {
                    tasks
                } else {
                    tasks?.filter { task -> task.number.contains(number.orEmpty()) }
                }?.let { taskList ->
                    val taskListSize = taskList.size
                    taskList.mapIndexed { index, task ->
                        ItemTaskListUi(
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

    val searchItems by lazy {
        foundTasks.combineLatest(searchNumberField).map {
            it?.let {
                val (tasks, number) = it

                if (isEnteredLogin(number)) {
                    tasks
                } else {
                    tasks?.filter { task -> task.number.contains(number.orEmpty()) }
                }?.let { taskList ->
                    val taskListSize = taskList.size
                    taskList.mapIndexed { index, task ->
                        ItemTaskListUi(
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
            updateProcessingTaskList()
        }
    }

    /**
    Методы
     */

    override fun onPageSelected(position: Int) {
        selectedPage.value = position

        when (position) {
            0 -> requestFocusToProcessingNumberField.value = true
            1 -> requestFocusToSearchNumberField.value = true
        }
    }

    private fun handleTaskListResult(result: TaskListResult) {
        launchUITryCatch {
            result.tasks?.let { manager.addTasks(it) }
        }
    }


    private fun loadSearchTaskList(value: String = "") {
        manager.searchParams?.let { params ->
            launchUITryCatch {
                navigator.showProgressLoadingData(::handleFailure)

                taskListNetRequest(
                        TaskListParams(
                                tkNumber = sessionInfo.market.orEmpty(),
                                value = value,
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
            manager.addFoundTasks(result.tasks.orEmpty())
        }
    }

    fun onClickItemPosition(position: Int) {
        selectedPage.value?.let { page ->
            when (page) {
                0 -> handleClickProcessingItems(position)
                1 -> handleClickSearchItems(position)
                else -> throw IllegalArgumentException("Wrong pager position!")
            }
        }
    }

    private fun handleClickProcessingItems(position: Int) {
        tasks.value?.let { tasks ->
            val numberOfClickedProcessing = processing.value?.getOrNull(position)?.number.orEmpty()
            tasks.find { it.number == numberOfClickedProcessing }?.let(::prepareToOpenTask)
                    .orIfNull { handleTaskNotFoundError() }
        }.orIfNull { handleTaskNotFoundError() }
    }

    private fun handleClickSearchItems(position: Int) {
        foundTasks.value?.let { tasks ->
            val numberOfClickedSearched = searchItems.value?.getOrNull(position)?.number.orEmpty()
            tasks.find { it.number == numberOfClickedSearched }?.let(::prepareToOpenTask)
                    .orIfNull { handleTaskNotFoundError() }
        }.orIfNull { handleTaskNotFoundError() }
    }

    private fun handleTaskNotFoundError() {
        Logg.e { "foundTasks null " }
        navigator.showInternalError(resource.taskNotFoundErrorMsg)
    }

    private fun prepareToOpenTask(task: TaskOpen) {
        with(task) {
            when (block.type) {
                BlockType.LOCK -> navigator.showAlertBlockedTaskAnotherUser(block.user, block.ip)
                BlockType.SELF_LOCK -> navigator.showAlertBlockedTaskByMe() { openTask(task) }
                else -> openTask(task)
            }
        }
    }

    private fun openTask(task: TaskOpen) {
        manager.updateCurrentTask(task)
        navigator.openTaskCardOpenScreen()
    }

    fun updateTaskList() {
        if (manager.isNeedLoadTaskListByParams) {
            manager.isNeedLoadTaskListByParams = false
            loadSearchTaskList()
        }
    }

    override fun onOkInSoftKeyboard(): Boolean {
        selectedPage.value?.let { page ->
            when (page) {
                0 -> updateProcessingTaskList()
                1 -> updateSearchTaskList()
                else -> throw IllegalArgumentException("Wrong pager position!")
            }
        }
        return true
    }

    private fun updateProcessingTaskList() {
        val entered = processingNumberField.value.orEmpty()
        loadProcessingTaskList(entered)
    }

    private fun updateSearchTaskList() {
        val entered = searchNumberField.value.orEmpty()
        if (isEnteredLogin(entered) || isEnteredUnknownTaskNumber(entered)) {
            loadSearchTaskList(entered)
        } else {
            loadSearchTaskList()
        }
    }

    private fun isEnteredLogin(number: String): Boolean {
        return number.isNotEmpty() && !number.all { it.isDigit() }
    }

    private fun isEnteredUnknownTaskNumber(entered: String): Boolean {
        val currentList = when (selectedPage.value) {
            0 -> processing.value
            1 -> searchItems.value
            else -> throw IllegalArgumentException("Wrong pager position!")
        }
        val isEnteredNotEmpty = entered.isNotEmpty()
        val isEnteredOnlyNumbers = entered.all { it.isDigit() }
        val isCurrentListEmpty = currentList.isNullOrEmpty()

        return isEnteredNotEmpty && isEnteredOnlyNumbers && isCurrentListEmpty
    }

    private fun loadProcessingTaskList(value: String, userNumber: String = "") {
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
            }.either(
                    fnL = {
                        if (it is Failure.SapError) {
                            navigator.showAlertDialogWithRedTriangle(it.message)
                        } else {
                            handleFailure(it)
                        }
                    },
                    fnR = ::handleTaskListResult
            )
        }
    }

    /**
    Обработка нажатий кнопок
     */

    fun onClickMenu() {
        navigator.goBack()
    }

    fun onClickUpdate() {
        updateProcessingTaskList()
    }

    fun onClickFilter() {
        navigator.openTaskSearchScreen()
    }

}