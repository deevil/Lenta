package com.lenta.movement.requests.network.models.approvalAndTransferToTasksCargoUnit

import com.google.gson.annotations.SerializedName

data class ApprovalAndTransferToTasksCargoUnitResult(
        /** Таблица состава задания */
        @SerializedName("ET_TASK_LIST")
        val taskList: List<ApprovalResultTask>,

        /** Код возврата */
        @SerializedName("EV_RETCODE")
        val retCode: String,

        /** Текст ошибки */
        @SerializedName("EV_ERROR_TEXT")
        val errorTxt: String
)