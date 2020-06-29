package com.lenta.movement.models

import com.google.gson.annotations.SerializedName
import com.lenta.movement.requests.network.StartConsolidation
import com.lenta.movement.requests.network.StartConsolidationResult

//EO item
data class ProcessingUnit(
        /** Номер ЕО */
        val processingUnitNumber: String,
        /** Номер корзины */
        val basketNumber: String,
        /** Поставщик */
        val supplier: String?,
        /** Флаг – «Алкоголь» */
        val isAlco: Boolean,
        /** Флаг – «Обычный товар» */
        val isUsual: Boolean,
        /** Количество позиций */
        val quantity: String) {
}
