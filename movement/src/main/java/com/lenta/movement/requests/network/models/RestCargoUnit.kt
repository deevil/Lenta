package com.lenta.movement.requests.network.models

import com.google.gson.annotations.SerializedName

/** ГЕ */
data class RestCargoUnit(
        /** Номер ГЕ */
        @SerializedName("EXIDV_TOP")
        val cargoUnitNumber: String,

        /** Номер ЕО */
        @SerializedName("EXIDV")
        val processingUnitNumber: String
)