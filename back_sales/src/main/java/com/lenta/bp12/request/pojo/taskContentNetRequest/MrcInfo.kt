package com.lenta.bp12.request.pojo.taskContentNetRequest

import com.google.gson.annotations.SerializedName

data class MrcInfo(
        @SerializedName("MATNR")
        val material: String?,

        @SerializedName("GROUP_MPR")
        val mprGroup: String?,

        @SerializedName("MPR")
        val maxRetailPrice: String?
)