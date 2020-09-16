package com.lenta.bp16.model.ingredients.ui

/** Справочник производителей */
data class ProducerDataInfoUI (
        /** Наименование товара */
        val mantr: String,
        /** Код производителя */
        val prodCode: String,
        /** Номер EAN */
        val ean: String,
        /** Наименование пользователя */
        val prodName: String
)