package com.pocketaps.cakeday.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.pocketaps.cakeday.core.database.entity.PersonEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonDao {

    @Query("SELECT * FROM person WHERE isDeleted = 0")
    fun observeAll(): Flow<List<PersonEntity>>

    @Upsert
    suspend fun upsert(entity: PersonEntity)

    @Query("UPDATE person SET isDeleted = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun softDelete(id: Long, updatedAt: Long)
}
