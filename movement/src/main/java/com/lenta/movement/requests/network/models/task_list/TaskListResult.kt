package com.lenta.movement.requests.network.models.task_list

import com.google.gson.annotations.SerializedName

data class TaskListResult(
        @SerializedName("ET_TASK_LIST")
        val taskList: List<TaskListTask>,

        @SerializedName("EV_RETCODE")
        var retCode: String,

        @SerializedName("EV_ERROR_TEXT")
        var errorText: String
)


