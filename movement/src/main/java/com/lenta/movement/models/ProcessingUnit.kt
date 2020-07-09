package com.lenta.movement.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

//EO item
@Parcelize
data class ProcessingUnit(
        /** Номер ЕО */
        val processingUnitNumber: String,
        /** Номер корзины */
        val basketNumber: String? = null,
        /** Поставщик */
        val supplier: String? = null,
        /** Флаг – «Алкоголь» */
        val isAlco: Boolean? = null,
        /** Флаг – «Обычный товар» */
        val isUsual: Boolean? = null,
        /** Количество позиций */
        val quantity: String? = null,
        /** номер ГЕ */
        var cargoUnitNumber: String? = null,
        /** состояние ЕО:
         * 1 - не обработана
         * 2 - ЕО верхнего уровня
         * 3 - Объединена в ГЕ */
        var state : State = State.NOT_PROCESSED

) : Parcelable {
        enum class State {
                NOT_PROCESSED,
                TOP_LEVEL_EO,
                COMBINED
        }
}
