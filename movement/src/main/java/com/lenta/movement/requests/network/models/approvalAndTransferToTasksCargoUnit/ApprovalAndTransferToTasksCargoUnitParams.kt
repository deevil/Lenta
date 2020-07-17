package com.lenta.movement.requests.network.models.approvalAndTransferToTasksCargoUnit

import com.google.gson.annotations.SerializedName

data class ApprovalAndTransferToTasksCargoUnitParams(
        /** IP адрес ТСД */
        @SerializedName("IV_IP")
        val deviceIp: String,

        /** Номер задания */
        @SerializedName("IV_TASK_NUM")
        val taskNumber: String,

        /** Табельный номер */
        @SerializedName("IV_PERNR")
        val personnelNumber: String
)