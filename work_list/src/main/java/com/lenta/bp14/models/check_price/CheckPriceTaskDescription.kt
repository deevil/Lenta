package com.lenta.bp14.models.check_price

import com.lenta.bp14.models.ITaskDescription
import com.lenta.bp14.requests.check_price.CheckPriceTaskInfoResult

data class CheckPriceTaskDescription(
        override val tkNumber: String,
        override val taskNumber: String,
        override var taskName: String,
        override val comment: String,
        override val description: String,
        override val isStrictList: Boolean,
        val additionalTaskInfo: CheckPriceTaskInfoResult?
) : ITaskDescription
