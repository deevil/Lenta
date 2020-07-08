package com.lenta.movement.requests.network.models.endConsolidation

import com.google.gson.annotations.SerializedName

data class EndConsolidationResult(

        /** Список созданных заданий */
        @SerializedName("ET_TASK_LIST")
        val taskList: List<EndConsolidationTask>,

        @SerializedName("EV_RETCODE")
        val retCode : String,

        @SerializedName("EV_ERROR_TEXT")
        val errorTxt : String
)