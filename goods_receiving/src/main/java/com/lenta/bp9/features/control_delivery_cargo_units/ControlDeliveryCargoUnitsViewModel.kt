package com.lenta.bp9.features.control_delivery_cargo_units

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.R
import com.lenta.bp9.features.loading.tasks.TaskCardMode
import com.lenta.bp9.model.processing.ProcessCargoUnitsService
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskCargoUnitInfoRestData
import com.lenta.bp9.model.task.TaskType
import com.lenta.bp9.model.task.revise.ConditionViewType
import com.lenta.bp9.model.task.revise.TransportConditionRestData
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.repos.IDataBaseRepo
import com.lenta.bp9.requests.network.*
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.combined.scan_info.pojo.QualityInfo
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.getDeviceIp
import com.lenta.shared.utilities.extentions.map
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
    lateinit var gettingDataNewCargoUnit: GettingDataNewCargoUnitNetRequest
    @Inject
    lateinit var context: Context
    @Inject
    lateinit var sessionInfo: ISessionInfo
    @Inject
    lateinit var dataBase: IDataBaseRepo

    val selectedPage = MutableLiveData(0)
    private val statusInfo: MutableLiveData<List<QualityInfo>> = MutableLiveData()
    private val listProcessedHolder: MutableLiveData<List<ControlDeliveryCargoUnitItem>> = MutableLiveData()
    private val listNotProcessedHolder: MutableLiveData<List<ControlDeliveryCargoUnitItem>> = MutableLiveData()
    val cargoUnitNumber: MutableLiveData<String> = MutableLiveData("")
    val requestFocusToCargoUnit: MutableLiveData<Boolean> = MutableLiveData()
    private val searchCargoUnitNumber: MutableLiveData<String> = MutableLiveData("")
    val isTaskPSP by lazy {
        taskManager.getReceivingTask()?.taskHeader?.taskType == TaskType.OwnProduction
    }

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
            statusInfo.value = dataBase.getAllStatusInfoForPRC()
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

    fun getDescription(): String {
        return if (taskManager.getReceivingTask()?.taskHeader?.taskType == TaskType.OwnProduction) {
            context.getString(R.string.control_delivery_eo)
        } else {
            context.getString(R.string.control_delivery_cargo_units)
        }
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
            it.conditionViewType == ConditionViewType.Seal && it.value.isEmpty()
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
                            status = statusInfo.value?.findLast {
                                it.code == taskCargoUnitInfo.cargoUnitStatus
                            }?.name ?: ""
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
            processCargoUnitsService.findCargoUnit(listNotProcessed.value?.get(position)?.name ?: "")?.let {
                screenNavigator.openCargoUnitCardScreen(it)
            }
        } else {
            processCargoUnitsService.findCargoUnit(listProcessed.value?.get(position)?.name ?: "")?.let {
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
        viewModelScope.launch {
            searchCargoUnitNumber.value = data
            val findCargoUnit = processCargoUnitsService.findCargoUnit(data)
            if (findCargoUnit == null) {
                screenNavigator.showProgressLoadingData()
                val params = GettingDataNewCargoUnitParameters(
                        taskNumber = taskManager.getReceivingTask()?.taskHeader?.taskNumber ?: "",
                        cargoUnitNumber = data
                )
                gettingDataNewCargoUnit(params).either(::handleFailure, ::handleSuccessNewCargoUnit)
                screenNavigator.hideProgress()
            } else {
                screenNavigator.openCargoUnitCardScreen(findCargoUnit)
            }
        }
    }

    private fun handleSuccessNewCargoUnit(result: GettingDataNewCargoUnitResult) {
        when (result.cargoUnitType.toInt()) {
            0 -> screenNavigator.openNewCargoUnitAnotherTransportationDialog(searchCargoUnitNumber.value ?: "", nextCallbackFunc = {
                screenNavigator.openCargoUnitCardScreen(TaskNewCargoUnitInfoRestData.inCargoUnitInfo(result.cargoUnitStructure, searchCargoUnitNumber.value ?: ""), true)
            })
            1 -> screenNavigator.openAlertNewCargoUnitScreen(searchCargoUnitNumber.value ?: "")
            2 -> screenNavigator.openNewCargoUnitCurrentTransportationDialog(searchCargoUnitNumber.value ?: "", nextCallbackFunc = {
                screenNavigator.openCargoUnitCardScreen(TaskNewCargoUnitInfoRestData.inCargoUnitInfo(result.cargoUnitStructure, searchCargoUnitNumber.value ?: ""), true)
            })
        }
    }
}

data class ControlDeliveryCargoUnitItem(
        val number: Int,
        val name: String,
        val status: String
)
