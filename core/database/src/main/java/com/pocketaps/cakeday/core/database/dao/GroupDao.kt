package com.pocketaps.cakeday.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.pocketaps.cakeday.core.database.entity.GroupEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupDao {

    @Query("SELECT * FROM groups WHERE isDeleted = 0 ORDER BY name")
    fun observeAll(): Flow<List<GroupEntity>>

    @Upsert
    suspend fun upsert(entity: GroupEntity)

    @Query("UPDATE groups SET isDeleted = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun softDelete(id: Long, updatedAt: Long)
}
