package com.lenta.bp14.requests.pojo

import com.google.gson.annotations.SerializedName

data class CreatedTaskInfo(
        @SerializedName("TASK_NUM")
        val taskNumber: String,
        @SerializedName("TEXT1")
        val text1: String,
        @SerializedName("TEXT2")
        val text2: String
)