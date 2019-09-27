package com.lenta.bp14.requests.pojo

import com.google.gson.annotations.SerializedName

data class ReportSentResult(
        @SerializedName("ET_RETCODE")
        val retCodes: List<RetCode>,
        @SerializedName("ET_TASK_LIST")
        val createdTasks: List<CreatedTaskInfo>
)