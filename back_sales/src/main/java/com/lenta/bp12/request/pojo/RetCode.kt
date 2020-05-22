package com.lenta.bp12.request.pojo

import com.google.gson.annotations.SerializedName

data class RetCode(
        /** Код возврата */
        @SerializedName("RETCODE")
        var retCode: Int,
        /** Текст ошибки */
        @SerializedName("ERROR_TEXT")
        var errorText: String
)