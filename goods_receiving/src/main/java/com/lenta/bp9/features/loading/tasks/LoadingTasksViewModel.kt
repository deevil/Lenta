package com.lenta.bp9.features.loading.tasks

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.model.task.TaskList
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.repos.IRepoInMemoryHolder
import com.lenta.bp9.requests.network.TaskListNetRequest
import com.lenta.bp9.requests.network.TaskListParams
import com.lenta.bp9.requests.network.TaskListSearchParams
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.features.loading.CoreLoadingViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.getDeviceIp
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
    @Inject
    lateinit var context: Context

    override val title: MutableLiveData<String> = MutableLiveData()
    override val progress: MutableLiveData<Boolean> = MutableLiveData(true)
    override val speedKbInSec: MutableLiveData<Int> = MutableLiveData()
    override val sizeInMb: MutableLiveData<Float> = MutableLiveData()

    var searchParams: TaskListSearchParams? = null
    var mode: TaskListLoadingMode = TaskListLoadingMode.None

    init {
        viewModelScope.launch {
            progress.value = true
            val params = TaskListParams(type = mode.taskListLoadingModeString,
                    ip = context.getDeviceIp(),
                    storeNumber = sessionInfo.market ?: "",
                    userNumber = sessionInfo.personnelNumber ?: "",
                    searchParams = searchParams
            )
            taskListNetRequest(params).either(::handleFailure, ::handleSuccess)
            progress.value = false
        }
    }

    override fun handleFailure(failure: Failure) {
        screenNavigator.openAlertNotFoundTaskScreen(failure)
    }

    private fun handleSuccess(taskList: TaskList) {
        Logg.d { "taskList $taskList" }
        screenNavigator.goBack()
        if (searchParams == null) {
            repoInMemoryHolder.taskList.value = taskList
            screenNavigator.openTaskListScreen()
        } else {
            repoInMemoryHolder.lastSearchResult.value = taskList
            screenNavigator.goBack()
        }
    }

    override fun clean() {
        progress.postValue(false)
    }
}