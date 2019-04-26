package com.lenta.bp10.features.job_card

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp10.platform.navigation.IScreenNavigator
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

    val productTypes: MutableLiveData<String> = MutableLiveData()
    val gisControls: MutableLiveData<String> = MutableLiveData()

    val storesNames = MutableLiveData<List<String>>()


    val onClickTaskTypes = object : OnPositionClickListener {
        override fun onClickPosition(position: Int) {
            selectedTaskTypePosition.value = position
            val taskType = taskSettingsList.value?.getOrNull(position)?.taskType
            updateProductTypes(taskType)
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
        screenNavigator.openGoodsListScreen()
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

    private fun updateProductTypes(taskType: String?) {
        viewModelScope.launch {
            productTypes.value = jobCardRepo.getProductTypes(taskType)
                    .joinToString(separator = "; ")
        }
    }


}

