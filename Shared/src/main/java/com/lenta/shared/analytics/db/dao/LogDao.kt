package com.lenta.shared.analytics.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.lenta.shared.analytics.db.entity.LogMessage

@Dao
interface LogDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(logMessage: LogMessage)
}
