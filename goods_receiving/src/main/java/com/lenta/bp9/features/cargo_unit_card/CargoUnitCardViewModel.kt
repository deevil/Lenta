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
    val spinStatusSelectedPosition: MutableLiveData<Int> = MutableLiveData()
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

    val storageValue by lazy {
        cargoUnitInfo.value?.stock ?: ""
    }

    val isTaskPSP by lazy {
        taskManager.getReceivingTask()?.taskHeader?.taskType == TaskType.OwnProduction
    }

    val isTaskShipmentRC by lazy {
        taskManager.getReceivingTask()?.taskHeader?.taskType == TaskType.ShipmentRC
    }

    val enabledApplyBtn: MutableLiveData<Boolean> = spinStatusSelectedPosition.combineLatest(spinTypePalletSelectedPosition).map {
        statusInfo.value?.get(it!!.first)?.code == "2" || spinTypePallet.value?.get(it!!.second) ?: "" != ""
    }

    val isGoodsForPackaging by lazy {
        taskManager.getReceivingTask()?.taskHeader?.taskType == TaskType.OwnProduction && cargoUnitInfo.value?.isPack == true
    }

    fun getTitle(): String {
        return taskManager.getReceivingTask()?.taskHeader?.caption ?: ""
    }

    val status: MutableLiveData<String> = MutableLiveData()
    init {
        viewModelScope.launch {
            typePalletInfo.value = dataBase.getTypePalletInfo()
            if (isTaskShipmentRC) {
                statusInfo.value = dataBase.getStatusInfoShipmentRC()
            } else {
                if (isSurplus.value!!) {
                    statusInfo.value = dataBase.getSurplusInfoForPRC()
                } else {
                    statusInfo.value = dataBase.getStatusInfoForPRC()
                }
            }

            spinStatus.value = statusInfo.value?.map {
                it.name
            }

            status.value = statusInfo.value?.findLast {
                it.code == cargoUnitInfo.value?.cargoUnitStatus
            }?.name ?: ""
            if (status.value?.isNotEmpty() == true) {
                spinStatusSelectedPosition.value = spinStatus.value?.indexOf(status.value ?: "")
                val pallet = typePalletInfo.value?.findLast {
                    it.code == cargoUnitInfo.value?.palletType
                }?.name ?: ""
                if (pallet.isNotEmpty()) {
                    spinTypePallet.value = listOf("").plus(typePalletInfo.value?.map {
                        it.name
                    } ?: emptyList())
                    spinTypePalletSelectedPosition.value = spinTypePallet.value?.indexOf(pallet)
                }
            } else {
                spinStatusSelectedPosition.value = 0
            }

        }
    }

    override fun onClickPosition(position: Int) {
        spinTypePalletSelectedPosition.value = position
    }

    fun onClickPositionSpinStatus(position: Int) {
        spinStatusSelectedPosition.value = position
        updateDataSpinTypePallet(statusInfo.value!![position].code)
    }

    private fun updateDataSpinTypePallet(selectedStatus: String) {
        if (!isTaskShipmentRC && selectedStatus == "2") {
            spinTypePallet.value = null
            spinTypePalletSelectedPosition.value = 0
        } else {
            spinTypePallet.value = listOf("").plus(typePalletInfo.value?.map {
                it.name
            } ?: emptyList())
        }
    }

    fun onClickApply() {
        val typePalletSave = if (spinTypePalletSelectedPosition.value == 0) "" else typePalletInfo.value?.get(spinTypePalletSelectedPosition.value!! -1)?.code
        processCargoUnitsService.apply(
                cargoUnitInfo.value!!,
                statusInfo.value?.get(spinStatusSelectedPosition.value!!)?.code ?: "",
                typePalletSave ?: ""
        )
        screenNavigator.goBack()
    }

    fun onClickThirdBtn() {
        if (isTaskShipmentRC) {
            processCargoUnitsService.apply(
                    cargoUnitInfo.value!!,
                    "5",
                    ""
            )
            screenNavigator.goBack()
        } else {
            processCargoUnitsService.delete(cargoUnitInfo.value!!)
            screenNavigator.goBack()
        }
    }
}
