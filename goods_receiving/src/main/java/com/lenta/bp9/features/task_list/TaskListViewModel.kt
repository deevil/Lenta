package com.lenta.bp9.features.task_list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.models.task.DirectSupplierTask
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.repos.IRepoInMemoryHolder
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.SelectionItemsHelper
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
    lateinit var screenNavigator: IScreenNavigator

    var selectedPage = MutableLiveData(0)
    val filter = MutableLiveData("")

    val tkNumber: String by lazy {
        sessionInfo.market ?: ""
    }

    val tasks: LiveData<List<TaskItemVm>> by lazy {
        repoInMemoryHolder.tasksListRestInfo.map {
            it!!.tasks
                    .mapIndexed { index, task ->
                        TaskItemVm(
                                taskNumber = task.taskNumber
                        )
                    }
        }
    }

    val tasksCount by lazy {
        tasks.map { tasks.value?.size ?: 0 }
    }

    init {

    }

    fun onClickUpdate() {

    }

    fun onClickMenu() {
        screenNavigator.openMainMenuScreen()
    }

    override fun handleFailure(failure: Failure) {
        super.handleFailure(failure)
        screenNavigator.openAlertScreen(failure)
    }

    override fun onOkInSoftKeyboard(): Boolean {
        return true
    }

    fun onPageSelected(position: Int) {

    }

    fun onClickItemPosition(position: Int) {

    }

    fun onResume() {

    }
}

data class TaskItemVm(
        val taskNumber: String
)