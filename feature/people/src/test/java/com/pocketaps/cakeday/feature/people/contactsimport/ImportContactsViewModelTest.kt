package com.pocketaps.cakeday.feature.people.contactsimport

import app.cash.turbine.test
import com.pocketaps.cakeday.core.domain.usecase.GetImportableContactsUseCase
import com.pocketaps.cakeday.core.domain.usecase.SavePersonUseCase
import com.pocketaps.cakeday.core.model.ContactBirthday
import com.pocketaps.cakeday.core.model.Person
import com.pocketaps.cakeday.core.testing.fake.FakeContactsRepository
import com.pocketaps.cakeday.core.testing.fake.FakePersonRepository
import com.pocketaps.cakeday.core.testing.rule.MainDispatcherRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ImportContactsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun createViewModel(
        fakePersonRepo: FakePersonRepository = FakePersonRepository(),
        fakeContactsRepo: FakeContactsRepository = FakeContactsRepository(),
    ): ImportContactsViewModel = ImportContactsViewModel(
        getImportableContacts = GetImportableContactsUseCase(fakeContactsRepo, fakePersonRepo),
        savePerson = SavePersonUseCase(fakePersonRepo),
    )

    @Test
    fun `loading contacts skips people already present`() = runTest {
        val fakePersonRepo = FakePersonRepository().apply {
            setAll(listOf(Person(name = "Alice", birthMonth = 5, birthDay = 10, birthYear = 1994)))
        }
        val fakeContactsRepo = FakeContactsRepository(
            contacts = listOf(
                ContactBirthday(contactId = "1", name = "Alice", birthMonth = 5, birthDay = 10, birthYear = 1994),
                ContactBirthday(contactId = "2", name = "Bob", birthMonth = 1, birthDay = 1, birthYear = null),
            ),
        )
        val viewModel = createViewModel(fakePersonRepo, fakeContactsRepo)

        viewModel.uiState.test {
            assertEquals(ImportContactsUiState.Loading, awaitItem())
            viewModel.onPermissionGranted()
            val loaded = awaitItem() as ImportContactsUiState.Content
            assertEquals(1, loaded.candidates.size)
            assertEquals("Bob", loaded.candidates[0].name)
        }
    }

    @Test
    fun `state is NoneFound when every contact is already a person`() = runTest {
        val fakePersonRepo = FakePersonRepository().apply {
            setAll(listOf(Person(name = "Alice", birthMonth = 5, birthDay = 10, birthYear = 1994)))
        }
        val fakeContactsRepo = FakeContactsRepository(
            contacts = listOf(
                ContactBirthday(contactId = "1", name = "Alice", birthMonth = 5, birthDay = 10, birthYear = 1994),
            ),
        )
        val viewModel = createViewModel(fakePersonRepo, fakeContactsRepo)

        viewModel.uiState.test {
            awaitItem()
            viewModel.onPermissionGranted()
            assertEquals(ImportContactsUiState.NoneFound, awaitItem())
        }
    }

    @Test
    fun `importing only persists the selected candidates`() = runTest {
        val fakePersonRepo = FakePersonRepository()
        val fakeContactsRepo = FakeContactsRepository(
            contacts = listOf(
                ContactBirthday(contactId = "1", name = "Alice", birthMonth = 5, birthDay = 10, birthYear = 1994),
                ContactBirthday(contactId = "2", name = "Bob", birthMonth = 1, birthDay = 1, birthYear = null),
            ),
        )
        val viewModel = createViewModel(fakePersonRepo, fakeContactsRepo)

        viewModel.onPermissionGranted()
        viewModel.onToggleSelected("2")

        viewModel.effect.test {
            viewModel.onImportClick()
            assertEquals(ImportContactsEffect.NavigateBack, awaitItem())
        }

        val saved = fakePersonRepo.observeAll().first()
        assertEquals(1, saved.size)
        assertEquals("Alice", saved[0].name)
    }

    @Test
    fun `toggling twice restores the selection`() = runTest {
        val fakeContactsRepo = FakeContactsRepository(
            contacts = listOf(
                ContactBirthday(contactId = "1", name = "Alice", birthMonth = 5, birthDay = 10, birthYear = 1994),
            ),
        )
        val viewModel = createViewModel(fakeContactsRepo = fakeContactsRepo)

        viewModel.uiState.test {
            awaitItem()
            viewModel.onPermissionGranted()
            val loaded = awaitItem() as ImportContactsUiState.Content
            assertTrue("1" in loaded.selectedIds)

            viewModel.onToggleSelected("1")
            val deselected = awaitItem() as ImportContactsUiState.Content
            assertTrue("1" !in deselected.selectedIds)

            viewModel.onToggleSelected("1")
            val reselected = awaitItem() as ImportContactsUiState.Content
            assertTrue("1" in reselected.selectedIds)
        }
    }
}
