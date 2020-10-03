package com.lenta.bp15.repository.requests.pojo

import com.google.gson.annotations.SerializedName
import com.lenta.shared.requests.SapResponse

data class SaveDataResult(
        /** Код возврата */
        @SerializedName("EV_RETCODE")
        override val retCode: Int?,
        /** Текст ошибки */
        @SerializedName("EV_ERROR_TEXT")
        override val errorText: String?
) : SapResponse