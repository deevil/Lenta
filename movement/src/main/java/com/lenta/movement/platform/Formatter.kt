package com.lenta.movement.platform

import android.content.Context
import com.lenta.movement.models.MovementType
import com.lenta.movement.models.TaskType

class Formatter(
    val context: Context
) : IFormatter {

    override fun getTaskTypeNameDescription(taskType: TaskType): String {
        return when (taskType) {
            TaskType.TransferWithOrder -> "Трансфер с заказа"
            TaskType.TransferWithoutOrder -> "Трансфер без заказа"
        }
    }

    override fun getMovementTypeNameDescription(movementType: MovementType): String {
        return when (movementType) {
            MovementType.SS -> "Для перемещения на ТК"
            MovementType.SCDS -> "Для перемещения на ТК" // TODO
            MovementType.SCS -> "Для перемещения на ТК" // TODO
            MovementType.SCST -> "Для перемещения на ТК" // TODO
        }
    }

}