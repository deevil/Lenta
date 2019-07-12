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

    fun formatDate(date: Date, targetFormat: String): String {
        return getDateFormat(targetFormat).format(date)
    }

    fun formatDate(unixTime: Long, targetFormat: String): String {
        return formatDate(Date(unixTime), targetFormat)
    }

    fun formatCurrentDate(targetFormat: String): String {
        return getDateFormat(targetFormat).format(Date())
    }

    fun convertTimeString(formatSource: String, formatDestination: String, date: String): String {
        return formatDate(getDateFormat(formatSource).parse(date).time, formatDestination)
    }

    fun getDateFromString(dateString: String, format: String): Date {
        return getDateFormat(format).parse(dateString)
    }

}