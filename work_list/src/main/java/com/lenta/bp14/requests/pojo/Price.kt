package com.lenta.bp14.requests.pojo

import com.google.gson.annotations.SerializedName

data class Price(
        @SerializedName("MATNR")
        var matnr: String,

        @SerializedName("PRICE1")
        var price1: String,

        @SerializedName("PRICE2")
        var price2: String,

        @SerializedName("PRICE3")
        var price3: String,

        @SerializedName("PRICE4")
        var price4: String


)