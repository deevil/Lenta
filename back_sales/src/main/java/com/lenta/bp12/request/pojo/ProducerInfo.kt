package com.lenta.bp12.request.pojo

import com.google.gson.annotations.SerializedName

data class ProducerInfo(
        /** Номер счета */
        @SerializedName("ZPROD")
        val code: String? = "",
        /** Наименование поставщика */
        @SerializedName("PROD_NAME")
        val name: String? = ""
)