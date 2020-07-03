package com.lenta.movement.requests.network.models.consolidation

import com.google.gson.annotations.SerializedName

data class ConsolidationResult(

        /** Список ГЕ */
        @SerializedName("ET_TASK_EXIDV_TOP")
        val geList: List<ConsolidationCargoUnit>,

        /** Код возврата */
        @SerializedName("EV_RETCODE")
        val retCode: String,

        /** Текст ошибки */
        @SerializedName("EV_ERROR_TEXT")
        val errorTxt: String
)