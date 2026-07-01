package com.pocketaps.cakeday.core.data.repository

import com.pocketaps.cakeday.core.common.dispatcher.DispatcherProvider
import com.pocketaps.cakeday.core.database.dao.PersonDao
import com.pocketaps.cakeday.core.database.entity.PersonEntity
import com.pocketaps.cakeday.core.domain.repository.PersonRepository
import com.pocketaps.cakeday.core.domain.util.BirthdayDateUtils
import com.pocketaps.cakeday.core.model.Person
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject

class PersonRepositoryImpl @Inject constructor(
    private val dao: PersonDao,
    private val dispatchers: DispatcherProvider,
) : PersonRepository {

    override fun observeAll(): Flow<List<Person>> =
        dao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override fun observeUpcoming(withinDays: Int): Flow<List<Person>> =
        observeAll().map { people ->
            val today = LocalDate.now()
            people.filter { person ->
                BirthdayDateUtils.daysUntilNextBirthday(person.birthMonth, person.birthDay, today) <= withinDays
            }
        }

    override suspend fun upsert(person: Person): Unit = withContext(dispatchers.io) {
        dao.upsert(person.toEntity(now = System.currentTimeMillis()))
    }

    override suspend fun delete(id: Long): Unit = withContext(dispatchers.io) {
        dao.softDelete(id, updatedAt = System.currentTimeMillis())
    }
}

private fun PersonEntity.toDomain(): Person = Person(
    id = id,
    name = name,
    birthMonth = birthMonth,
    birthDay = birthDay,
    birthYear = birthYear,
    note = note,
    groupId = groupId,
    reminderLeadDaysOverride = reminderLeadDaysOverride,
    createdAt = createdAt,
)

private fun Person.toEntity(now: Long): PersonEntity = PersonEntity(
    id = id,
    name = name,
    birthMonth = birthMonth,
    birthDay = birthDay,
    birthYear = birthYear,
    note = note,
    groupId = groupId,
    reminderLeadDaysOverride = reminderLeadDaysOverride,
    createdAt = if (createdAt == 0L) now else createdAt,
    updatedAt = now,
)
