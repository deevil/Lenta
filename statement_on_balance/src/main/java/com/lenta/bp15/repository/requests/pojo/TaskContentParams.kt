package com.lenta.bp15.repository.requests.pojo

import com.google.gson.annotations.SerializedName
import com.lenta.shared.utilities.extentions.toSapBooleanString

data class TaskContentParams(
        /** Номер задания */
        @SerializedName("IV_TASK_NUM")
        val taskNumber: String,
        /** IP адрес ТСД */
        @SerializedName("IV_IP")
        val deviceIp: String,
        /** Табельный номер */
        @SerializedName("IV_PERNR")
        val userNumber: String,
        /** Режим работы: 1 - получение состава задания, 2 - получение состава задания с переблокировкой */
        @SerializedName("IV_MODE")
        val mode: Int,
        /** Необходимость заполнения справочных данных */
        @SerializedName("IV_MATNR_DATA_FLG")
        val isNeedAdditionalData: String = false.toSapBooleanString()
)