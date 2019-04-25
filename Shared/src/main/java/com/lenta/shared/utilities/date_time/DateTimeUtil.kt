package com.lenta.shared.utilities.date_time

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object DateTimeUtil {
    private val mapOfFormatters = ConcurrentHashMap<String, DateFormat>()

    fun getDateFormat(format: String): DateFormat {
        return mapOfFormatters.getOrPut(format) {
            SimpleDateFormat(format, Locale.ENGLISH)
        }
    }

    fun formatDate(unixTime: Long, targetFormat: String): String {
        return getDateFormat(targetFormat).format(Date(unixTime))
    }

    fun formatCurrentDate(targetFormat: String): String {
        return getDateFormat(targetFormat).format(Date())
    }

}