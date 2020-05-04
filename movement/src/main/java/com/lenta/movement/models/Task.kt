package com.lenta.movement.models

data class Task(
    val isCreated: Boolean,
    val name: String,
    val description: String,
    val taskType: TaskType,
    val movementType: MovementType,
    val receiver: String,
    val pikingStorage: String,
    val shipmentStorage: String,
    val shipmentDate: String
)