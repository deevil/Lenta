package com.lenta.bp12.request.pojo

import com.google.gson.annotations.SerializedName

data class ProviderInfo(
        /** Номер счета */
        @SerializedName("LIFNR")
        val code: String? = "",
        /** Наименование поставщика */
        @SerializedName("LIFNR_NAME")
        val name: String? = ""
)