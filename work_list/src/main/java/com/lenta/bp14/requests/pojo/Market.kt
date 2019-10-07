package com.lenta.bp14.requests.pojo

import com.google.gson.annotations.SerializedName

data class Market(
        @SerializedName("WERKS")
        val tkNumber: String,
        @SerializedName("ADDRES")
        val address: String,
        @SerializedName("RETAIL_TYPE")
        val retailType: String
)