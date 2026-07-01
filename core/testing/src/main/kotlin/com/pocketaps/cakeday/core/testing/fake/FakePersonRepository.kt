package com.pocketaps.cakeday.core.testing.fake

import com.pocketaps.cakeday.core.domain.repository.PersonRepository
import com.pocketaps.cakeday.core.domain.util.BirthdayDateUtils
import com.pocketaps.cakeday.core.model.Person
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.util.concurrent.atomic.AtomicLong

class FakePersonRepository : PersonRepository {

    private val store = MutableStateFlow<List<Person>>(emptyList())
    private val idCounter = AtomicLong(1L)

    override fun observeAll(): Flow<List<Person>> = store

    override fun observeUpcoming(withinDays: Int): Flow<List<Person>> = store.map { people ->
        val today = LocalDate.now()
        people.filter { person ->
            BirthdayDateUtils.daysUntilNextBirthday(person.birthMonth, person.birthDay, today) <= withinDays
        }
    }

    override suspend fun upsert(person: Person) {
        val now = System.currentTimeMillis()
        val current = store.value.toMutableList()
        val idx = current.indexOfFirst { it.id == person.id }
        if (idx >= 0) {
            current[idx] = person.copy(createdAt = current[idx].createdAt)
        } else {
            val id = if (person.id != 0L) person.id else idCounter.getAndIncrement()
            current += person.copy(
                id = id,
                createdAt = if (person.createdAt == 0L) now else person.createdAt,
            )
        }
        store.value = current
    }

    override suspend fun delete(id: Long) {
        store.value = store.value.filter { it.id != id }
    }

    fun setAll(people: List<Person>) {
        store.value = people
    }
}
