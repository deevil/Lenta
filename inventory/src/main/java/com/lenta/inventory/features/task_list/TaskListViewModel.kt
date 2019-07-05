package com.lenta.inventory.features.task_list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.inventory.platform.navigation.IScreenNavigator
import com.lenta.inventory.platform.navigation.ScreenNavigator
import com.lenta.inventory.repos.IRepoInMemoryHolder
import com.lenta.inventory.requests.network.TaskListNetRequest
import com.lenta.inventory.requests.network.TasksListParams
import com.lenta.inventory.requests.network.TasksListRestInfo
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.view.OnPositionClickListener
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
                        typeConversion = TypeConversion.Primary,
                        statusTask = StatusTask.from(task.blockType),
                        count = "0"
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

}

data class TaskItem(
        val number: String,
        val title: String,
        val stock: String,
        val typeConversion: TypeConversion,
        val statusTask: StatusTask,
        val count: String

)


//TODO нужно уточнить формирование и отображение признаков

enum class TypeConversion {
    Primary,
    Secondary,
    Parallels;
}

enum class StatusTask {
    Free,
    BlockedMe,
    BlockedNotMe,
    Processed;

    companion object {
        fun from(code: String): StatusTask {
            return Free
        }

    }
}


