package com.lenta.bp16.model.ingredients.ui

import com.lenta.bp16.model.IDataInfo

data class MercuryPartDataInfoUI (
        /**SAP-код товара*/
        val matnr: String,
        /**Номер ЗСЖ*/
        val entryId: String,
        /**GUID Производителя*/
        override val prodCode: String,
        /**Наименование производителя*/
        override val prodName: String,
        /**Дата производства*/
        val prodDate: String
): IDataInfo