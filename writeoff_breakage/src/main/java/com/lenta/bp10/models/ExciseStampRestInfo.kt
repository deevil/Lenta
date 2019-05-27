package com.lenta.bp10.models

import com.google.gson.annotations.SerializedName

class ExciseStampRestInfo(
        /// Номер набора (null для ненабора)
        @SerializedName("MATNR_OSN")
        val matnrOsn: String,

        /// Номер товара
        @SerializedName("MATNR")
        val matnr: String,

        /// Причина движения
        @SerializedName("GRUND")
        val writeOffCause: String,

        /// Код акцизной марки
        @SerializedName("PDF417")
        val stamp: String,

        /// Признак bad mark
        @SerializedName("REG")
        val reg: String
)