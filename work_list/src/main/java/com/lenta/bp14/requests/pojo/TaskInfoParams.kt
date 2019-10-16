package com.lenta.bp14.requests.pojo

import com.google.gson.annotations.SerializedName


data class TaskInfoParams(
        /** Номер задания */
        @SerializedName("IV_TASK_NUM")
        val taskNumber: String,
        /** Режим работы: «1» - получение состава задания, «2» - получение состава задания с переблокировкой */
        @SerializedName("IV_MODE")
        val mode: String,
        /** Необходимость заполнения справочных данных */
        @SerializedName("IV_MATNR_DATA_FLAG")
        val withProductInfo: String,
        /** IP адрес ТСД */
        @SerializedName("IV_IP")
        val ip: String
)