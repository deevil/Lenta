package com.lenta.movement.models

import com.google.gson.annotations.SerializedName

enum class MovementType {
    @SerializedName("SS")
    SS,
    @SerializedName("SCDS")
    SCDS,
    @SerializedName("SCS")
    SCS,
    @SerializedName("SCST")
    SCST
}