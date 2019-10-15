package com.lenta.bp14.requests.pojo

import com.google.gson.annotations.SerializedName

data class Stock(
        /** SAP-код товара */
        @SerializedName("MATNR")
        var matnr: String,
        /** Склад */
        @SerializedName("LGORT")
        var lgort: String,
        /** Остаток товара на складе */
        @SerializedName("STOCK")
        var stock: Double
)