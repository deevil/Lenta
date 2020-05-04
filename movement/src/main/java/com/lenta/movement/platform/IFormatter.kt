package com.lenta.movement.platform

import com.lenta.movement.models.MovementType
import com.lenta.movement.models.TaskType

interface IFormatter {

    fun getTaskTypeNameDescription(taskType: TaskType): String

    fun getMovementTypeNameDescription(movementType: MovementType): String

}