package com.lenta.bp10.mobileObjectsService.models

import com.fasterxml.jackson.annotation.JsonProperty

// Модель справочника списка наборов
class MB_S_22(
        /// Номер товара
        @JsonProperty("MATNR_OSN")
        val matnr_osn: String,

        /// Компонент спецификации
        @JsonProperty("MATNR")
        val matnr: String,

        /// Количество вложенного
        @JsonProperty("MENGE")
        val menge: Double,

        /// Базисная единица измерения
        @JsonProperty("MEINS")
        val meins: String
)