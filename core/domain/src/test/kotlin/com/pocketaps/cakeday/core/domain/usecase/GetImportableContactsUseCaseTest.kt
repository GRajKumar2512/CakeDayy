package com.pocketaps.cakeday.core.domain.usecase

import com.pocketaps.cakeday.core.model.ContactBirthday
import com.pocketaps.cakeday.core.model.Person
import com.pocketaps.cakeday.core.testing.fake.FakeContactsRepository
import com.pocketaps.cakeday.core.testing.fake.FakePersonRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetImportableContactsUseCaseTest {

    @Test
    fun `skips contacts that already match an existing person by name and birth date`() = runTest {
        val fakePersonRepo = FakePersonRepository().apply {
            setAll(listOf(Person(name = "Alice", birthMonth = 5, birthDay = 10, birthYear = 1994)))
        }
        val fakeContactsRepo = FakeContactsRepository(
            contacts = listOf(
                ContactBirthday(contactId = "1", name = "Alice", birthMonth = 5, birthDay = 10, birthYear = 1994),
                ContactBirthday(contactId = "2", name = "Bob", birthMonth = 1, birthDay = 1, birthYear = null),
            ),
        )
        val useCase = GetImportableContactsUseCase(fakeContactsRepo, fakePersonRepo)

        val result = useCase()

        assertEquals(1, result.size)
        assertEquals("Bob", result[0].name)
    }

    @Test
    fun `is case-insensitive and treats different birth years as distinct`() = runTest {
        val fakePersonRepo = FakePersonRepository().apply {
            setAll(listOf(Person(name = "alice", birthMonth = 5, birthDay = 10, birthYear = 1994)))
        }
        val fakeContactsRepo = FakeContactsRepository(
            contacts = listOf(
                ContactBirthday(contactId = "1", name = "Alice", birthMonth = 5, birthDay = 10, birthYear = 1994),
                ContactBirthday(contactId = "2", name = "Alice", birthMonth = 5, birthDay = 10, birthYear = 1995),
            ),
        )
        val useCase = GetImportableContactsUseCase(fakeContactsRepo, fakePersonRepo)

        val result = useCase()

        assertEquals(1, result.size)
        assertEquals(1995, result[0].birthYear)
    }

    @Test
    fun `returns all contacts when there are no existing people`() = runTest {
        val fakeContactsRepo = FakeContactsRepository(
            contacts = listOf(
                ContactBirthday(contactId = "1", name = "Alice", birthMonth = 5, birthDay = 10, birthYear = 1994),
            ),
        )
        val useCase = GetImportableContactsUseCase(fakeContactsRepo, FakePersonRepository())

        val result = useCase()

        assertEquals(1, result.size)
    }
}
