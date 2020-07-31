package com.lenta.movement.requests.network.models.scanInfoNetRequest

import com.google.gson.annotations.SerializedName

data class ScanInfoSet(
        @SerializedName("MATNR")
        val matNr: String?,
        @SerializedName("MATNR_OSN")
        val matNrOsn: String?,
        @SerializedName("MEINS")
        val meins: String?,
        @SerializedName("MENGE")
        val menge: String?
)