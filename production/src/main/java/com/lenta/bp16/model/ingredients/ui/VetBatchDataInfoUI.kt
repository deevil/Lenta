package com.lenta.bp16.model.ingredients.ui

data class VetBatchDataInfoUI(
        /** Номер ЗСЖ */
        val entryId: String,
        /** Наименование производителя */
        val prodName: String,
        /** Код производителя */
        val prodCode: String,
        /** Дата производства */
        val prodDate: String
)