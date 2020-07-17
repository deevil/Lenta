package com.lenta.movement.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
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
) : Parcelable {

    sealed class Status : Parcelable {

        abstract val text: String?

        @Parcelize
        data class Created(override val text: String? = null): Status()

        @Parcelize
        data class Counted(override val text: String? = null): Status()

        @Parcelize
        data class Published(override val text: String? = null): Status()

        @Parcelize
        data class Unknown(override val text: String): Status()

        @Parcelize
        data class ToConsolidation(override val text: String? = null) : Status()

        @Parcelize
        data class Consolidation(override val text: String? = null) : Status()

        @Parcelize
        data class Consolidated(override val text: String? = null) : Status()

        @Parcelize
        data class ProcessingOnGz(override val text: String? = null) : Status()

        companion object {
            const val CREATED = "Создано"

            const val COUNTED = "Посчитано"
            const val COUNTED_CODE = "10"

            const val PUBLISHED = "Опубликовано"
            const val PUBLISHED_CODE = "04"

            const val UNKNOWN = "Неизвестно"

            const val TO_CONSOLIDATION = "К консолидации"
            const val TO_CONSOLIDATION_CODE = "13"

            const val CONSOLIDATION = "Консолидация"

            const val CONSOLIDATED = "Консолидировано"
            const val CONSOLIDATED_CODE = "19"

            const val PROCESSING_ON_GZ = "Обработка на ГЗ"
            const val PROCESSING_ON_GZ_CODE = "70"
        }
    }
}