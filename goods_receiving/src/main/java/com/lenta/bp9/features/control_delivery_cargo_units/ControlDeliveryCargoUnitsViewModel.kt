package com.lenta.bp9.features.control_delivery_cargo_units

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.features.mercury_list.MercuryListItem
import com.lenta.bp9.features.task_list.TaskItemVm
import com.lenta.bp9.features.task_list.TaskPostponedStatus
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.databinding.Evenable
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toStringFormatted
import kotlinx.coroutines.launch
import javax.inject.Inject

class ControlDeliveryCargoUnitsViewModel : CoreViewModel(), PageSelectionListener, OnOkInSoftKeyboardListener {

    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var taskManager: IReceivingTaskManager

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
            updateNotProcessed()
            updateProcessed()
        }
    }

    fun getTitle(): String {
        return taskManager.getReceivingTask()?.taskHeader?.caption ?: ""
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

    fun onClickSave() {
        return
    }

    private fun updateNotProcessed() {
        listNotProcessedHolder.postValue(
                taskManager.getReceivingTask()?.getCargoUnits()?.filter {
                    it.cargoUnitStatus.isEmpty()
                }?.mapIndexed { index, taskCargoUnitInfo ->
                    ControlDeliveryCargoUnitItem(
                            number = index + 1,
                            name = taskCargoUnitInfo.cargoUnitNumber,
                            status = ""
                    )
                }?.reversed()
        )
    }

    private fun updateProcessed() {
        listProcessedHolder.postValue(
                taskManager.getReceivingTask()?.getCargoUnits()?.filter {
                    it.cargoUnitStatus.isNotEmpty()
                }?.mapIndexed { index, taskCargoUnitInfo ->
                    ControlDeliveryCargoUnitItem(
                            number = index + 1,
                            name = taskCargoUnitInfo.cargoUnitNumber,
                            status = taskCargoUnitInfo.cargoUnitStatus
                    )
                }?.reversed()
        )
    }

    fun onDigitPressed(digit: Int) {
        requestFocusToCargoUnit.value = true
        cargoUnitNumber.value = cargoUnitNumber.value ?: "" + digit
    }

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    override fun onOkInSoftKeyboard(): Boolean {
        return true
    }

}

data class ControlDeliveryCargoUnitItem(
        val number: Int,
        val name: String,
        val status: String
)
