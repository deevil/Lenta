package com.lenta.movement.utils

import com.lenta.shared.utilities.date_time.DateTimeUtil
import java.util.*


object DateTimeUtils {

    const val YYYY_MM_dd_DATE_PATTERN = "yyyy-MM-dd"
    const val NORMAL_DATE_PATTERN = "dd.MM.yyyy"

    fun formatServerDate(serverDate : String, serverDatePattern : String) : String {
        val dateOfPour: Date = DateTimeUtil.getDateFromString(serverDate, serverDatePattern)
        return DateTimeUtil.formatDate(dateOfPour, NORMAL_DATE_PATTERN)
    }
}
