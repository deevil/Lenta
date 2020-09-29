package com.lenta.bp15.features.task_list

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.lenta.bp15.model.ITaskManager
import com.lenta.bp15.model.pojo.Task
import com.lenta.bp15.platform.navigation.IScreenNavigator
import com.lenta.bp15.platform.resource.IResourceManager
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.mapSkipNulls
import javax.inject.Inject

class TaskListViewModel : CoreViewModel(), PageSelectionListener, OnOkInSoftKeyboardListener {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var resource: IResourceManager

    @Inject
    lateinit var manager: ITaskManager


    /**
    Переменные
     */

    val title by lazy {
        resource.tk(sessionInfo.market.orEmpty())
    }

    val processingField by lazy {
        MutableLiveData(sessionInfo.userName)
    }

    val searchField = MutableLiveData("")

    val requestFocusToProcessingField = MutableLiveData(false)

    val requestFocusToSearchField = MutableLiveData(false)

    val taskList by lazy {
        manager.tasks.combineLatest(processingField).mapSkipNulls {
            val (tasks, _) = it
            tasks
        }.map(taskListFilterFunc)
    }

    val searchList by lazy {
        manager.foundTasks.combineLatest(searchField).mapSkipNulls {
            val (tasks, _) = it
            tasks
        }.map(taskListFilterFunc)
    }

    private val taskListFilterFunc = { tasks: List<Task> ->
        when {
            isEnteredLogin() -> tasks
            else -> tasks.filter { task -> task.number.contains(getCurrentFieldValue().orEmpty()) }
        }.let { list ->
            list.mapIndexed { index, task ->
                task.convertToItemTaskUi(index)
            }
        }
    }

    /**
    Блок инициализации
     */

    init {
        updateTaskList()
    }

    /**
    Методы
     */

    private fun updateTaskList() {
        launchUITryCatch {
            manager.updateTaskList()
        }
    }

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    override fun onOkInSoftKeyboard(): Boolean {
        return false
    }

    private fun isEnteredLogin(): Boolean {
        return getCurrentFieldValue()?.let { numberOrLogin ->
            numberOrLogin.isNotEmpty() && !numberOrLogin.all { it.isDigit() }
        } ?: false
    }

    private fun getCurrentFieldValue(): String? {
        return when (selectedPage.value) {
            0 -> processingField.value
            1 -> searchField.value
            else -> throw IllegalArgumentException("Wrong pager position!")
        }
    }

    /**
    Обработка нажатий
     */

    fun onClickItemTaskPosition(position: Int) {

    }

    fun onClickItemSearchPosition(position: Int) {

    }

    fun onClickUpdate() {
        updateTaskList()
    }

    fun onClickFilter() {
        navigator.openSearchTaskScreen()
    }

}