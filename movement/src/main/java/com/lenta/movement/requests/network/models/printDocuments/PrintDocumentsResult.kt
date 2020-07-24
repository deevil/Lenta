package com.lenta.movement.requests.network.models.printDocuments

import com.google.gson.annotations.SerializedName

data class PrintDocumentsResult(
        /** Код возврата */
        @SerializedName("EV_RETCODE")
        val retCode: String?,

        /** Текст ошибки */
        @SerializedName("EV_ERROR_TEXT")
        val errorTxt: String?
)