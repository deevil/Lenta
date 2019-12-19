package com.lenta.bp9.features.control_delivery_cargo_units

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.features.loading.tasks.TaskCardMode
import com.lenta.bp9.features.mercury_list.MercuryListItem
import com.lenta.bp9.features.task_list.TaskItemVm
import com.lenta.bp9.features.task_list.TaskPostponedStatus
import com.lenta.bp9.model.processing.ProcessCargoUnitsService
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskCargoUnitInfoRestData
import com.lenta.bp9.model.task.revise.ConditionViewType
import com.lenta.bp9.model.task.revise.TransportConditionRestData
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.requests.network.UnloadingEndReceptionDistrCenterNetRequest
import com.lenta.bp9.requests.network.UnloadingEndReceptionDistrCenterParameters
import com.lenta.bp9.requests.network.UnloadingEndReceptionDistrCenterResult
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.databinding.Evenable
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.getDeviceIp
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toStringFormatted
import kotlinx.coroutines.launch
import javax.inject.Inject

class ControlDeliveryCargoUnitsViewModel : CoreViewModel(), PageSelectionListener, OnOkInSoftKeyboardListener {

    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var taskManager: IReceivingTaskManager
    @Inject
    lateinit var processCargoUnitsService: ProcessCargoUnitsService
    @Inject
    lateinit var unloadingEndReceptionDistrCenter: UnloadingEndReceptionDistrCenterNetRequest
    @Inject
    lateinit var context: Context
    @Inject
    lateinit var sessionInfo: ISessionInfo

    val selectedPage = MutableLiveData(0)
    private val listProcessedHolder: MutableLiveData<List<ControlDeliveryCargoUnitItem>> = MutableLiveData()
    private val listNotProcessedHolder: MutableLiveData<List<ControlDeliveryCargoUnitItem>> = MutableLiveData()
    val cargoUnitNumber: MutableLiveData<String> = MutableLiveData("")
    val requestFocusToCargoUnit: MutableLiveData<Boolean> = MutableLiveData()

    val listProcessed by lazy {
        listProcessedHolder.combineLatest(cargoUnitNumber).map { pair ->
            pair!!.first.filter {
                it.name.contains(pair.second, true)
            }.map {
                ControlDeliveryCargoUnitItem(
                        number = it.number,
                        name = it.name,
                        status = it.status
                )
            }
        }
    }

    val listNotProcessed by lazy {
        listNotProcessedHolder.combineLatest(cargoUnitNumber).map { pair ->
            pair!!.first.filter {
                it.name.contains(pair.second, true)
            }.map {
                ControlDeliveryCargoUnitItem(
                        number = it.number,
                        name = it.name,
                        status = it.status
                )
            }
        }
    }

    val saveEnabled = listNotProcessedHolder.map {
        it?.size == 0
    }

    init {
        viewModelScope.launch {
            taskManager.getReceivingTask()?.getCargoUnits()?.let {
                processCargoUnitsService.newProcessCargoUnitsService(it)
            }
            onResume()
        }
    }

    fun onResume() {
        updateNotProcessed()
        updateProcessed()
    }

    fun getTitle(): String {
        return taskManager.getReceivingTask()?.taskHeader?.caption ?: ""
    }

    fun onClickSave() {
        viewModelScope.launch {
            screenNavigator.showProgressLoadingData()
            processCargoUnitsService.save()
            val params = UnloadingEndReceptionDistrCenterParameters(
                    taskNumber = taskManager.getReceivingTask()?.taskHeader?.taskNumber ?: "",
                    deviceIP = context.getDeviceIp(),
                    personalNumber = sessionInfo.personnelNumber ?: "",
                    transportConditions = taskManager.getReceivingTask()?.taskRepository?.getReviseDocuments()?.getTransportConditions()?.map { TransportConditionRestData.from(it) } ?: emptyList(),
                    cargoUnits = taskManager.getReceivingTask()?.getCargoUnits()?.map { TaskCargoUnitInfoRestData.from(it) } ?: emptyList()
            )
            unloadingEndReceptionDistrCenter(params).either(::handleFailure, ::handleSuccess)
            screenNavigator.hideProgress()
        }
    }

    override fun handleFailure(failure: Failure) {
        screenNavigator.openAlertScreen(failure)
    }

    private fun handleSuccess(result: UnloadingEndReceptionDistrCenterResult) {
        screenNavigator.openMainMenuScreen()
        screenNavigator.openTaskListScreen()
        screenNavigator.openTaskCardScreen(TaskCardMode.Full)
        taskManager.getReceivingTask()?.taskRepository?.getReviseDocuments()?.getTransportConditions()?.filter {
            it.conditionViewType == ConditionViewType.Seal //&& it.value.isEmpty()
        }.let {
            if (it?.size != 0) {
                screenNavigator.openAlertSealDamageScreen()
            }
        }
    }

    private fun updateNotProcessed() {
        listNotProcessedHolder.postValue(
                processCargoUnitsService.getCargoUnits().filter {
                    it.cargoUnitStatus.isEmpty()
                }.mapIndexed { index, taskCargoUnitInfo ->
                    ControlDeliveryCargoUnitItem(
                            number = index + 1,
                            name = taskCargoUnitInfo.cargoUnitNumber,
                            status = ""
                    )
                }.reversed()
        )
    }

    private fun updateProcessed() {
        listProcessedHolder.postValue(
                processCargoUnitsService.getCargoUnits().filter {
                    it.cargoUnitStatus.isNotEmpty()
                }.mapIndexed { index, taskCargoUnitInfo ->
                    ControlDeliveryCargoUnitItem(
                            number = index + 1,
                            name = taskCargoUnitInfo.cargoUnitNumber,
                            status = taskCargoUnitInfo.cargoUnitStatus
                    )
                }.reversed()
        )
    }

    fun onDigitPressed(digit: Int) {
        requestFocusToCargoUnit.value = true
        cargoUnitNumber.value = cargoUnitNumber.value ?: "" + digit
    }

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    fun onClickItemPosition(position: Int) {
        if (selectedPage.value == 0) {
            taskManager.getReceivingTask()?.taskRepository?.getCargoUnits()?.findCargoUnits(listNotProcessed.value?.get(position)?.name ?: "")?.let {
                screenNavigator.openCargoUnitCardScreen(it)
            }
        } else {
            taskManager.getReceivingTask()?.taskRepository?.getCargoUnits()?.findCargoUnits(listProcessed.value?.get(position)?.name ?: "")?.let {
                screenNavigator.openCargoUnitCardScreen(it)
            }
        }
    }

    override fun onOkInSoftKeyboard(): Boolean {
        searchCargoUnit(cargoUnitNumber.value ?: "")
        return true
    }

    fun onScanResult(data: String) {
        searchCargoUnit(data)
    }

    private fun searchCargoUnit(data: String) {
        val findCargoUnit = processCargoUnitsService.findCargoUnit(data)
        if (findCargoUnit == null) {
            //screenNavigator.openCargoUnitCardScreen(it)
        } else {
            screenNavigator.openCargoUnitCardScreen(findCargoUnit)
        }
    }
}

data class ControlDeliveryCargoUnitItem(
        val number: Int,
        val name: String,
        val status: String
)
