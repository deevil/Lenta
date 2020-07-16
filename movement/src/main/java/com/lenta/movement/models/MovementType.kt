package com.lenta.movement.models

import com.google.gson.annotations.SerializedName

enum class MovementType(val propertyName: String) {
    @SerializedName("SS")
    SS("SS"),
    @SerializedName("SCDS")
    SCDS("SCDS"),
    @SerializedName("SCS")
    SCS("SCS"),
    @SerializedName("SCST")
    SCST("SCST")
}