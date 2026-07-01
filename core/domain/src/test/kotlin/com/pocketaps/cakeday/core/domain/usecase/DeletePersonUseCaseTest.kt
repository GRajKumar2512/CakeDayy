package com.pocketaps.cakeday.core.domain.usecase

import app.cash.turbine.test
import com.pocketaps.cakeday.core.model.Person
import com.pocketaps.cakeday.core.testing.fake.FakePersonRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class DeletePersonUseCaseTest {

    private lateinit var fakeRepo: FakePersonRepository
    private lateinit var useCase: DeletePersonUseCase

    @Before
    fun setUp() {
        fakeRepo = FakePersonRepository()
        useCase = DeletePersonUseCase(fakeRepo)
    }

    @Test
    fun `deleting a person removes it from the repository`() = runTest {
        fakeRepo.setAll(listOf(Person(id = 1L, name = "Alice", birthMonth = 5, birthDay = 10)))

        useCase(1L)

        fakeRepo.observeAll().test {
            assertEquals(emptyList<Person>(), awaitItem())
            cancel()
        }
    }

    @Test
    fun `deleting an unrelated id leaves other people untouched`() = runTest {
        fakeRepo.setAll(
            listOf(
                Person(id = 1L, name = "Alice", birthMonth = 5, birthDay = 10),
                Person(id = 2L, name = "Bob", birthMonth = 6, birthDay = 1),
            ),
        )

        useCase(1L)

        fakeRepo.observeAll().test {
            val people = awaitItem()
            assertEquals(1, people.size)
            assertEquals("Bob", people[0].name)
            cancel()
        }
    }
}
