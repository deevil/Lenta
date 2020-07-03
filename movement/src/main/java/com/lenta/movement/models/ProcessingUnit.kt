package com.lenta.movement.models

//EO item
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
        val cargoUnitNumber: String? = null
)
