package com.lenta.bp10.mobileObjectsService.models

import com.google.gson.annotations.SerializedName

// Модель справочника списка наборов
class MB_S_22(
        /// Номер товара
        @SerializedName("MATNR_OSN")
        val matnr_osn: String,

        /// Компонент спецификации
        @SerializedName("MATNR")
        val matnr: String,

        /// Количество вложенного
        @SerializedName("MENGE")
        val menge: Double,

        /// Базисная единица измерения
        @SerializedName("MEINS")
        val meins: String
)