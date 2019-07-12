package com.lenta.shared.analytics.db

import androidx.room.*
import com.lenta.shared.analytics.db.dao.LogDao
import com.lenta.shared.analytics.db.entity.LogMessage


@Database(entities = [LogMessage::class], version = 1)
abstract class RoomAppDatabase : RoomDatabase() {
    abstract fun logDao(): LogDao
}




