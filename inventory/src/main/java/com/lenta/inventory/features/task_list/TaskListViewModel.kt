package com.lenta.inventory.features.task_list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.inventory.R
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

    val filter = MutableLiveData("")

    val tkNumber: String by lazy {
        sessionInfo.market ?: ""
    }
    val tasks: LiveData<List<TaskItem>> by lazy {
        repoInMemoryHolder.tasksListRestInfo.map {
            it?.tasks?.mapIndexed { index, task ->
                TaskItem(
                        number = "${index + 1}",
                        title = task.taskName,
                        stock = task.stock,
                        typeConversion = if (task.isRecount.isBlank()) "1" else "2",
                        statusTask = StatusTask.from(task),
                        count = task.countProductsInTask
                )
            }?.reversed()
        }
    }
    val tasksCount by lazy {
        tasks.map { tasks.value?.size ?: 0 }
    }


    init {
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

    override fun handleFailure(failure: Failure) {
        super.handleFailure(failure)
        screenNavigator.openAlertScreen(failure)
    }

    private fun handleUpdateSuccess(tasksListRestInfo: TasksListRestInfo) {
        repoInMemoryHolder.tasksListRestInfo.value = tasksListRestInfo

    }

    override fun onOkInSoftKeyboard(): Boolean {
        onClickUpdate()
        return true
    }

    fun onClickItemPosition(position: Int) {
        screenNavigator.openJobCard()
    }

}

data class TaskItem(
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
        fun from(taskItem: TasksItem): StatusTask {
            return when {
                taskItem.notFinish.isNotBlank() -> Processed
                taskItem.mode == "2" || taskItem.mode == "3" -> Parallels
                taskItem.blockType == "1" -> BlockedMe
                taskItem.blockType == "2" -> BlockedNotMe
                else -> Free


            }

        }

    }
}




