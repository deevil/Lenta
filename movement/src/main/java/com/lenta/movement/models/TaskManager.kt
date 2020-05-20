package com.lenta.movement.models

import com.lenta.movement.fmp.resources.fast.ZmpUtz47V001
import com.lenta.movement.fmp.resources.fast.ZmpUtz48V001
import com.lenta.movement.fmp.resources.fast.ZmpUtz79V001
import com.lenta.shared.platform.constants.Constants
import com.lenta.shared.utilities.date_time.DateTimeUtil
import com.lenta.shared.utilities.extentions.isSapTrue
import com.mobrun.plugin.api.HyperHive
import java.util.*

class TaskManager(
    private val hyperHive: HyperHive
) : ITaskManager {

    private var onTaskChanges: ((Task) -> Unit)? = null
    private var task: Task? = null

    override fun getTask(): Task {
        if (task == null) {
            val settings = getTaskSettings(TaskType.TransferWithoutOrder, MovementType.SS)
            task = Task(
                isCreated = false,
                name = "Перемещение от ${DateTimeUtil.formatDate(Date(), Constants.DATE_FORMAT_dd_mm_yyyy_hh_mm)}",
                taskType = TaskType.TransferWithoutOrder,
                movementType = MovementType.SS,
                receiver = "",
                description = "",
                pikingStorage = "",
                shipmentStorage = "",
                shipmentDate = "",
                settings = settings
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
                it.taskType == task?.taskType && it.mvmType == task?.movementType
            }
            .map {
                it.lgortSource
            }
            .distinct()
    }

    override fun getAvailableShipmentStorageList(): List<String> {
        return ZmpUtz47V001(hyperHive).localHelper_ET_TASK_TPS.all
            .filter {
                it.taskType == task?.taskType && it.mvmType == task?.movementType
            }
            .map {
                it.lgortTarget
            }
            .distinct()
    }

    override fun getTaskAnnotation(): String {
        return ZmpUtz47V001(hyperHive).localHelper_ET_TASK_TPS.all
            .first {
                it.taskType == task?.taskType && it.mvmType == task?.movementType
            }
            .annotation
    }

    override fun clear() {
        task = null
    }

    private fun getTaskSettings(taskType: TaskType, movementType: MovementType): TaskSettings {
        val result = ZmpUtz47V001(hyperHive).localHelper_ET_TASK_TPS.all
            .first {
                it.taskType == taskType && it.mvmType == movementType
            }

        val signOfDivision = mutableSetOf<GoodsSignOfDivision>()

        if (result.divMarkParts.isSapTrue()) {
            signOfDivision.add(GoodsSignOfDivision.MARK_PARTS)
        }

        if (result.divAlco.isSapTrue()) {
            signOfDivision.add(GoodsSignOfDivision.ALCO)
        }

        if (result.divUsual.isSapTrue()) {
            signOfDivision.add(GoodsSignOfDivision.USUAL)
        }

        if (result.divVet.isSapTrue()) {
            signOfDivision.add(GoodsSignOfDivision.VET)
        }

        if (result.divParts.isSapTrue()) {
            signOfDivision.add(GoodsSignOfDivision.PARTS)
        }

        if (result.divMtart.isSapTrue()) {
            signOfDivision.add(GoodsSignOfDivision.MTART)
        }

        if (result.divFood.isSapTrue()) {
            signOfDivision.add(GoodsSignOfDivision.FOOD)
        }

        if (result.divLifnr.isSapTrue()) {
            signOfDivision.add(GoodsSignOfDivision.LIF_NUMBER)
        }

        if (result.divMatnr.isSapTrue()) {
            signOfDivision.add(GoodsSignOfDivision.MATERIAL_NUMBER)
        }

        return TaskSettings(
            description = result.annotation,
            shipmentStorage = result.lgortTarget,
            //signsOfDiv = signOfDivision.toSet()
            signsOfDiv = setOf(
                GoodsSignOfDivision.ALCO,
                GoodsSignOfDivision.VET,
                GoodsSignOfDivision.FOOD
            )
        )
    }
}