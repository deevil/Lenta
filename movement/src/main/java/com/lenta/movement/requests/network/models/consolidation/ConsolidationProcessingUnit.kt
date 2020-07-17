package com.lenta.movement.requests.network.models.consolidation

import com.google.gson.annotations.SerializedName

data class ConsolidationProcessingUnit (
        @SerializedName("EXIDV")
        val eoNumber : String
)