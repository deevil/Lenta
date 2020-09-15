package com.lenta.bp16.model.ingredients.ui

data class MercuryPartDataInfoUI (
        /**SAP-код товара*/
        val matnr: String,
        /**Номер ЗСЖ*/
        val entryId: String,
        /**GUID Производителя*/
        val zProd: String,
        /**Наименование производителя*/
        val prodName: String,
        /**Дата производства*/
        val prodDate: String
)