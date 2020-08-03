package com.lenta.movement.requests.network.models.scanInfoNetRequest

import com.google.gson.annotations.SerializedName

data class ScanInfoParams(
        @SerializedName("IV_EAN")
        val ean: String,
        @SerializedName("IV_WERKS")
        val tk: String,
        @SerializedName("IV_MATNR")
        val matNr: String,
        @SerializedName("IV_CODEBP")
        val codeEBP: String,
        @SerializedName("IV_MODE")
        val mode: String = ONE_POSITION_INDICATOR
) {
    companion object {
        private const val ONE_POSITION_INDICATOR = "1"
    }
}