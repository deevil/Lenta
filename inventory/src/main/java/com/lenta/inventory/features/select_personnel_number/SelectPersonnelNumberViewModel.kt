package com.lenta.inventory.features.select_personnel_number

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.inventory.models.IPersistInventoryTask
import com.lenta.inventory.models.task.IInventoryTaskManager
import com.lenta.inventory.platform.navigation.IScreenNavigator
import com.lenta.inventory.repos.IRepoInMemoryHolder
import com.lenta.inventory.requests.network.TaskListNetRequest
import com.lenta.inventory.requests.network.TasksListParams
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.features.select_personnel_number.SelectPersonnelNumberDelegate
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.settings.IAppSettings
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map
import javax.inject.Inject

class SelectPersonnelNumberViewModel : CoreViewModel(), OnOkInSoftKeyboardListener {
    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var sessionInfo: ISessionInfo
    @Inject
    lateinit var appSettings: IAppSettings
    @Inject
    lateinit var selectPersonnelNumberDelegate: SelectPersonnelNumberDelegate
    @Inject
    lateinit var persistInventoryTask: IPersistInventoryTask
    @Inject
    lateinit var inventoryTaskManager: IInventoryTaskManager
    @Inject
    lateinit var taskListNetRequest: TaskListNetRequest
    @Inject
    lateinit var repoInMemoryHolder: IRepoInMemoryHolder

    val editTextFocus = MutableLiveData<Boolean>()
    private val nextButtonFocus = MutableLiveData<Boolean>()
    val isScreenMainMenu: MutableLiveData<Boolean> = MutableLiveData()
    val personnelNumber = MutableLiveData("")
    val fullName = MutableLiveData("")
    val employeesPosition = MutableLiveData("")
    val enabledBackButton by lazy {
        isScreenMainMenu
    }
    val enabledNextButton = fullName.map { !it.isNullOrBlank() }


    init {
        launchUITryCatch {
            selectPersonnelNumberDelegate.personnelNumber = personnelNumber
            selectPersonnelNumberDelegate.fullName = fullName
            selectPersonnelNumberDelegate.employeesPosition = employeesPosition
            selectPersonnelNumberDelegate.editTextFocus = editTextFocus
            selectPersonnelNumberDelegate.nextButtonFocus = nextButtonFocus
            selectPersonnelNumberDelegate.init(
                    viewModelScope = this@SelectPersonnelNumberViewModel::viewModelScope,
                    onNextScreenOpen = {
                        persistInventoryTask.getSavedWriteOffTask().let {
                            if (it == null || it.taskDescription.tkNumber != sessionInfo.market) {
                                screenNavigator.openMainMenuScreen()
                            } else {
                                checkCorrectSavedData()
                            }
                        }
                    }
            )
        }
    }

    private fun checkCorrectSavedData() {

        launchUITryCatch {
            screenNavigator.showProgress(taskListNetRequest)
            taskListNetRequest(
                    TasksListParams(
                            werks = sessionInfo.market
                                   .orEmpty(),
                            user = sessionInfo.userName
                                   .orEmpty()
                    )
            ).either(::handleFailure)
            { tasksListRestInfo ->
                Logg.d { "tasksListRestInfo $tasksListRestInfo" }
                repoInMemoryHolder.tasksListRestInfo.value = tasksListRestInfo
                if (repoInMemoryHolder.tasksListRestInfo.value?.tasks?.firstOrNull { it.taskNumber == persistInventoryTask.getSavedWriteOffTask()?.taskDescription?.taskNumber } != null) {
                    openDetectionSavedDataScreen()
                } else {
                    persistInventoryTask.saveWriteOffTask(null)
                }
            }

            screenNavigator.hideProgress()
        }


    }

    private fun openDetectionSavedDataScreen() {
        screenNavigator.openDetectedSavedDataScreen(
                deleteCallback = {
                    persistInventoryTask.saveWriteOffTask(null)
                    screenNavigator.openMainMenuScreen()
                },
                confirmCallback = {
                    inventoryTaskManager.setTask(persistInventoryTask.getSavedWriteOffTask())
                    screenNavigator.closeAllScreen()
                    screenNavigator.openTasksList()
                    screenNavigator.openJobCard(inventoryTaskManager.getInventoryTask()!!.taskDescription.taskNumber)
                }
        )
    }

    override fun handleFailure(failure: Failure) {
        super.handleFailure(failure)
        selectPersonnelNumberDelegate.handleFailure(failure)
    }

    override fun onOkInSoftKeyboard(): Boolean {
        return selectPersonnelNumberDelegate.onOkInSoftKeyboard()
    }

    fun onClickNext() {
        selectPersonnelNumberDelegate.onClickNext()
    }


    fun onResume() {
        selectPersonnelNumberDelegate.onResume()
    }


    fun onScanResult(data: String) {
        selectPersonnelNumberDelegate.onScanResult(data)
    }

    fun onBackPressed() {
        screenNavigator.openMainMenuScreen()
    }

}
