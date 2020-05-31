package com.lenta.movement.models

data class Task(
    val number: String,
    val isCreated: Boolean,
    val currentStatus: Status,
    val nextStatus: Status,
    val name: String,
    val comment: String,
    val taskType: TaskType,
    val movementType: MovementType,
    val receiver: String,
    val pikingStorage: String,
    val shipmentStorage: String,
    val shipmentDate: String,
    val settings: TaskSettings
) {

    sealed class Status {
        abstract val text: String?

        class Created(override val text: String? = null): Status()

        class Counted(override val text: String? = null): Status()

        class Unknown(override val text: String): Status()
    }
}