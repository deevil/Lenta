package com.lenta.bp10.mobileObjectsService.models

import com.google.gson.annotations.SerializedName

// Модель справочника товаров
class MB_S_30(
        /// Номер товара
        @SerializedName("MATERIAL")
        val material: String,

        /// Длинное наименование
        @SerializedName("NAME")
        val name: String,

        /// Вид товара
        @SerializedName("MATYPE")
        val matype: String,

        /// Базисная единица измерения
        @SerializedName("BUOM")
        val buom: String,

        @SerializedName("MATR_TYPE")
        val matrType: String,

        /// Номер секции товара
        @SerializedName("ABTNR")
        val abtnr: String,

        /// Признак возвратности товара
        @SerializedName("IS_RETURN")
        val isReturn: String,

        /// Флаг "Акцизный товар"
        @SerializedName("IS_EXC")
        val exc: String,

        /// Флаг "Алкоголь"
        @SerializedName("IS_ALCO")
        val alco: String,

        @SerializedName("MATKL")
        val matkl: String?,

        @SerializedName("EKGRP")
        val ekgrp: String?
) {
    fun isAlco(): Boolean {
        return alco == "X"
    }

    fun IsExcise(): Boolean {
        return exc == "X"
    }
}