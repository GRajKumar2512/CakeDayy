package com.pocketaps.cakeday.core.testing.fake

import com.pocketaps.cakeday.core.domain.repository.GroupRepository
import com.pocketaps.cakeday.core.model.Group
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.concurrent.atomic.AtomicLong

class FakeGroupRepository : GroupRepository {

    private val store = MutableStateFlow<List<Group>>(emptyList())
    private val idCounter = AtomicLong(1L)

    override fun observeGroups(): Flow<List<Group>> = store

    override suspend fun upsertGroup(group: Group) {
        val now = System.currentTimeMillis()
        val current = store.value.toMutableList()
        val idx = current.indexOfFirst { it.id == group.id }
        if (idx >= 0) {
            current[idx] = group
        } else {
            val id = if (group.id != 0L) group.id else idCounter.getAndIncrement()
            current += group.copy(
                id = id,
                createdAt = if (group.createdAt == 0L) now else group.createdAt,
            )
        }
        store.value = current
    }

    override suspend fun deleteGroup(id: Long) {
        store.value = store.value.filter { it.id != id }
    }

    fun setAll(groups: List<Group>) {
        store.value = groups
    }
}
