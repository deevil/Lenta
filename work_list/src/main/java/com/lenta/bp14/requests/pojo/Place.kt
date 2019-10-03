package com.lenta.bp14.requests.pojo

import com.google.gson.annotations.SerializedName

data class Place(
        @SerializedName("MATNR")
        var matnr: String,
        @SerializedName("PLACE_CODE")
        var placeCode: String
)