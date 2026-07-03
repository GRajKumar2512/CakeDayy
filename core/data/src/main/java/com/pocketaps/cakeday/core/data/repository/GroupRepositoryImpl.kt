package com.pocketaps.cakeday.core.data.repository

import com.pocketaps.cakeday.core.common.dispatcher.DispatcherProvider
import com.pocketaps.cakeday.core.database.CakeDayyDatabase
import com.pocketaps.cakeday.core.database.entity.GroupEntity
import com.pocketaps.cakeday.core.domain.repository.GroupRepository
import com.pocketaps.cakeday.core.model.Group
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GroupRepositoryImpl @Inject constructor(
    private val database: CakeDayyDatabase,
    private val dispatchers: DispatcherProvider,
) : GroupRepository {

    override fun observeGroups(): Flow<List<Group>> =
        database.groupDao().observeAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun upsertGroup(group: Group): Unit = withContext(dispatchers.io) {
        database.groupDao().upsert(group.toEntity(now = System.currentTimeMillis()))
    }

    override suspend fun deleteGroup(id: Long): Unit = withContext(dispatchers.io) {
        database.deleteGroupAndUnassignPeople(id, updatedAt = System.currentTimeMillis())
    }
}

private fun GroupEntity.toDomain(): Group = Group(
    id = id,
    name = name,
    colorHex = colorHex,
    createdAt = createdAt,
)

private fun Group.toEntity(now: Long): GroupEntity = GroupEntity(
    id = id,
    name = name,
    colorHex = colorHex,
    createdAt = if (createdAt == 0L) now else createdAt,
    updatedAt = now,
)
