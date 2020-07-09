package com.lenta.bp10.features.job_card

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp10.models.IPersistWriteOffTask
import com.lenta.bp10.models.repositories.IWriteOffTaskManager
import com.lenta.bp10.models.task.TaskDescription
import com.lenta.bp10.platform.navigation.IScreenNavigator
import com.lenta.bp10.platform.resources.IStringResourceManager
import com.lenta.bp10.requests.db.TaskCreatingParams
import com.lenta.bp10.requests.db.TaskDescriptionDbRequest
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.settings.IAppSettings
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
    @Inject
    lateinit var persistWriteOffTask: IPersistWriteOffTask
    @Inject
    lateinit var appSettings: IAppSettings
    @Inject
    lateinit var stringResourceManager: IStringResourceManager

    private val taskSettingsList: MutableLiveData<List<TaskSetting>> = MutableLiveData()
    val taskName: MutableLiveData<String> = MutableLiveData()
    val selectedTaskTypePosition: MutableLiveData<Int> = MutableLiveData(0)
    val selectedStorePosition: MutableLiveData<Int> = MutableLiveData(0)
    val enabledChangeTaskSettings: MutableLiveData<Boolean> = MutableLiveData()
    val enabledNextButton: MutableLiveData<Boolean> = selectedTaskTypePosition.map { it!! > 0 }

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
            taskSettingsList.value =
                    mutableListOf(TaskSetting(name = stringResourceManager.selectTaskType(),
                            motionType = "",
                            taskType = ""))
                            .apply {
                                addAll(jobCardRepo.getAllTaskSettings())
                            }


            processServiceManager.getWriteOffTask().let { writeOffTask ->
                if (writeOffTask != null) {
                    taskName.value = writeOffTask.taskDescription.taskName
                } else if (taskName.value == null) {
                    taskName.value = jobCardRepo.generateNameTask()
                }

                val taskType = writeOffTask?.taskDescription?.taskType?.code
                        ?: appSettings.lastJobType

                taskSettingsList.value?.let { list ->
                    selectedTaskTypePosition.value = list.indexOfFirst { it.taskType == taskType }
                }

                if (selectedTaskTypePosition.value!! < 0) {
                    selectedTaskTypePosition.value = 0
                }



                updateChangesEnabledStatus()
                updateDependencies()
            }


        }
    }

    private fun updateChangesEnabledStatus() {
        enabledChangeTaskSettings.value = processServiceManager.getWriteOffTask() == null
    }

    fun onClickNext() {
        viewModelScope.launch {
            getSelectedTaskSettings()?.let {
                appSettings.lastJobType = it.taskType
                taskDescriptionDbRequest(
                        TaskCreatingParams(
                                taskName = taskName.value!!,
                                gisControlList = jobCardRepo.getGisControlList(getSelectedTaskSettings()?.taskType),
                                taskSetting = it,
                                stock = getSelectedStock().orEmpty()
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
            processServiceManager.getWriteOffTask()?.let { writeOffTask ->
                storesNames.value?.let { list ->
                    selectedStorePosition.value = list.indexOfFirst { it == writeOffTask.taskDescription.stock }
                }
            }

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
        persistWriteOffTask.saveWriteOffTask(null)
    }


}

