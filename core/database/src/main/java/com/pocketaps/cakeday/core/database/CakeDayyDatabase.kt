package com.pocketaps.cakeday.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.withTransaction
import com.pocketaps.cakeday.core.database.dao.GroupDao
import com.pocketaps.cakeday.core.database.dao.PersonDao
import com.pocketaps.cakeday.core.database.entity.GroupEntity
import com.pocketaps.cakeday.core.database.entity.PersonEntity

@Database(
    entities = [PersonEntity::class, GroupEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class CakeDayyDatabase : RoomDatabase() {
    abstract fun personDao(): PersonDao
    abstract fun groupDao(): GroupDao

    // A group's soft-delete and clearing its people's groupId must happen atomically,
    // otherwise a crash between the two calls could leave people pointing at a deleted group.
    suspend fun deleteGroupAndUnassignPeople(groupId: Long, updatedAt: Long) = withTransaction {
        groupDao().softDelete(groupId, updatedAt)
        personDao().clearGroupAssignments(groupId, updatedAt)
    }
}
