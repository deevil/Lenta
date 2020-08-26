package com.lenta.bp16.request.pojo

import com.google.gson.annotations.SerializedName

data class SetInfo (
        /**Номер товара*/
        @SerializedName("MATNR_OSN")
        val ean: String?,
        /**Компонент спецификации*/
        @SerializedName("MATNR")
        val material: String?,
        /**Количество вложенного*/
        @SerializedName("MENGE")
        val innerQuantity: String?,
        /**Базисная единица измерения*/
        @SerializedName("MEINS")
        val unitCode: String?
)