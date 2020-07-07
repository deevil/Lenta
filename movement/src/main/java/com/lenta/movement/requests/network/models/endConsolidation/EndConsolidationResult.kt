package com.lenta.movement.requests.network.models.endConsolidation

import com.google.gson.annotations.SerializedName

data class EndConsolidationResult(

        @SerializedName("EV_RETCODE")
        val retCode : String,

        @SerializedName("EV_ERROR_TEXT")
        val errorTxt : String
)