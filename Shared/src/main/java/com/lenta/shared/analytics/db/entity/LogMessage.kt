package com.lenta.shared.analytics.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.TypeConverters
import com.lenta.shared.analytics.db.converters.Converters
import java.util.*

@Entity(tableName = "_logs", primaryKeys = ["time", "message"])
@TypeConverters(Converters::class)
data class LogMessage(
        @ColumnInfo(name = "time") val time: Date,
        @ColumnInfo(name = "infoLevel") val infoLevel: InfoLevel,
        @ColumnInfo(name = "message") val message: String
)

enum class InfoLevel {
    INFO,
    ERROR,
    FATAL
}


