package com.lenta.bp10.requests.network.pojo

import com.google.gson.annotations.SerializedName

data class MaterialNumber(
        // <summary>
        // Номер товара
        // </summary>
        @SerializedName("MATNR")
        val matnr: String,

        // <summary>
        // Причина движения
        // </summary>
        @SerializedName("GRUND")
        val writeOffCause: String,

        // <summary>
        // Место возникновения затрат
        // </summary>
        @SerializedName("KOSTL")
        val kostl: String,

        // <summary>
        // Введенное количество
        // </summary>
        @SerializedName("FIRST_QNT")
        val amount: String

)