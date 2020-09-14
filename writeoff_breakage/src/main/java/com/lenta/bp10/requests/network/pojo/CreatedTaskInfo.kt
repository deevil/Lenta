package com.lenta.bp10.requests.network.pojo

import com.google.gson.annotations.SerializedName

data class CreatedTaskInfo(
        @SerializedName("TASK_NUM")
        val number: String,
        @SerializedName("TASK_TYPE")
        val taskType: String,
        @SerializedName("DESCR")
        val description: String
)