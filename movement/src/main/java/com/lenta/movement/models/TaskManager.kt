package com.lenta.movement.models

import com.lenta.movement.fmp.resources.fast.ZmpUtz47V001
import com.lenta.movement.fmp.resources.fast.ZmpUtz48V001
import com.lenta.movement.fmp.resources.fast.ZmpUtz79V001
import com.lenta.shared.platform.constants.Constants
import com.lenta.shared.utilities.date_time.DateTimeUtil
import com.mobrun.plugin.api.HyperHive
import java.util.*

class TaskManager(
    private val hyperHive: HyperHive
) : ITaskManager {

    private var onTaskChanges: ((Task) -> Unit)? = null
    private var task: Task? = null

    override fun getTask(): Task {
        if (task == null) {
            task = Task(
                isCreated = false,
                name = "Перемещение от ${DateTimeUtil.formatDate(Date(), Constants.DATE_FORMAT_dd_mm_yyyy_hh_mm)}",
                taskType = TaskType.TransferWithoutOrder,
                movementType = MovementType.SS,
                receiver = "",
                description = "",
                pikingStorage = "",
                shipmentStorage = "",
                shipmentDate = ""
            )
        }

        return task!!
    }

    override fun setTask(task: Task) {
        this.task = task
        onTaskChanges?.invoke(task)
    }

    override fun setOnTaskChanges(block: (Task) -> Unit) {
        onTaskChanges = block
    }

    override fun getAvailableReceivers(): List<String> {
        return ZmpUtz79V001(hyperHive).localHelper_ET_PLANTS.all
            .map { table ->
                table.plant
            }
            .distinct()
    }

    override fun getAvailablePikingStorageList(): List<String> {
        return ZmpUtz48V001(hyperHive).localHelper_ET_LGORT_SRC.all
            .filter {
                it.mvmType == task?.movementType && it.mvmType == task?.movementType
            }
            .map {
                it.lgortSource
            }
            .distinct()
    }

    override fun getAvailableShipmentStorageList(): List<String> {
        return ZmpUtz47V001(hyperHive).localHelper_ET_TASK_TPS.all
            .filter {
                it.mvmType == task?.movementType && it.mvmType == task?.movementType
            }
            .map {
                it.lgortTarget
            }
            .distinct()
    }

    override fun getTaskAnnotation(): String {
        return ZmpUtz47V001(hyperHive).localHelper_ET_TASK_TPS.all
            .first {
                it.mvmType == task?.movementType && it.mvmType == task?.movementType
            }
            .annotation
    }
}