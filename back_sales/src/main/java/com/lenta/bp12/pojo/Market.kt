package com.lenta.bp12.pojo

import com.google.gson.annotations.SerializedName

data class Market(
        /** Номер магазина */
        @SerializedName("WERKS")
        val tkNumber: String,
        /** Адрес магазина */
        @SerializedName("ADDRES")
        val address: String,
        /** Тип магазина */
        @SerializedName("RETAIL_TYPE")
        val retailType: String
)