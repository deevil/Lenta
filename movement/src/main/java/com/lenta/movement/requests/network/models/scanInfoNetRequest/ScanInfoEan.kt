package com.lenta.movement.requests.network.models.scanInfoNetRequest

import com.google.gson.annotations.SerializedName

data class ScanInfoEan(
        @SerializedName("EAN")
        val ean: String?,
        @SerializedName("MATERIAL")
        val materialNumber: String?,
        @SerializedName("UMREN")
        val umren: Int?,
        @SerializedName("UMREZ")
        val umrez: Int?,
        @SerializedName("UOM")
        val uom: String?
)