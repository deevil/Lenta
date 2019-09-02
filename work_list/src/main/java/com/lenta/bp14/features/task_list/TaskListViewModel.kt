package com.lenta.bp14.features.task_list

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp14.data.TaskListTab
import com.lenta.bp14.data.TaskManager
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

    val marketNumber = MutableLiveData<String>("")

    val processingTasks = MutableLiveData<List<TaskInfoVM>>(getTestItems())
    val searchTasks = MutableLiveData<List<TaskInfoVM>>(getTestItems())

    val thirdButtonVisibility = selectedPage.map { it == TaskListTab.PROCESSING.position }

    init {
        viewModelScope.launch {
            marketNumber.value = taskManager.marketNumber
        }
    }

    override fun onOkInSoftKeyboard(): Boolean {
        return true
    }

    private fun getTestItems(): List<TaskInfoVM>? {
        return listOf(
                TaskInfoVM(
                        number = 3,
                        name = "name3",
                        type = "type",
                        status = "status",
                        quantity = "10"
                ),
                TaskInfoVM(
                        number = 2,
                        name = "name2",
                        type = "type",
                        status = "status",
                        quantity = "10"
                ),
                TaskInfoVM(
                        number = 1,
                        name = "name1",
                        type = "type",
                        status = "status",
                        quantity = "10"
                )
        )
    }

    val filter = MutableLiveData("")

    override fun onPageSelected(position: Int) {
        Logg.d { "onPageSelected: $position" }
        selectedPage.value = position
    }

    fun onClickUpdate() {

    }

    fun onClickFilter() {

    }

    fun onClickSave() {

    }

    fun onClickUnprocessedTask(position: Int) {
        navigator.openJobCardScreen(taskNumber = "100")
    }

    fun onClickProcessedTask(position: Int) {
        navigator.openJobCardScreen(taskNumber = "100")
    }

    fun onClickMenu() {

    }

    fun onClickBack() {
        navigator.openMainMenuScreen()
    }
}

data class TaskInfoVM(
        val number: Int,
        val name: String,
        val type: String,
        val status: String,
        val quantity: String
)

