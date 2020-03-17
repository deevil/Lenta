package com.lenta.bp12.model.pojo

import com.google.gson.annotations.SerializedName

data class ProviderItem(
        /** Номер счета */
        @SerializedName("LIFNR")
        val code: String,
        /** Наименование поставщика */
        @SerializedName("LIFNR_NAME")
        val name: String
)