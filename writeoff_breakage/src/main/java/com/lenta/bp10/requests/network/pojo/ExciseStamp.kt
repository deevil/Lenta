package com.lenta.bp10.requests.network.pojo

import com.google.gson.annotations.SerializedName

// Акцизная марка, элемент списка IT_MARKS в ZMP_UTZ_WOB_04_V001
// </summary>
data class ExciseStamp(
        // <summary>
        // Номер набора ("" для ненабора)
        // </summary>
        @SerializedName("MATNR_OSN")
        val matnrOsn: String,

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
        // Код акцизной марки
        // </summary>
        @SerializedName("PDF417")
        val stamp: String,

        // <summary>
        // Признак bad mark
        // </summary>
        @SerializedName("REG")
        val reg: String

)