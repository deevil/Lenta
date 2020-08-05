package com.lenta.movement.requests.network.models.scanInfoNetRequest

import com.google.gson.annotations.SerializedName

data class ScanInfoEgaisOrg(
        @SerializedName("ZPROD")
        val egaisOrgCode: String?,
        @SerializedName("PROD_NAME")
        val egaisOrgName: String?
)