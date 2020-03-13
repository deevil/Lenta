package com.lenta.bp12.model.pojo

import com.google.gson.annotations.SerializedName

data class ProducerItem(
        /** Номер счета */
        @SerializedName("ZPROD")
        val code: String,
        /** Наименование поставщика */
        @SerializedName("PROD_NAME")
        val name: String
)