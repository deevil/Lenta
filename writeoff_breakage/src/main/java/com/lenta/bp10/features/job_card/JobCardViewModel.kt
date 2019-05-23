package com.lenta.bp10.features.job_card

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp10.models.repositories.IWriteOffTaskManager
import com.lenta.bp10.models.task.TaskDescription
import com.lenta.bp10.platform.navigation.IScreenNavigator
import com.lenta.bp10.requests.db.TaskCreatingParams
import com.lenta.bp10.requests.db.TaskDescriptionDbRequest
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.view.OnPositionClickListener
import kotlinx.coroutines.launch
import javax.inject.Inject

class JobCardViewModel : CoreViewModel() {
    @Inject
    lateinit var jobCardRepo: IJobCardRepo
    @Inject
    lateinit var sessionInfo: ISessionInfo
    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var processServiceManager: IWriteOffTaskManager
    @Inject
    lateinit var taskDescriptionDbRequest: TaskDescriptionDbRequest

    private val taskSettingsList: MutableLiveData<List<TaskSetting>> = MutableLiveData()
    val taskName: MutableLiveData<String> = MutableLiveData()
    val selectedTaskTypePosition: MutableLiveData<Int> = MutableLiveData(0)
    val selectedStorePosition: MutableLiveData<Int> = MutableLiveData()
    val enabledChangeTaskSettings: MutableLiveData<Boolean> = MutableLiveData()

    val taskTypeNames: MutableLiveData<List<String>> = taskSettingsList.map { taskSettingsList ->
        taskSettingsList?.map { it.name }
    }

    val motionType: MutableLiveData<String> = selectedTaskTypePosition.map {
        it?.let { pos ->
            taskSettingsList.value?.getOrNull(pos)?.motionType
        }
    }

    val materialTypes: MutableLiveData<String> = MutableLiveData()
    val gisControls: MutableLiveData<String> = MutableLiveData()

    val storesNames = MutableLiveData<List<String>>()


    val onClickTaskTypes = object : OnPositionClickListener {
        override fun onClickPosition(position: Int) {
            selectedTaskTypePosition.value = position
            updateDependencies()
        }
    }

    val onClickStores = object : OnPositionClickListener {
        override fun onClickPosition(position: Int) {
            selectedStorePosition.value = position
        }
    }


    init {
        viewModelScope.launch {
            taskSettingsList.value = jobCardRepo.getAllTaskSettings()
            if (taskName.value == null) {
                taskName.value = jobCardRepo.generateNameTask()
            }
            selectedTaskTypePosition.value = 0
            updateChangesEnabledStatus()
            updateDependencies()
        }
    }

    private fun updateChangesEnabledStatus() {
        enabledChangeTaskSettings.value = processServiceManager.getWriteOffTask() == null
    }

    fun onClickNext() {
        viewModelScope.launch {
            getSelectedTaskSettings()?.let {
                taskDescriptionDbRequest(
                        TaskCreatingParams(
                                taskName = taskName.value!!,
                                gisControlList = jobCardRepo.getGisControlList(getSelectedTaskSettings()?.taskType),
                                taskSetting = it,
                                stock = getSelectedStock() ?: ""
                        )).either(::handleFailure, ::openNextScreen)

            }
        }

    }

    private fun openNextScreen(taskDescription: TaskDescription) {
        if (processServiceManager.getWriteOffTask() == null) {
            processServiceManager.newWriteOffTask(taskDescription = taskDescription)
        } else {
            processServiceManager.getWriteOffTask()?.let {
                it.taskDescription.taskName = taskDescription.taskName
                it.taskDescription.stock = taskDescription.stock
            }
        }
        updateChangesEnabledStatus()
        screenNavigator.openGoodsListScreen()
    }

    private fun getSelectedTaskSettings(): TaskSetting? {
        return taskSettingsList.value?.getOrNull(selectedTaskTypePosition.value ?: -1)
    }

    private fun getSelectedStock(): String? {
        return storesNames.value?.getOrNull(selectedStorePosition.value ?: -1)
    }

    fun onClickBack() {
        processServiceManager.getWriteOffTask()?.let {
            it.taskDescription.taskName = taskName.value!!
            screenNavigator.openRemoveTaskConfirmationScreen(it.taskDescription.taskName, 0)
            return
        }
        screenNavigator.goBack()
    }


    private fun updateDependencies() {
        val taskType = getSelectedTaskSettings()?.taskType
        updateMaterialTypes(taskType)
        updateGisControls(taskType)
        updateStores(taskType)
    }


    private fun updateStores(taskType: String?) {
        viewModelScope.launch {
            storesNames.value = jobCardRepo.getStores(taskType)
        }
    }

    private fun updateGisControls(taskType: String?) {
        viewModelScope.launch {
            gisControls.value = jobCardRepo
                    .getGisControlList(taskType)
                    .joinToString(separator = "; ") { it.name }
        }

    }

    private fun updateMaterialTypes(taskType: String?) {
        viewModelScope.launch {
            materialTypes.value = jobCardRepo
                    .getMaterialTypes(taskType)
                    .joinToString(separator = "; ")
        }
    }

    fun getMarket(): String? {
        return sessionInfo.market
    }

    fun onConfirmRemoving() {
        processServiceManager.clearTask()
        screenNavigator.goBack()
    }


}

