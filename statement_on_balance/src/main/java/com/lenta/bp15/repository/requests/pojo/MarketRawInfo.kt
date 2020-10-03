package com.lenta.bp15.repository.requests.pojo

import com.google.gson.annotations.SerializedName

data class MarketRawInfo(
        /** Номер магазина */
        @SerializedName("WERKS")
        val tkNumber: String,
        /** Адрес магазина */
        @SerializedName("ADDRES")
        val address: String,
        /** Формат магазина */
        @SerializedName("RETAIL_TYPE")
        val retailType: String,
        /** Версия */
        @SerializedName("VERSION")
        val version: String
)