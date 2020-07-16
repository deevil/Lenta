package com.lenta.movement.requests.network.models.documentsToPrint

import com.google.gson.annotations.SerializedName

data class DocumentsToPrintParams(
        /** Номер задания */
        @SerializedName("IV_TASK_NUM")
        val taskNumber: String
)