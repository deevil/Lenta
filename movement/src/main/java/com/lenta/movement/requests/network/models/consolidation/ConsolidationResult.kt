package com.lenta.movement.requests.network.models.consolidation

import com.google.gson.annotations.SerializedName
import com.lenta.movement.requests.network.models.RestCargoUnit

data class ConsolidationResult(

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