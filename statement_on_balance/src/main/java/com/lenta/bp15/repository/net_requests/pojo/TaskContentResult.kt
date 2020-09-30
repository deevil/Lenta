package com.lenta.bp15.repository.net_requests.pojo

import com.google.gson.annotations.SerializedName
import com.lenta.shared.utilities.extentions.IResultWithRetCodes

data class TaskContentResult(
        /** Таблица состава задания */
        @SerializedName("ET_TASK_POS")
        val positions: List<PositionRawInfo>?,
        /** Таблица марок задания */
        @SerializedName("ET_TASK_MARK")
        val marks: List<MarkRawInfo>?,
        /** Таблица возврата */
        @SerializedName("ET_RETCODE")
        override val retCodes: List<RetCode>?
) : IResultWithRetCodes