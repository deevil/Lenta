package com.lenta.bp14.models.check_price

import com.lenta.bp14.models.ITaskDescription

data class CheckPriceTaskDescription(
        override val tkNumber: String,
        override val taskNumber: String,
        override var taskName: String,
        override val comment: String,
        override val description: String
) : ITaskDescription
