package com.lenta.bp12.features.task_list

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp12.model.BlockType
import com.lenta.bp12.model.ITaskManager
import com.lenta.bp12.model.TaskStatus
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.bp12.request.TaskListNetRequest
import com.lenta.bp12.request.TaskListParams
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.settings.IAppSettings
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.databinding.PageSelectionListener
import kotlinx.coroutines.launch
import javax.inject.Inject

class TaskListViewModel : CoreViewModel(), PageSelectionListener, OnOkInSoftKeyboardListener {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var taskListNetRequest: TaskListNetRequest

    @Inject
    lateinit var appSettings: IAppSettings

    @Inject
    lateinit var manager: ITaskManager


    val title by lazy {
        "TK - ${sessionInfo.market}"
    }

    val selectedPage = MutableLiveData(0)

    val numberField: MutableLiveData<String> = MutableLiveData("")

    val requestFocusToNumberField by lazy {
        MutableLiveData(true)
    }

    val processing by lazy {
        MutableLiveData(List(3) {
            ItemTaskUi(
                    position = "${it + 1}",
                    name = "Test name ${it + 1}",
                    supplier = "Test supplier ${it + 1}",
                    taskStatus = TaskStatus.COMMON,
                    blockType = BlockType.UNLOCK,
                    quantity = (1..15).random().toString()
            )
        })
    }

    val search by lazy {
        MutableLiveData(List(3) {
            ItemTaskUi(
                    position = "${it + 1}",
                    name = "Test name ${it + 1}",
                    supplier = "Test supplier ${it + 1}",
                    taskStatus = TaskStatus.COMMON,
                    blockType = BlockType.UNLOCK,
                    quantity = (1..15).random().toString()
            )
        })
    }

    // -----------------------------

    init {
        viewModelScope.launch {
            loadTaskList()
        }
    }

    // -----------------------------

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    fun loadTaskList() {
        viewModelScope.launch {
            navigator.showProgressLoadingData()

            taskListNetRequest(
                    TaskListParams(
                            tkNumber = sessionInfo.market ?: "",
                            userAddress = "",
                            userNumber = appSettings.lastPersonnelNumber ?: "Not found!",
                            mode = 1
                    )
            ).also {
                navigator.hideProgress()
            }.either(::handleFailure) { taskListResult ->
                manager.addTasks(taskListResult.tasks)
            }
        }
    }

    override fun handleFailure(failure: Failure) {
        super.handleFailure(failure)
        navigator.openAlertScreen(failure)
    }

    fun onClickMenu() {
        navigator.goBack()
    }

    fun onClickUpdate() {

    }

    fun onClickItemPosition(position: Int) {

    }

    override fun onOkInSoftKeyboard(): Boolean {
        return true
    }

}

data class ItemTaskUi(
        val position: String,
        val name: String,
        val supplier: String,
        val taskStatus: TaskStatus,
        val blockType: BlockType,
        val quantity: String
)