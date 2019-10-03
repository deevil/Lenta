package com.lenta.bp14.requests.pojo

import com.google.gson.annotations.SerializedName

data class CheckPlace(
        @SerializedName("MATNR")
        val matNr: String,
        // оформленно - 2, не оформленно - 3, есть кол-во - 1
        @SerializedName("STAT_CHECK")
        val statCheck: String
)