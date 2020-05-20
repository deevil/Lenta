package com.lenta.movement.models

data class Task(
    val isCreated: Boolean,
    val name: String,
    @Deprecated("use settings")
    val description: String,
    val taskType: TaskType,
    val movementType: MovementType,
    val receiver: String,
    val pikingStorage: String,
    @Deprecated("use settings")
    val shipmentStorage: String,
    val shipmentDate: String,
    val settings: TaskSettings
)