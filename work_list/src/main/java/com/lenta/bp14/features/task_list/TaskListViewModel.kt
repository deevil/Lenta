package com.lenta.bp14.features.task_list

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp14.models.data.TaskListTab
import com.lenta.bp14.models.data.TaskManager
import com.lenta.bp14.models.data.pojo.Task
import com.lenta.bp14.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class TaskListViewModel : CoreViewModel(), PageSelectionListener, OnOkInSoftKeyboardListener {

    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var taskManager: TaskManager

    val selectedPage = MutableLiveData(0)

    val numberField: MutableLiveData<String> = MutableLiveData("")
    val requestFocusToNumberField: MutableLiveData<Boolean> = MutableLiveData()

    val marketNumber = MutableLiveData<String>("")

    val processingTasks = MutableLiveData<List<Task>>()
    val searchTasks = MutableLiveData<List<Task>>()

    val thirdButtonVisibility = selectedPage.map { it == TaskListTab.PROCESSING.position }

    init {
        viewModelScope.launch {
            marketNumber.value = taskManager.marketNumber

            processingTasks.value = taskManager.getTestTaskList(4)
            searchTasks.value = taskManager.getTestTaskList(3)
        }
    }

    override fun onOkInSoftKeyboard(): Boolean {
        return true
    }

    override fun onPageSelected(position: Int) {
        Logg.d { "onPageSelected: $position" }
        selectedPage.value = position
    }

    fun onClickUpdate() {

    }

    fun onClickFilter() {
        navigator.openSearchFilterTlScreen()
    }

    fun onClickMenu() {
        navigator.openMainMenuScreen()
    }

    fun onClickProcessingTask(position: Int) {
        navigator.openJobCardScreen(taskNumber = "100")
    }

    fun onClickSearchTask(position: Int) {
        navigator.openJobCardScreen(taskNumber = "100")
    }

}

