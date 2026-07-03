package com.pocketaps.cakeday.core.domain.repository

import com.pocketaps.cakeday.core.model.Group
import kotlinx.coroutines.flow.Flow

interface GroupRepository {
    fun observeGroups(): Flow<List<Group>>
    suspend fun upsertGroup(group: Group)
    suspend fun deleteGroup(id: Long)
}
