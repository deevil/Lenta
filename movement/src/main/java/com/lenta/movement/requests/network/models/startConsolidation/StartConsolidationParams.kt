package com.lenta.movement.requests.network.models.startConsolidation

import com.google.gson.annotations.SerializedName

data class StartConsolidationParams(
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

        /** Флаг: необходимость заполнения справочных данных,
         * если SlowData справочник товаров ZMP_UTZ_30_V001 не пришел еще,
         * то сервер пришлёт его сейчас */
        @SerializedName("IV_MATNR_DATA_FLG")
        val withProductInfo: String
)