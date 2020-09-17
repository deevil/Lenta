package com.lenta.bp16.model.ingredients.ui

import com.lenta.bp16.model.IDataInfo

data class ZPartDataInfoUI (
        /** SAP-код */
        val matnr: String,
        /** Номер партии */
        val batchId: String,
        /** Код производителя */
        override val prodCode: String,
        /** Наименование производителя */
        override val prodName: String,
        /** Дата производства */
        val prodDate: String
): IDataInfo