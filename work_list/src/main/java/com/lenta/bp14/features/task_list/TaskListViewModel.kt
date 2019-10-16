package com.lenta.bp14.features.task_list

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp14.models.data.TaskListTab
import com.lenta.bp14.models.general.ITasksSearchHelper
import com.lenta.bp14.models.general.TaskInfo
import com.lenta.bp14.platform.navigation.IScreenNavigator
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class TaskListViewModel : CoreViewModel(), PageSelectionListener, OnOkInSoftKeyboardListener {

    @Inject
    lateinit var sessionInfo: ISessionInfo
    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var tasksSearchHelper: ITasksSearchHelper

    val selectedPage = MutableLiveData(0)

    val searchFieldProcessing: MutableLiveData<String> = MutableLiveData("")

    val searchFieldFiltered: MutableLiveData<String> = MutableLiveData("")

    val requestFocusToNumberField: MutableLiveData<Boolean> = MutableLiveData()

    val marketNumber by lazy { sessionInfo.market }

    private val funcTaskAdapter = { taskList: List<TaskInfo>? ->
        taskList?.mapIndexed { index, taskInfo ->
            ItemTaskUi(
                    position = (taskList.size - index).toString(),
                    type = taskInfo.taskTypeInfo,
                    name = taskInfo.taskName,
                    isProcessed = taskInfo.isNotFinished,
                    blockingStatus = if (taskInfo.isMyBlock == null) TaskBlockingStatus.NOT_BLOCKED else {
                        if (taskInfo.isMyBlock) TaskBlockingStatus.SELF_BLOCK else TaskBlockingStatus.BLOCK
                    },
                    quantity = taskInfo.quantityPositions.toString(),
                    taskId = taskInfo.taskId
            )
        } ?: emptyList()
    }

    val processingTasks by lazy {
        tasksSearchHelper.taskList.map(funcTaskAdapter)
    }
    val searchTasks by lazy {
        tasksSearchHelper.filteredTaskList.map(funcTaskAdapter)
    }

    val thirdButtonVisibility = selectedPage.map { it == TaskListTab.PROCESSING.position }

    init {
        viewModelScope.launch {
            searchFieldProcessing.value = tasksSearchHelper.processedFilter ?: ""
            searchFieldFiltered.value = tasksSearchHelper.searchFilter ?: ""
            updateProcessing()
        }

    }

    override fun onOkInSoftKeyboard(): Boolean {
        if (selectedPage.value == 0) {
            updateProcessing()
        } else {
            updateFiltered()
        }
        return true
    }


    override fun onPageSelected(position: Int) {
        Logg.d { "onPageSelected: $position" }
        selectedPage.value = position
    }

    fun onClickUpdate() {
        updateProcessing()
    }

    private fun updateProcessing() {
        viewModelScope.launch {
            navigator.showProgressLoadingData()
            tasksSearchHelper.processedFilter = searchFieldProcessing.value
            tasksSearchHelper.updateTaskList().either({
                navigator.openAlertScreen(it)
            }) {
                // not used
            }
            navigator.hideProgress()
        }
    }

    private fun updateFiltered() {
        viewModelScope.launch {
            navigator.showProgressLoadingData()
            tasksSearchHelper.searchFilter = searchFieldFiltered.value
            tasksSearchHelper.updateFilteredTaskList().either({
                navigator.openAlertScreen(it)
            }) {
                // not used
            }
            navigator.hideProgress()
        }
    }

    fun onClickFilter() {
        navigator.openSearchFilterTlScreen()
    }

    fun onClickMenu() {
        navigator.openMainMenuScreen()
    }

    fun onClickProcessingTask(position: Int) {
        processingTasks.value?.getOrNull(position)?.taskId?.let {
            setProcessedTask(it)
        }
    }

    fun onClickSearchTask(position: Int) {
        searchTasks.value?.getOrNull(position)?.taskId?.let {
            setProcessedTask(it)
        }
    }


    private fun setProcessedTask(taskId: String) {
        tasksSearchHelper.setProcessedTask(taskId)
        navigator.openJobCardScreen()
    }

    fun onResume() {
        if (tasksSearchHelper.isNewSearchData) {
            tasksSearchHelper.isNewSearchData = false
            selectedPage.postValue(1)
        }
        if (tasksSearchHelper.isDataChanged) {
            tasksSearchHelper.isDataChanged = false
            onClickUpdate()
        }
    }

    fun onDigitPressed(digit: Int) {
        when (selectedPage.value) {
            0 -> searchFieldProcessing
            else -> searchFieldFiltered
        }.let {
            it.postValue(it.value ?: "" + digit)
        }
        requestFocusToNumberField.value = true
    }

}


data class ItemTaskUi(
        val taskId: String,
        val position: String,
        val type: String,
        val name: String,
        val blockingStatus: TaskBlockingStatus,
        val isProcessed: Boolean,
        val quantity: String
)

enum class TaskBlockingStatus {
    NOT_BLOCKED,
    SELF_BLOCK,
    BLOCK
}

