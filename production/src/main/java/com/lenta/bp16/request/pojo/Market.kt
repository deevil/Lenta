package com.lenta.bp16.request.pojo

import com.google.gson.annotations.SerializedName

data class Market(
        @SerializedName("WERKS")
        val tkNumber: String,
        @SerializedName("ADDRES")
        val address: String,
        @SerializedName("RETAIL_TYPE")
        val retailType: String,
        /**
         * Версия приложения для обновления через FMP
         */
        @SerializedName("VERSION")
        val version: String
)