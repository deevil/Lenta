package com.lenta.bp14.models.check_price

import com.lenta.bp14.models.ITaskDescription

data class CheckPriceTaskDescription(
        val tkNumber: String,
        override var taskName: String,
        override var comment: String,
        override var description: String
) : ITaskDescription
