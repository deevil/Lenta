package com.lenta.movement.requests.network.models.saveTask

import com.google.gson.annotations.SerializedName

data class SaveTaskResult(
        @SerializedName("ET_TASK_LIST")
        val tasks: List<SaveTaskResultTask>?,
        @SerializedName("EV_ERROR_TEXT")
        val errorTxt: String?,
        @SerializedName("EV_RETCODE")
        val retCode: String?
)