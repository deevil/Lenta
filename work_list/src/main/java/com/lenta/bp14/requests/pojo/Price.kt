package com.lenta.bp14.requests.pojo

import com.google.gson.annotations.SerializedName

data class Price(
        @SerializedName("MATNR")
        val matnr: String,

        @SerializedName("PRICE1")
        val price1: Double,

        @SerializedName("PRICE2")
        val price2: Double,

        @SerializedName("PRICE3")
        val price3: Double,

        @SerializedName("PRICE4")
        val price4: Double,

        @SerializedName("START_PROMO")
        val startPromo: String,

        @SerializedName("END_PROMO")
        val endPromo: String


)