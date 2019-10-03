package com.lenta.bp14.requests.pojo

import com.google.gson.annotations.SerializedName

data class Position(
        @SerializedName("MATNR")
        val matNr: String,
        @SerializedName("XZAEL")
        val isProcessed: String,
        @SerializedName("FACT_QNT")
        val quantity: Double
)

