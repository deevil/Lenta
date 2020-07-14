package com.lenta.movement.requests.network.models.printDocuments

import com.google.gson.annotations.SerializedName
import com.lenta.movement.requests.network.models.documentsToPrint.DocumentsToPrintDocument

data class PrintDocumentsParams(
        /** Номер задания */
        @SerializedName("IV_TASK_NUM")
        val taskNum: String,

        /** Документы для печати */
        @SerializedName("IT_DOC_PRINT")
        val docList: List<DocumentsToPrintDocument>,

        /** Имя принтера */
        @SerializedName("IV_PRINTERNAME")
        val printerName: String
)