package com.lenta.bp10.mobileObjectsService.models

import com.google.gson.annotations.SerializedName

/// модель справочник единиц измерения
class MB_S_07(
        /// Единица измерения
        @SerializedName("UOM")
        val uom: String,

        /// Внешняя ЕИ - коммерческое представление (3-значная)
        @SerializedName("NAME")
        val name: String,

        /// Число десятичных разрядов при представлении чисел
        @SerializedName("DECAN")
        val decan: Short
)