package com.lenta.bp14.requests.pojo

import com.google.gson.annotations.SerializedName

data class Stock(

        @SerializedName("MATNR")
        var matnr: String,

        @SerializedName("LGORT")
        var lgort: String,

        @SerializedName("STOCK")
        var stock: Double
)