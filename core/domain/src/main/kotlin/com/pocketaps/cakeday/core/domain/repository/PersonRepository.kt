package com.pocketaps.cakeday.core.domain.repository

import com.pocketaps.cakeday.core.model.Person
import kotlinx.coroutines.flow.Flow

interface PersonRepository {
    fun observeAll(): Flow<List<Person>>
    fun observeUpcoming(withinDays: Int): Flow<List<Person>>
    suspend fun upsert(person: Person)
    suspend fun delete(id: Long)
}
