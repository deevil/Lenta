package com.lenta.movement.requests.network.models.saveTask

import com.google.gson.annotations.SerializedName

data class SaveTaskResult(
        @SerializedName("ET_TASK_LIST")
        val tasks: List<SaveTaskResultTask>
)