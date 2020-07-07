package com.lenta.movement.requests.network.models.endConsolidation

import com.google.gson.annotations.SerializedName

data class EndConsolidationParams(
        /** IP адрес ТСД */
        @SerializedName("IV_IP")
        val deviceIp: String,

        /** Номер задания */
        @SerializedName("IV_TASK_NUM")
        val taskNumber: String,

        /** Табельный номер */
        @SerializedName("IV_PERNR")
        val personnelNumber: String,

        /** Список созданных заданий */
        @SerializedName("ET_TASK_LIST")
        val taskList: List<EndConsolidationTask>
)