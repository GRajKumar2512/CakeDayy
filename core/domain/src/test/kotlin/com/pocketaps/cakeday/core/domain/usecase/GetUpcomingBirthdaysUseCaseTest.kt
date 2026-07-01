package com.pocketaps.cakeday.core.domain.usecase

import app.cash.turbine.test
import com.pocketaps.cakeday.core.model.Person
import com.pocketaps.cakeday.core.testing.fake.FakePersonRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class GetUpcomingBirthdaysUseCaseTest {

    private lateinit var fakeRepo: FakePersonRepository
    private lateinit var useCase: GetUpcomingBirthdaysUseCase

    @Before
    fun setUp() {
        fakeRepo = FakePersonRepository()
        useCase = GetUpcomingBirthdaysUseCase(fakeRepo)
    }

    @Test
    fun `empty repo emits empty list`() = runTest {
        useCase().test {
            assertEquals(emptyList<Any>(), awaitItem())
            cancel()
        }
    }

    @Test
    fun `person with birthday today has daysUntil 0 and correct age`() = runTest {
        val today = LocalDate.now()
        fakeRepo.setAll(
            listOf(
                Person(
                    id = 1L,
                    name = "Alice",
                    birthMonth = today.monthValue,
                    birthDay = today.dayOfMonth,
                    birthYear = today.year - 30,
                ),
            ),
        )
        useCase().test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals(0, result[0].daysUntilNext)
            assertEquals(30, result[0].nextAge)
            cancel()
        }
    }

    @Test
    fun `person with unknown birth year has null nextAge`() = runTest {
        val today = LocalDate.now()
        fakeRepo.setAll(
            listOf(
                Person(
                    id = 1L,
                    name = "Bob",
                    birthMonth = today.monthValue,
                    birthDay = today.dayOfMonth,
                    birthYear = null,
                ),
            ),
        )
        useCase().test {
            val result = awaitItem()
            assertNull(result[0].nextAge)
            cancel()
        }
    }

    @Test
    fun `people are sorted ascending by daysUntilNext`() = runTest {
        val today = LocalDate.now()
        val soon = today.plusDays(5)
        val later = today.plusDays(20)
        fakeRepo.setAll(
            listOf(
                Person(id = 2L, name = "Later", birthMonth = later.monthValue, birthDay = later.dayOfMonth),
                Person(id = 1L, name = "Soon", birthMonth = soon.monthValue, birthDay = soon.dayOfMonth),
            ),
        )
        useCase().test {
            val result = awaitItem()
            assertEquals("Soon", result[0].person.name)
            assertEquals("Later", result[1].person.name)
            cancel()
        }
    }

    @Test
    fun `repo update propagates downstream`() = runTest {
        val today = LocalDate.now()
        useCase().test {
            assertEquals(emptyList<Any>(), awaitItem())
            fakeRepo.setAll(
                listOf(Person(id = 1L, name = "Carol", birthMonth = today.monthValue, birthDay = today.dayOfMonth)),
            )
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals("Carol", result[0].person.name)
            cancel()
        }
    }
}
