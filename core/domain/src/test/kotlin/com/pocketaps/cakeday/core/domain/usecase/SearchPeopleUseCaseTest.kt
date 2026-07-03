package com.pocketaps.cakeday.core.domain.usecase

import app.cash.turbine.test
import com.pocketaps.cakeday.core.model.Person
import com.pocketaps.cakeday.core.testing.fake.FakePersonRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class SearchPeopleUseCaseTest {

    private val today: LocalDate = LocalDate.now()

    private fun person(name: String, note: String? = null, groupId: Long? = null) = Person(
        name = name,
        birthMonth = today.monthValue,
        birthDay = today.dayOfMonth,
        note = note,
        groupId = groupId,
    )

    @Test
    fun `blank query and no group returns everyone`() = runTest {
        val fakeRepo = FakePersonRepository().apply { setAll(listOf(person("Alice"), person("Bob"))) }
        val useCase = SearchPeopleUseCase(GetUpcomingBirthdaysUseCase(fakeRepo))

        useCase(query = "", groupId = null).test {
            assertEquals(2, awaitItem().size)
        }
    }

    @Test
    fun `query matches name case-insensitively`() = runTest {
        val fakeRepo = FakePersonRepository().apply { setAll(listOf(person("Alice"), person("Bob"))) }
        val useCase = SearchPeopleUseCase(GetUpcomingBirthdaysUseCase(fakeRepo))

        useCase(query = "ali", groupId = null).test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals("Alice", result[0].person.name)
        }
    }

    @Test
    fun `query matches note`() = runTest {
        val fakeRepo = FakePersonRepository().apply {
            setAll(listOf(person("Alice", note = "Loves chocolate cake"), person("Bob")))
        }
        val useCase = SearchPeopleUseCase(GetUpcomingBirthdaysUseCase(fakeRepo))

        useCase(query = "chocolate", groupId = null).test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals("Alice", result[0].person.name)
        }
    }

    @Test
    fun `group filter alone restricts to that group`() = runTest {
        val fakeRepo = FakePersonRepository().apply {
            setAll(listOf(person("Alice", groupId = 1L), person("Bob", groupId = 2L)))
        }
        val useCase = SearchPeopleUseCase(GetUpcomingBirthdaysUseCase(fakeRepo))

        useCase(query = "", groupId = 1L).test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals("Alice", result[0].person.name)
        }
    }

    @Test
    fun `search and group filter combine with AND semantics`() = runTest {
        val fakeRepo = FakePersonRepository().apply {
            setAll(
                listOf(
                    person("Alice", groupId = 1L),
                    person("Alicia", groupId = 2L),
                    person("Bob", groupId = 1L),
                ),
            )
        }
        val useCase = SearchPeopleUseCase(GetUpcomingBirthdaysUseCase(fakeRepo))

        useCase(query = "ali", groupId = 1L).test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals("Alice", result[0].person.name)
        }
    }

    @Test
    fun `no match returns an empty list`() = runTest {
        val fakeRepo = FakePersonRepository().apply { setAll(listOf(person("Alice"))) }
        val useCase = SearchPeopleUseCase(GetUpcomingBirthdaysUseCase(fakeRepo))

        useCase(query = "zzz", groupId = null).test {
            assertEquals(0, awaitItem().size)
        }
    }
}
