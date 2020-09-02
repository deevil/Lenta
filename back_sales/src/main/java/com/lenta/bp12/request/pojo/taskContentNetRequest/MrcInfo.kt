package com.lenta.bp12.request.pojo.taskContentNetRequest

import com.google.gson.annotations.SerializedName
import com.lenta.shared.utilities.extentions.dropZeros

data class MrcInfo(
        @SerializedName("MATNR")
        val material: String?,

        @SerializedName("GROUP_MPR")
        val mprGroup: String?,

        @SerializedName("MPR")
        val maxRetailPrice: String?
)

data class Mrc(
        val material: String,
        val mprGroup: String,
        val maxRetailPrice: String
)

fun MrcInfo.toMrc(): Mrc {
    return Mrc(
            material = material.orEmpty(),
            mprGroup = mprGroup.orEmpty(),
            maxRetailPrice = maxRetailPrice?.toDoubleOrNull().dropZeros()
    )
}

fun List<MrcInfo>.toMrcList(): List<Mrc> {
        return this.map {
                it.toMrc()
        }
}