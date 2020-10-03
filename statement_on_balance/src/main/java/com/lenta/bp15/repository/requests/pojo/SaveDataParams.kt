package com.lenta.bp15.repository.requests.pojo

import com.google.gson.annotations.SerializedName

data class SaveDataParams(
        /** Номер задания */
        @SerializedName("IV_TASK_NUM")
        val taskNumber: String,
        /** IP адрес ТСД */
        @SerializedName("IV_IP")
        val deviceIp: String,
        /** Табельный номер */
        @SerializedName("IV_PERNR")
        val userNumber: String,
        /** Обработка задания завершена */
        @SerializedName("IV_FINISH")
        val isFinish: String,
        /** Таблица марок задания */
        @SerializedName("ET_TASK_MARK")
        val marks: List<MarkRawInfo>
)