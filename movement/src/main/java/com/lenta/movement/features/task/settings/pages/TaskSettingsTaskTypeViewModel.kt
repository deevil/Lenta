package com.lenta.movement.features.task.settings.pages

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lenta.movement.models.ITaskManager
import com.lenta.movement.models.MovementType
import com.lenta.movement.models.TaskType
import com.lenta.movement.platform.IFormatter
import com.lenta.shared.view.OnPositionClickListener
import javax.inject.Inject

class TaskSettingsTaskTypeViewModel : ViewModel() {

    @Inject
    lateinit var formatter: IFormatter

    @Inject
    lateinit var taskManager: ITaskManager

    val taskTypeEnabled = MutableLiveData(false)
    val taskTypesFormatted by lazy {
        MutableLiveData(TaskType.values().map { formatter.getTaskTypeNameDescription(it) })
    }
    val taskTypeSelectedPosition by lazy { MutableLiveData(taskManager.getTask().taskType.ordinal) }

    val movementTypeEnabled = MutableLiveData(false)
    val movementTypesFormatted by lazy {
        MutableLiveData(MovementType.values().map { formatter.getMovementTypeNameDescription(it) })
    }
    val movementSelectedPosition by lazy { MutableLiveData(taskManager.getTask().movementType.ordinal) }

    val taskName by lazy { MutableLiveData(taskManager.getTask().name) }

    val receivers by lazy { MutableLiveData(listOf(taskManager.getTask().receiver)) }
    val receiverSelectedPosition = MutableLiveData(0)
    val receiverSelectedPositionListener = object : OnPositionClickListener {
        override fun onClickPosition(position: Int) {
            receiverSelectedPosition.postValue(position)
            taskManager.setTask(taskManager.getTask().copy(receiver = receivers.value.orEmpty()[position]))
        }
    }

    fun onResume() {
        if (taskManager.getTask().isCreated.not()) {
            val availableReceivers = taskManager.getAvailableReceivers()
            receivers.postValue(availableReceivers)
            taskManager.setTask(taskManager.getTask().copy(receiver = availableReceivers.get(receiverSelectedPosition.value ?: 0)))
        }
    }

    fun onTaskNameChanges(taskName: String) {
        taskManager.setTask(taskManager.getTask().copy(name = taskName))
    }

}