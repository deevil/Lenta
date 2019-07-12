package com.lenta.shared.analytics.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.lenta.shared.analytics.db.entity.LogMessage

@Dao
interface LogDao {
    @Insert
    fun insertAll(vararg logMessage: LogMessage)

    @Query("SELECT * FROM logs")
    fun getAll(): List<LogMessage>

}
