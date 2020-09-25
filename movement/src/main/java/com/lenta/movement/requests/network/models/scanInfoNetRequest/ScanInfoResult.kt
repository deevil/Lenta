package com.lenta.movement.requests.network.models.scanInfoNetRequest

import com.google.gson.annotations.SerializedName
import com.lenta.shared.models.core.Supplier
import com.lenta.shared.requests.SapResponse

data class ScanInfoResult(
        @SerializedName("ES_EAN")
        val ean: ScanInfoEan?,
        @SerializedName("ES_MATERIAL")
        val material: ScanInfoMaterial?,
        @SerializedName("ET_LIFNR")
        val suppliers: List<Supplier>?,
        @SerializedName("ET_SET")
        val set: List<ScanInfoSet>?,
        @SerializedName("ET_PROD")
        val egaisOrg: List<ScanInfoEgaisOrg>?,
        @SerializedName("EV_ERROR_TEXT")
        override val errorText: String?,
        @SerializedName("EV_RETCODE")
        override val retCode: Int?
): SapResponse