package com.lenta.movement.requests.network.models.startConsolidation

import com.google.gson.annotations.SerializedName
import com.lenta.movement.requests.network.models.RestCargoUnit

data class StartConsolidationResult(
        /** Таблица состава задания */
        @SerializedName("ET_TASK_POS")
        val taskComposition: List<StartConsolidationTaskComposition>,

        /** Список ЕО */
        @SerializedName("ET_TASK_EXIDV")
        val eoList: List<StartConsolidationProcessingUnit>,

        /** Список ГЕ */
        @SerializedName("ET_TASK_EXIDV_TOP")
        val geList: List<RestCargoUnit>,

        /** Код возврата */
        @SerializedName("EV_RETCODE")
        val retCode: String,

        /** Текст ошибки */
        @SerializedName("EV_ERROR_TEXT")
        val errorTxt: String
)