package com.lenta.bp14.requests.pojo

import com.google.gson.annotations.SerializedName

data class Place(
        /** SAP-код товара */
        @SerializedName("MATNR")
        var matnr: String,
        /** Код места хранения */
        @SerializedName("PLACE_CODE")
        var placeCode: String
)