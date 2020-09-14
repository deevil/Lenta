package com.lenta.bp10.requests.network.pojo

import com.google.gson.annotations.SerializedName

data class ExciseStamp(
        /** Номер набора ("" для не набора) */
        @SerializedName("MATNR_OSN")
        val matnrOsn: String = "",
        /** Номер товара */
        @SerializedName("MATNR")
        val matnr: String,
        /** Причина движения */
        @SerializedName("GRUND")
        val writeOffCause: String,
        /** Код акцизной марки */
        @SerializedName("PDF417")
        val stamp: String,
        /** Номер блока */
        @SerializedName("PACK_NUM")
        val packNumber: String = "",
        /** Признак bad mark */
        @SerializedName("REG")
        val reg: String
)