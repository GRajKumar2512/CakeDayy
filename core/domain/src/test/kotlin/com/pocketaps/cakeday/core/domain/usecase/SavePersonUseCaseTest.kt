package com.pocketaps.cakeday.core.domain.usecase

import app.cash.turbine.test
import com.pocketaps.cakeday.core.model.Person
import com.pocketaps.cakeday.core.testing.fake.FakePersonRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class SavePersonUseCaseTest {

    private lateinit var fakeRepo: FakePersonRepository
    private lateinit var useCase: SavePersonUseCase

    @Before
    fun setUp() {
        fakeRepo = FakePersonRepository()
        useCase = SavePersonUseCase(fakeRepo)
    }

    @Test
    fun `saving a new person adds it to the repository`() = runTest {
        useCase(Person(name = "Alice", birthMonth = 5, birthDay = 10))

        fakeRepo.observeAll().test {
            val people = awaitItem()
            assertEquals(1, people.size)
            assertEquals("Alice", people[0].name)
            cancel()
        }
    }

    @Test
    fun `saving an existing person preserves its id`() = runTest {
        useCase(Person(name = "Alice", birthMonth = 5, birthDay = 10))
        val saved = fakeRepo.observeAll().first().single()

        useCase(saved.copy(name = "Alice Updated"))

        fakeRepo.observeAll().test {
            val people = awaitItem()
            assertEquals(1, people.size)
            assertEquals(saved.id, people[0].id)
            assertEquals("Alice Updated", people[0].name)
            cancel()
        }
    }
}
