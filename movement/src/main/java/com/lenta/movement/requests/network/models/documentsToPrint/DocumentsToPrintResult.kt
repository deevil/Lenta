package com.lenta.movement.requests.network.models.documentsToPrint

import com.google.gson.annotations.SerializedName

data class DocumentsToPrintResult(

        /** Список документов для печати */
        @SerializedName("ET_DOC_PRINT")
        val docList: List<DocumentsToPrintDocument>?,

        /** Код возврата */
        @SerializedName("EV_RETCODE")
        val retCode: String?,

        /** Текст ошибки */
        @SerializedName("EV_ERROR_TEXT")
        val errorTxt: String?
)