package com.pocketaps.cakeday.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.pocketaps.cakeday.core.database.dao.PersonDao
import com.pocketaps.cakeday.core.database.entity.PersonEntity

@Database(
    entities = [PersonEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class CakeDayyDatabase : RoomDatabase() {
    abstract fun personDao(): PersonDao
}
