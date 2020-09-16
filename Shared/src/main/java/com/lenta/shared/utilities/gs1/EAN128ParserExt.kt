package com.lenta.shared.utilities.gs1

import com.lenta.shared.utilities.gs1.ElementStrings.SequenceReader
import java.util.*

fun Map<EAN128Parser.AII, String>.getString(identifier: ApplicationIdentifier): String {
    return this.entries.find { it.key.AI == identifier.key }?.value.orEmpty()
}

fun Map<EAN128Parser.AII, String>.getLong(identifier: ApplicationIdentifier): Long? {
    return try {
        getString(identifier).toLong()
    } catch (e: Exception) {
        null
    }
}

fun Map<EAN128Parser.AII, String>.getDouble(identifier: ApplicationIdentifier): Double? {
    val value = getString(identifier)
    return try {
        value.toDouble()
    } catch (e: Exception) {
        null
    }
}

fun Map<EAN128Parser.AII, String>.getDate(identifier: ApplicationIdentifier): Date? {
    val value = getString(identifier)
    return try {
        SequenceReader.parseDateAndTime(value)
    } catch (e: Exception) {
        try {
            parseDateAndTime(value)
        } catch (e: Exception) {
            null
        }
    }
}

private fun parseDateAndTime(s: String): Date? {
    var year: Int
    val month: Int
    var day: Int
    val hour: Int
    val minutes: Int
    val seconds: Int
    try {
        year = s.substring(0, 4).toInt()
        month = s.substring(4, 6).toInt()
        day = s.substring(6, 8).toInt()
        hour = if (s.length >= 8) s.substring(8, 10).toInt() else 0
        minutes = if (s.length >= 12) s.substring(10, 12).toInt() else 0
        seconds = if (s.length >= 14) s.substring(12, 14).toInt() else 0
    } catch (e: NumberFormatException) {
        throw IllegalArgumentException("data field must be numeric")
    }
    return try {
        val calendar = Calendar.getInstance()
        year = SequenceReader.resolveTwoDigitYear(year, calendar[Calendar.YEAR])
        // When day is zero that means last day of the month
        val lastOfMonth = day == 0
        day = if (day == 0) 1 else day
        calendar.clear()
        calendar.isLenient = false
        calendar[year, month - 1, day, hour, minutes] = seconds
        if (lastOfMonth) {
            calendar.add(Calendar.MONTH, 1)
            calendar.add(Calendar.DAY_OF_MONTH, -1)
        }
        calendar.time
    } catch (e: IllegalArgumentException) {
        throw IllegalArgumentException("invalid date")
    }
}