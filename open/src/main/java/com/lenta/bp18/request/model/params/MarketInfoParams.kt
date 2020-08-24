package com.lenta.bp18.request.model.params

import com.google.gson.annotations.SerializedName

data class MarketInfoParams(
        @SerializedName("IV_IP")
        val ipAdress: String,
        @SerializedName("IV_MODE")
        val mode: String,
        @SerializedName("IV_WERKS")
        val werks: String? = ""
)