package com.lenta.bp15.repository.requests.pojo

import com.google.gson.annotations.SerializedName

data class UnlockTaskParams(
        /** Номер задания */
        @SerializedName("IV_TASK_NUM")
        val taskNumber: String,
        /** Табельный номер */
        @SerializedName("IV_PERNR")
        val userNumber: String,
        /** IP адрес ТСД */
        @SerializedName("IV_IP_PDA")
        val deviceIp: String
)