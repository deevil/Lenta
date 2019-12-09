package com.lenta.bp16.request.pojo

import com.google.gson.annotations.SerializedName

data class PackCode(
        /** Код тары */
        @SerializedName("CODE_CONT")
        var code: String
)