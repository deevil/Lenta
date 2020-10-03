package com.lenta.bp15.repository.requests.pojo

import com.google.gson.annotations.SerializedName
import com.lenta.shared.utilities.extentions.IResultWithRetCodes

data class UnlockTaskResult(
        /** Таблица возврата */
        @SerializedName("ET_RETCODE")
        override val retCodes: List<RetCode>
) : IResultWithRetCodes