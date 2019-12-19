package com.lenta.bp9.features.cargo_unit_card

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.model.processing.ProcessCargoUnitsService
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskCargoUnitInfo
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.repos.IDataBaseRepo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.combined.scan_info.pojo.QualityInfo
import com.lenta.shared.view.OnPositionClickListener
import kotlinx.coroutines.launch
import javax.inject.Inject

class CargoUnitCardViewModel : CoreViewModel(), OnPositionClickListener {

    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var taskManager: IReceivingTaskManager
    @Inject
    lateinit var dataBase: IDataBaseRepo
    @Inject
    lateinit var processCargoUnitsService: ProcessCargoUnitsService

    val cargoUnitInfo: MutableLiveData<TaskCargoUnitInfo> = MutableLiveData()
    val spinStatus: MutableLiveData<List<String>> = MutableLiveData()
    val spinStatusSelectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val spinTypePallet: MutableLiveData<List<String>> = MutableLiveData()
    val spinTypePalletSelectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    private val statusInfo: MutableLiveData<List<QualityInfo>> = MutableLiveData()
    private val typePalletInfo: MutableLiveData<List<QualityInfo>> = MutableLiveData()
    val recountValue by lazy {
        if (cargoUnitInfo.value?.isCount == true) {
            "Да"
        } else {
            "Нет"
        }
    }

    fun getTitle(): String {
        return taskManager.getReceivingTask()?.taskHeader?.caption ?: ""
    }

    init {
        viewModelScope.launch {
            statusInfo.value = dataBase.getStatusInfoForPRC()
            spinStatus.value = statusInfo.value?.map {
                it.name
            }
        }
    }

    override fun onClickPosition(position: Int) {
        spinTypePalletSelectedPosition.value = position
    }

    fun onClickPositionSpinStatus(position: Int) {
        viewModelScope.launch {
            spinStatusSelectedPosition.value = position
            updateDataSpinTypePallet(statusInfo.value!![position].code)
        }
    }

    private suspend fun updateDataSpinTypePallet(selectedStatus: String) {
        viewModelScope.launch {
            if (selectedStatus == "1") {
                screenNavigator.showProgressLoadingData()
                spinTypePalletSelectedPosition.value = 0
                typePalletInfo.value = dataBase.getTypePalletInfo()
                spinTypePallet.value = typePalletInfo.value?.map {
                    it.name
                }
                screenNavigator.hideProgress()
            } else {
                spinTypePallet.value = null
            }

        }
    }

    fun onClickApply() {
        processCargoUnitsService.change(
                cargoUnitInfo.value?.cargoUnitNumber ?: "",
                spinStatus.value?.get(spinStatusSelectedPosition.value!!) ?: "",
                spinTypePallet.value?.get(spinTypePalletSelectedPosition.value!!) ?: ""
        )
        screenNavigator.goBack()
    }
}
