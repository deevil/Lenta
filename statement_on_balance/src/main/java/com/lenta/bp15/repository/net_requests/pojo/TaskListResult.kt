package com.lenta.bp15.repository.net_requests.pojo

import com.google.gson.annotations.SerializedName
import com.lenta.shared.utilities.extentions.IResultWithRetCodes

data class TaskListResult(
        /** Список объектов */
        @SerializedName("ET_TASK_LIST")
        val tasks: List<TaskRawInfo>,
        /** Таблица возврата */
        @SerializedName("ET_RETCODE")
        override val retCodes: List<RetCode>
) : IResultWithRetCodes