package com.lenta.movement.requests.network.models.checkExciseBox

import com.google.gson.annotations.SerializedName

data class CheckExciseBoxRestInfo(
        @SerializedName("EV_STAT")
        val statusCode: String,
        @SerializedName("EV_STAT_TEXT")
        val statText: String,
        @SerializedName("EV_ERROR_TEXT")
        val errorText: String
)