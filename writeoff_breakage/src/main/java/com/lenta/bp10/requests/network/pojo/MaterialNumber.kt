package com.lenta.bp10.requests.network.pojo

import com.google.gson.annotations.SerializedName

data class MaterialNumber(
        /** Номер товара */
        @SerializedName("MATNR")
        val matnr: String,
        /** Причина движения */
        @SerializedName("GRUND")
        val writeOffCause: String,
        /** Место возникновения затрат */
        @SerializedName("KOSTL")
        val kostl: String,
        /** Введенное количество */
        @SerializedName("FIRST_QNT")
        val amount: String
)