package com.lenta.bp10.mobileObjectsService.models

import com.fasterxml.jackson.annotation.JsonProperty

// Модель справочника товаров
class MB_S_30(
        /// Номер товара
        @JsonProperty("MATERIAL")
        val material: String,

        /// Длинное наименование
        @JsonProperty("NAME")
        val name: String,

        /// Вид товара
        @JsonProperty("MATYPE")
        val matype: String,

        /// Базисная единица измерения
        @JsonProperty("BUOM")
        val buom: String,

        @JsonProperty("MATR_TYPE")
        val matrType: String,

        /// Номер секции товара
        @JsonProperty("ABTNR")
        val abtnr: String,

        /// Признак возвратности товара
        @JsonProperty("IS_RETURN")
        val isReturn: String,

        /// Флаг "Акцизный товар"
        @JsonProperty("IS_EXC")
        val exc: String,

        /// Флаг "Алкоголь"
        @JsonProperty("IS_ALCO")
        val alco: String,

        @JsonProperty("MATKL")
        val matkl: String?,

        @JsonProperty("EKGRP")
        val ekgrp: String?
) {
    fun isAlco(): Boolean {
        return alco == "X"
    }

    fun IsExcise(): Boolean {
        return exc == "X"
    }
}