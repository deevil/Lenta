package com.lenta.bp12.request.pojo

import com.google.gson.annotations.SerializedName

data class TaskProducerInfo(
        /** SAP-код товара */
        @SerializedName("MATNR")
        var material: String = "",
        /** Код производителя товара */
        @SerializedName("ZPROD")
        var code: String = "",
        /** Имя производителя товара */
        @SerializedName("PROD_NAME")
        var name: String = ""
)