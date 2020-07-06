package com.lenta.movement.requests.network.models.consolidation

import com.google.gson.annotations.SerializedName
import com.lenta.movement.requests.network.models.RestCargoUnit

data class ConsolidationParams(
        /** IP адрес ТСД */
        @SerializedName("IV_IP")
        val deviceIp: String,

        /** Номер задания */
        @SerializedName("IV_TASK_NUM")
        val taskNumber: String,

        /** Табельный номер */
        @SerializedName("IV_PERNR")
        val personnelNumber: String,

        /** Режим обработки:
         * 01 – Консолидация ЕО в ГЕ
         * 02 – Разъединение ГЕ в ЕО
         * 03 – Докомплектовать ГЕ
         */
        @SerializedName("IV_MODE")
        val mode: String,

        /** Список ЕО */
        @SerializedName("IT_TASK_EXIDV")
        val eoNumberList: List<ConsolidationProcessingUnit>? = listOf(),

        /** Список ГЕ */
        @SerializedName("IT_TASK_EXIDV_TOP")
        val geNumberList: List<RestCargoUnit>? = listOf()
)
