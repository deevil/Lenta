package com.lenta.bp10.features.job_card

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp10.models.repositories.IWriteOffTaskManager
import com.lenta.bp10.models.task.TaskDescription
import com.lenta.bp10.platform.navigation.IScreenNavigator
import com.lenta.bp10.requests.db.TaskCreatingParams
import com.lenta.bp10.requests.db.TaskDescriptionDbRequest
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.view.OnPositionClickListener
import kotlinx.coroutines.launch
import javax.inject.Inject

class JobCardViewModel : CoreViewModel() {
    @Inject
    lateinit var jobCardRepo: IJobCardRepo
    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var processServiceManager: IWriteOffTaskManager
    @Inject
    lateinit var taskDescriptionDbRequest: TaskDescriptionDbRequest

    val taskSettingsList: MutableLiveData<List<TaskSetting>> = MutableLiveData()
    val taskName: MutableLiveData<String> = MutableLiveData()
    val selectedTaskTypePosition: MutableLiveData<Int> = MutableLiveData()
    val selectedStorePosition: MutableLiveData<Int> = MutableLiveData()

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
            val taskType = getSelectedTaskSettings()?.taskType
            updateMaterialTypes(taskType)
            updateGisControls(taskType)
            updateStores(taskType)
        }
    }

    val onClickStores = object : OnPositionClickListener {
        override fun onClickPosition(position: Int) {
            selectedStorePosition.value = position
        }
    }


    init {
        viewModelScope.launch {
            taskSettingsList.postValue(jobCardRepo.getAllTaskSettings())
            if (taskName.value == null) {
                taskName.value = jobCardRepo.generateNameTask()
            }
        }
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
                        )).either(::handleFailure, ::createNewTask)

            }
        }

    }

    private fun createNewTask(taskDescription: TaskDescription) {
        processServiceManager.newWriteOffTask(taskDescription = taskDescription)
        screenNavigator.openGoodsListScreen()
    }

    private fun getSelectedTaskSettings(): TaskSetting? {
        return taskSettingsList.value?.getOrNull(selectedTaskTypePosition.value ?: -1)
    }

    private fun getSelectedStock(): String? {
        return storesNames.value?.getOrNull(selectedStorePosition.value ?: -1)
    }

    fun onClickBack() {

        //TODO (DB) нужно уточнить реализацию


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


}

