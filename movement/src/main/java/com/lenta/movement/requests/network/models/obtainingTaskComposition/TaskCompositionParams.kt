package com.lenta.movement.requests.network.models.obtainingTaskComposition

import com.google.gson.annotations.SerializedName

data class TaskCompositionParams(
        /** IP адрес ТСД */
        @SerializedName("IV_IP")
        val deviceIp: String,

        /** Номер задания */
        @SerializedName("IV_TASK_NUM")
        val taskNumber: String,

        /** Режим работы:
         * 1 - получение состава задания
         * 2 - получение состава задания с переблокировкой
         */
        @SerializedName("IV_MODE")
        val mode: Int,

        /** Табельный номер */
        @SerializedName("IV_PERNR")
        val personnelNumber: String,

        /**Необходимость заполнения справочных данных*/
        @SerializedName("IV_MATNR_DATA_FLG")
        val withProductInfo: String
)