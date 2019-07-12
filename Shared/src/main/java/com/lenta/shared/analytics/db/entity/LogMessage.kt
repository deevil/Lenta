package com.lenta.shared.analytics.db.entity

import androidx.room.*
import java.util.*

@Entity(tableName = "logs")
@TypeConverters(Converters::class)
data class LogMessage(
        @PrimaryKey @ColumnInfo(name = "time") val time: String,
        @ColumnInfo(name = "infoLevel") val infoLevel: InfoLevel,
        @ColumnInfo(name = "message") val message: String
        )

enum class InfoLevel {
    INFO,
    ERROR,
    FATAL
}

class Converters {

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return if (value == null) null else Date(value)
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
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
