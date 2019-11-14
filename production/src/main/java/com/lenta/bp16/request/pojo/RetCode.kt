package com.lenta.bp16.request.pojo

import com.google.gson.annotations.SerializedName

data class RetCode(
        @SerializedName("RETCODE")
        var retCode: Int,

        @SerializedName("ERROR_TEXT")
        var errorText: String
)