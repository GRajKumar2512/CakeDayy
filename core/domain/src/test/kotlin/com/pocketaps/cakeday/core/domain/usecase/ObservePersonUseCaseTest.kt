package com.pocketaps.cakeday.core.domain.usecase

import app.cash.turbine.test
import com.pocketaps.cakeday.core.model.Person
import com.pocketaps.cakeday.core.testing.fake.FakePersonRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class ObservePersonUseCaseTest {

    private lateinit var fakeRepo: FakePersonRepository
    private lateinit var useCase: ObservePersonUseCase

    @Before
    fun setUp() {
        fakeRepo = FakePersonRepository()
        useCase = ObservePersonUseCase(fakeRepo)
    }

    @Test
    fun `emits the matching person when present`() = runTest {
        fakeRepo.setAll(
            listOf(
                Person(id = 1L, name = "Alice", birthMonth = 5, birthDay = 10),
                Person(id = 2L, name = "Bob", birthMonth = 6, birthDay = 1),
            ),
        )

        useCase(2L).test {
            assertEquals("Bob", awaitItem()?.name)
            cancel()
        }
    }

    @Test
    fun `emits null when no person matches the id`() = runTest {
        fakeRepo.setAll(listOf(Person(id = 1L, name = "Alice", birthMonth = 5, birthDay = 10)))

        useCase(99L).test {
            assertNull(awaitItem())
            cancel()
        }
    }

    @Test
    fun `emits updated person when repository changes`() = runTest {
        fakeRepo.setAll(listOf(Person(id = 1L, name = "Alice", birthMonth = 5, birthDay = 10)))

        useCase(1L).test {
            assertEquals("Alice", awaitItem()?.name)
            fakeRepo.setAll(listOf(Person(id = 1L, name = "Alice Updated", birthMonth = 5, birthDay = 10)))
            assertEquals("Alice Updated", awaitItem()?.name)
            cancel()
        }
    }
}
