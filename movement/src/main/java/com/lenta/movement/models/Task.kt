package com.lenta.movement.models

import java.util.*

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
    val shipmentDate: Date
) {

    sealed class Status {
        abstract val text: String?

        data class Created(override val text: String? = null): Status()

        data class Counted(override val text: String? = null): Status()

        data class Published(override val text: String? = null): Status()

        data class Unknown(override val text: String): Status()

        data class ToConsolidation(override val text: String? = null) : Status()

        data class Consolidation(override val text: String? = null) : Status()

        data class Consolidated(override val text: String? = null) : Status()
    }
}