package com.lenta.bp14.models.check_price

import com.lenta.bp14.models.ITaskDescription

data class CheckPriceTaskDescription(
        val tkNumber: String,
        val taskName: String
) : ITaskDescription
