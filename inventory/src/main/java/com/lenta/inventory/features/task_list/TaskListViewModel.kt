package com.lenta.inventory.features.task_list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.inventory.models.RecountType
import com.lenta.inventory.platform.navigation.IScreenNavigator
import com.lenta.inventory.repos.IRepoInMemoryHolder
import com.lenta.inventory.requests.network.TaskListNetRequest
import com.lenta.inventory.requests.network.TasksItem
import com.lenta.inventory.requests.network.TasksListParams
import com.lenta.inventory.requests.network.TasksListRestInfo
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.extentions.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class TaskListViewModel : CoreViewModel(), OnOkInSoftKeyboardListener {
    @Inject
    lateinit var sessionInfo: ISessionInfo
    @Inject
    lateinit var repoInMemoryHolder: IRepoInMemoryHolder
    @Inject
    lateinit var taskListNetRequest: TaskListNetRequest
    @Inject
    lateinit var screenNavigator: IScreenNavigator

    private var isNeedUpdateOnResume = true

    val filter = MutableLiveData("")

    val tkNumber: String by lazy {
        sessionInfo.market ?: ""
    }
    val tasks: LiveData<List<TaskItemVm>> by lazy {
        repoInMemoryHolder.tasksListRestInfo.map {
            it!!.tasks
                    .mapIndexed { index, task ->
                        TaskItemVm(
                                taskNumber = task.taskNumber,
                                number = "${repoInMemoryHolder.tasksListRestInfo.value!!.tasks.size
                                        - index}",
                                title = "${task.taskType}-${task.taskNumber}-${task.taskName}",
                                stock = task.stock,
                                typeConversion = if (task.isRecount.isBlank()) "1" else "2",
                                statusTask = StatusTask.from(taskItem = task, userName = sessionInfo.userName!!),
                                count = task.countProductsInTask
                        )
                    }
        }
    }
    val tasksCount by lazy {
        tasks.map { tasks.value?.size ?: 0 }
    }


    init {
        viewModelScope.launch {
            filter.value = sessionInfo.userName
        }
    }


    fun onClickUpdate() {
        viewModelScope.launch {
            screenNavigator.showProgress(taskListNetRequest)
            taskListNetRequest(
                    TasksListParams(
                            werks = sessionInfo.market ?: "",
                            user = if (filter.value.isNullOrBlank()) sessionInfo.userName!! else filter.value!!
                    )
            )
                    .either(::handleFailure, ::handleUpdateSuccess)
            screenNavigator.hideProgress()
        }

    }

    fun onClickMenu() {
        screenNavigator.openMainMenuScreen()
    }

    override fun handleFailure(failure: Failure) {
        super.handleFailure(failure)
        screenNavigator.openAlertScreen(failure)
    }

    override fun onOkInSoftKeyboard(): Boolean {
        onClickUpdate()
        return true
    }

    fun onClickItemPosition(position: Int) {
        tasks.value?.getOrNull(position)?.let { taskItem ->

            when (taskItem.statusTask) {
                StatusTask.BlockedMe -> openConfirmationScreen(taskItem.taskNumber).apply {
                    isNeedUpdateOnResume = true
                }
                StatusTask.BlockedNotMe -> return
                else -> screenNavigator.openJobCard(taskItem.taskNumber).apply {
                    isNeedUpdateOnResume = true
                }
            }
        }

    }

    private fun openConfirmationScreen(taskNumber: String) {
        repoInMemoryHolder.tasksListRestInfo.value!!.tasks
                .firstOrNull { it.taskNumber == taskNumber }?.let {
                    screenNavigator.openConfirmationTaskOpenScreen(it.lockUser, it.lockIP) {
                        screenNavigator.openJobCard(taskNumber)
                    }
                }
    }


    private fun handleUpdateSuccess(tasksListRestInfo: TasksListRestInfo) {
        if (tasksListRestInfo.retcode != "0") {
            screenNavigator.openInfoScreen(tasksListRestInfo.error)
        }
        repoInMemoryHolder.tasksListRestInfo.value = tasksListRestInfo

    }

    fun onResume() {
        if (isNeedUpdateOnResume) {
            onClickUpdate()
        }
        isNeedUpdateOnResume = false
    }

}


data class TaskItemVm(
        val taskNumber: String,
        val number: String,
        val title: String,
        val stock: String,
        val typeConversion: String,
        val statusTask: StatusTask,
        val count: String

)


enum class StatusTask {
    Free,
    BlockedMe,
    BlockedNotMe,
    Processed,
    Parallels;

    companion object {
        fun from(taskItem: TasksItem, userName: String): StatusTask {
            return when {
                taskItem.mode == RecountType.ParallelByStorePlaces.recountType || taskItem.mode == RecountType.ParallelByPerNo.recountType -> Parallels
                taskItem.blockType == "1" && taskItem.lockUser == userName -> BlockedMe
                taskItem.blockType == "1" || taskItem.blockType == "2" -> BlockedNotMe
                taskItem.notFinish.isNotBlank() && taskItem.mode == RecountType.Simple.recountType -> Processed
                else -> Free


            }

        }

    }
}




