package com.lenta.bp14.models.data.pojo

import com.lenta.shared.utilities.extentions.getFormattedDate
import com.lenta.shared.utilities.extentions.getFormattedTime
import java.util.*

data class SalesStatistics(
        val lastSaleDate: Date,
        val daySales: Int,
        val weekSales: Int
) {

    fun getFormattedDate(): String {
        return lastSaleDate.getFormattedDate()
    }

    fun getFormattedTime(): String {
        return lastSaleDate.getFormattedTime()
    }

}