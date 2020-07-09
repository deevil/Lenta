package com.lenta.inventory.features.loading.tasks

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.inventory.platform.navigation.IScreenNavigator
import com.lenta.inventory.repos.IRepoInMemoryHolder
import com.lenta.inventory.requests.network.TaskListNetRequest
import com.lenta.inventory.requests.network.TasksListParams
import com.lenta.inventory.requests.network.TasksListRestInfo
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.features.loading.CoreLoadingViewModel
import com.lenta.shared.utilities.Logg
import kotlinx.coroutines.launch
import javax.inject.Inject

class LoadingTasksViewModel : CoreLoadingViewModel() {
    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var taskListNetRequest: TaskListNetRequest
    @Inject
    lateinit var sessionInfo: ISessionInfo
    @Inject
    lateinit var repoInMemoryHolder: IRepoInMemoryHolder

    override val title: MutableLiveData<String> = MutableLiveData()
    override val progress: MutableLiveData<Boolean> = MutableLiveData(true)
    override val speedKbInSec: MutableLiveData<Int> = MutableLiveData()
    override val sizeInMb: MutableLiveData<Float> = MutableLiveData()

    init {
        viewModelScope.launch {
            progress.value = true
            taskListNetRequest(TasksListParams(
                    werks = sessionInfo.market.orEmpty(),
                    user = sessionInfo.userName.orEmpty())
            ).either(::handleFailure, ::handleSuccess)
            progress.value = false
        }
    }

    override fun handleFailure(failure: Failure) {
        screenNavigator.openMainMenuScreen()
        screenNavigator.openAlertScreen(failure)
    }

    private fun handleSuccess(tasksListRestInfo: TasksListRestInfo) {
        Logg.d { "tasksListRestInfo $tasksListRestInfo" }
        repoInMemoryHolder.tasksListRestInfo.value = tasksListRestInfo
        screenNavigator.openTasksList()
    }

    override fun clean() {
        progress.postValue(false)
    }
}