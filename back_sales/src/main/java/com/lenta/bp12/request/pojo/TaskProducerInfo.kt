package com.lenta.bp12.request.pojo

import com.google.gson.annotations.SerializedName

data class TaskProducerInfo(
        /** SAP-код товара */
        @SerializedName("MATNR")
        var material: String,
        /** Производитель товара */
        @SerializedName("ZPROD")
        var producer: String
)