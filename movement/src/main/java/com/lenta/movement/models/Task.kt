package com.lenta.movement.models

data class Task(
    val number: String,
    val isCreated: Boolean,
    val name: String,
    val comment: String,
    val taskType: TaskType,
    val movementType: MovementType,
    val receiver: String,
    val pikingStorage: String,
    val shipmentStorage: String,
    val shipmentDate: String,
    val settings: TaskSettings
)