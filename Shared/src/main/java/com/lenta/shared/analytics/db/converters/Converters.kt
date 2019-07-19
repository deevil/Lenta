package com.lenta.shared.analytics.db.converters

import androidx.room.TypeConverter
import com.lenta.shared.analytics.db.entity.InfoLevel
import com.lenta.shared.platform.constants.Constants.TIME_FORMAT_LOGS
import com.lenta.shared.utilities.date_time.DateTimeUtil.formatDate
import com.lenta.shared.utilities.date_time.DateTimeUtil.getDateFromString
import java.util.*

class Converters {

    @TypeConverter
    fun fromTimestamp(value: String?): Date? {
        return if (value == null) null else getDateFromString(value, TIME_FORMAT_LOGS)
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): String? {
        return if (date == null) {
            null
        } else {
            formatDate(date, TIME_FORMAT_LOGS)
        }
    }

    @TypeConverter
    fun fromInfoLevel(value: InfoLevel?): String? {
        return value?.name
    }

    @TypeConverter
    fun toInfoLevel(levelName: String?): InfoLevel? {
        return levelName?.let { InfoLevel.valueOf(it) }
    }

}