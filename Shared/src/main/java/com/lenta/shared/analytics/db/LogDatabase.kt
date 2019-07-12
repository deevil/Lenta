package com.lenta.shared.analytics.db

import androidx.room.*
import com.lenta.shared.analytics.db.dao.LogDao
import com.lenta.shared.analytics.db.entity.LogMessage


@Database(entities = [LogMessage::class, User::class], version = 1)
abstract class LogDatabase : RoomDatabase() {
    abstract fun logDao(): LogDao
    abstract fun userDao(): UserDao
}


//@Entity(primaryKeys = ["first_name", "last_name"])
@Entity
data class User(
        @PrimaryKey var id: Int,
        @ColumnInfo(name = "first_name") val firstName: String?,
        @ColumnInfo(name = "last_name") val lastName: String?
)

@Dao
interface UserDao {
    @Query("SELECT * FROM user")
    fun getAll(): List<User>


    @Query("SELECT * FROM user WHERE first_name LIKE :first AND " +
            "last_name LIKE :last LIMIT 1")
    fun findByName(first: String, last: String): User

    /*@Insert
    fun insertAll(vararg users: User)

    @Delete
    fun delete(user: User)*/
}


@Database(entities = [User::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}


