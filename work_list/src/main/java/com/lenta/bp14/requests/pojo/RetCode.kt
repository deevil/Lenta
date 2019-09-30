package com.lenta.bp14.requests.pojo

import com.google.gson.annotations.SerializedName

data class RetCode(
        @SerializedName("RETCODE")
        var retCode: Int,

        @SerializedName("ERROR_TEXT")
        var errorText: String
)