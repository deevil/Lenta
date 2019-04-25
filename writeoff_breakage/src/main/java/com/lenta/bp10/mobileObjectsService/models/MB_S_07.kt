package com.lenta.bp10.mobileObjectsService.models

import com.fasterxml.jackson.annotation.JsonProperty

/// модель справочник единиц измерения
class MB_S_07(
        /// Единица измерения
        @JsonProperty("UOM")
        val uom: String,

        /// Внешняя ЕИ - коммерческое представление (3-значная)
        @JsonProperty("NAME")
        val name: String,

        /// Число десятичных разрядов при представлении чисел
        @JsonProperty("DECAN")
        val decan: Short
)