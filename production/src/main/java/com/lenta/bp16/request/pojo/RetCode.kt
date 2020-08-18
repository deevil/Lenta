package com.lenta.bp16.request.pojo

import com.google.gson.annotations.SerializedName
import com.lenta.shared.utilities.extentions.IRetCode

data class RetCode(
        /** Код возврата */
        @SerializedName("RETCODE")
        override val retCode: Int,
        /** Текст ошибки */
        @SerializedName("ERROR_TEXT")
        override val errorText: String
) : IRetCode