package com.lenta.bp9.features.cargo_unit_card

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.model.processing.ProcessCargoUnitsService
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskCargoUnitInfo
import com.lenta.bp9.model.task.TaskType
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.repos.IDataBaseRepo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.combined.scan_info.pojo.QualityInfo
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.map
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
    val isSurplus: MutableLiveData<Boolean> = MutableLiveData()
    private val statusInfo: MutableLiveData<List<QualityInfo>> = MutableLiveData()
    private val typePalletInfo: MutableLiveData<List<QualityInfo>> = MutableLiveData()
    val deleteVisibility by lazy {
        MutableLiveData(cargoUnitInfo.value?.cargoUnitStatus == "3" || taskManager.getReceivingTask()?.taskHeader?.taskType == TaskType.ReceptionDistributionCenter || taskManager.getReceivingTask()?.taskHeader?.taskType == TaskType.OwnProduction)
    }
    val recountValue by lazy {
        if (cargoUnitInfo.value?.isCount == true) {
            "Да"
        } else {
            "Нет"
        }
    }

    val enabledApplyBtn: MutableLiveData<Boolean> = spinStatusSelectedPosition.combineLatest(spinTypePalletSelectedPosition).map {
        statusInfo.value?.get(it!!.first)?.code == "2" || spinTypePallet.value?.get(it!!.second) ?: "" != ""
    }

    fun getTitle(): String {
        return taskManager.getReceivingTask()?.taskHeader?.caption ?: ""
    }

    init {
        viewModelScope.launch {
            if (isSurplus.value!!) {
                statusInfo.value = dataBase.getSurplusInfoForPRC()
            } else {
                statusInfo.value = dataBase.getStatusInfoForPRC()
            }
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
            if (selectedStatus == "2") {
                spinTypePallet.value = null
            } else {
                screenNavigator.showProgressLoadingData()
                spinTypePalletSelectedPosition.value = 0
                typePalletInfo.value = dataBase.getTypePalletInfo()
                spinTypePallet.value = listOf("").plus(typePalletInfo.value?.map {
                    it.name
                } ?: emptyList())

                screenNavigator.hideProgress()
            }

        }
    }

    fun onClickApply() {
        processCargoUnitsService.apply(
                cargoUnitInfo.value!!,
                statusInfo.value?.get(spinStatusSelectedPosition.value!!)?.code ?: "",
                typePalletInfo.value?.get(spinTypePalletSelectedPosition.value!!)?.code ?: ""
        )
        screenNavigator.goBack()
    }

    fun onClickDelete() {
        processCargoUnitsService.delete(cargoUnitInfo.value!!)
        screenNavigator.goBack()
    }
}
