package com.lenta.bp16.model.ingredients.ui

data class ZPartDataInfoUI (
        /** SAP-код */
        val matnr: String,
        /** Номер партии */
        val batchId: String,
        /** Код производителя */
        val prodCode: String,
        /** Наименование производителя */
        val prodName: String,
        /** Дата производства */
        val prodDate: String
)