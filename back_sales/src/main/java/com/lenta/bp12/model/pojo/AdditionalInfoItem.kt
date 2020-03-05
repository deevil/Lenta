package com.lenta.bp12.model.pojo

import com.google.gson.annotations.SerializedName

data class AdditionalInfoItem(
        /** SAP-код товара */
        @SerializedName("MATNR")
        var material: String,
        /** Длинное наименование */
        @SerializedName("NAME")
        var name: String,
        /** Вид товара */
        @SerializedName("MATYPE")
        var type: String,
        /** Базисная единица измерения */
        @SerializedName("BUOM")
        var unitsCode: String,
        /** Тип матрицы SKU */
        @SerializedName("MATR_TYPE")
        var matrix: String,
        /** Номер отдела (секция) */
        @SerializedName("ABTNR")
        var section: String,
        /** Признак – товар акцизный */
        @SerializedName("IS_EXC")
        var isExcise: String,
        /** Товар алкогольный */
        @SerializedName("IS_ALCO")
        var isAlcohol: String,
        /** Группа товаров */
        @SerializedName("MATKL")
        var goodGroup: String,
        /** Группа закупок */
        @SerializedName("EKGRP")
        var purchaseGroup: String,
        /** Единица измерения заказа */
        @SerializedName("BSTME")
        var orderUnitsCode: String
)