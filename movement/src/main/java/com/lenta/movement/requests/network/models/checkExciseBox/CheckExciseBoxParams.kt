package com.lenta.movement.requests.network.models.checkExciseBox

import com.google.gson.annotations.SerializedName

data class CheckExciseBoxParams(
        @SerializedName("IV_WERKS")
        val tk: String,
        @SerializedName("IV_MATNR")
        val materialNumber: String,
        @SerializedName("IV_BOX_NUM")
        val boxCode: String
)