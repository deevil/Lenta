package com.lenta.movement.features.task.settings.pages

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lenta.movement.models.ITaskManager
import com.lenta.shared.view.OnPositionClickListener
import javax.inject.Inject

class TaskSettingsPropertiesViewModel: ViewModel() {

    @Inject
    lateinit var taskManager: ITaskManager

    val pikingStorageList = MutableLiveData<List<String>>()
    val pikingStorageSelectedPosition = MutableLiveData<Int>()
    val pikingStorageSelectedPositionListener = object : OnPositionClickListener {
        override fun onClickPosition(position: Int) {
            pikingStorageSelectedPosition.postValue(position)
            taskManager.setTask(taskManager.getTask().copy(pikingStorage = pikingStorageList.value.orEmpty().getOrNull(position).orEmpty()))
        }
    }

    val shipmentStorageList = MutableLiveData<List<String>>()
    val shipmentStorageSelectedPosition = MutableLiveData<Int>()
    val shipmentStorageSelectedPositionListener = object : OnPositionClickListener {
        override fun onClickPosition(position: Int) {
            shipmentStorageSelectedPosition.postValue(position)
            taskManager.setTask(taskManager.getTask().copy(shipmentStorage = shipmentStorageList.value.orEmpty().getOrNull(position).orEmpty()))
        }
    }

    val shipmentDate by lazy { MutableLiveData(taskManager.getTask().shipmentDate) }

    fun onResume() {
        val availablePikingStorageList = taskManager.getAvailablePikingStorageList().addFirstEmptyIfNeeded()
        pikingStorageList.postValue(availablePikingStorageList)
        taskManager.setTask(taskManager.getTask().copy(pikingStorage = availablePikingStorageList[pikingStorageSelectedPosition.value ?: 0]))

        val availableShipmentStorageList = taskManager.getAvailableShipmentStorageList().addFirstEmptyIfNeeded()
        shipmentStorageList.postValue(availableShipmentStorageList)
        taskManager.setTask(taskManager.getTask().copy(shipmentStorage = availableShipmentStorageList[shipmentStorageSelectedPosition.value ?: 0]))
    }

    fun onShipmentDateChange(shipmentDate: String) {
        taskManager.setTask(taskManager.getTask().copy(shipmentDate = shipmentDate))
    }

    private fun List<String>.addFirstEmptyIfNeeded(): List<String> {
        return if (size > 1) {
            listOf("") + this
        } else {
            this
        }
    }
}