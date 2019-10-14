package com.lenta.bp14.requests.pojo

import com.google.gson.annotations.SerializedName

data class Supplier(
        @SerializedName("MATNR")
        val matnr: String,

        @SerializedName("LIFNR")
        val lifnr: String,

        @SerializedName("LIFNR_NAME")
        val lifnrName: String,

        @SerializedName("PERIOD_ACT")
        val periodAct: String
)