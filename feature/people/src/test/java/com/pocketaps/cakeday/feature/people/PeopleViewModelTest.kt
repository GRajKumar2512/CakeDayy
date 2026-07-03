package com.pocketaps.cakeday.feature.people

import app.cash.turbine.test
import com.pocketaps.cakeday.core.domain.usecase.DeletePersonUseCase
import com.pocketaps.cakeday.core.domain.usecase.GetUpcomingBirthdaysUseCase
import com.pocketaps.cakeday.core.domain.usecase.ObserveGroupsUseCase
import com.pocketaps.cakeday.core.domain.usecase.SearchPeopleUseCase
import com.pocketaps.cakeday.core.model.Group
import com.pocketaps.cakeday.core.model.Person
import com.pocketaps.cakeday.core.testing.fake.FakeGroupRepository
import com.pocketaps.cakeday.core.testing.fake.FakePersonRepository
import com.pocketaps.cakeday.core.testing.rule.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class PeopleViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakePersonRepo: FakePersonRepository
    private lateinit var fakeGroupRepo: FakeGroupRepository
    private lateinit var viewModel: PeopleViewModel

    @Before
    fun setUp() {
        fakePersonRepo = FakePersonRepository()
        fakeGroupRepo = FakeGroupRepository()
        viewModel = PeopleViewModel(
            searchPeople = SearchPeopleUseCase(GetUpcomingBirthdaysUseCase(fakePersonRepo)),
            observeGroups = ObserveGroupsUseCase(fakeGroupRepo),
            deletePerson = DeletePersonUseCase(fakePersonRepo),
        )
    }

    @Test
    fun `state is empty content when repository has no people`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem() as PeopleUiState.Content
            assertTrue(state.people.isEmpty())
            assertTrue(!state.isFiltering)
        }
    }

    @Test
    fun `state is content when repository has people`() = runTest {
        val today = LocalDate.now()
        fakePersonRepo.setAll(
            listOf(Person(id = 1L, name = "Alice", birthMonth = today.monthValue, birthDay = today.dayOfMonth)),
        )

        viewModel.uiState.test {
            val state = awaitItem() as PeopleUiState.Content
            assertEquals(1, state.people.size)
        }
    }

    @Test
    fun `deleting a person removes it from the list`() = runTest {
        val today = LocalDate.now()
        fakePersonRepo.setAll(
            listOf(Person(id = 1L, name = "Alice", birthMonth = today.monthValue, birthDay = today.dayOfMonth)),
        )

        viewModel.uiState.test {
            assertEquals(1, (awaitItem() as PeopleUiState.Content).people.size)
            viewModel.onDeletePerson(1L)
            assertTrue((awaitItem() as PeopleUiState.Content).people.isEmpty())
        }
    }

    @Test
    fun `add click emits NavigateToAdd effect`() = runTest {
        viewModel.effect.test {
            viewModel.onAddClick()
            assertEquals(PeopleEffect.NavigateToAdd, awaitItem())
        }
    }

    @Test
    fun `person click emits NavigateToEdit effect with the id`() = runTest {
        viewModel.effect.test {
            viewModel.onPersonClick(42L)
            assertEquals(PeopleEffect.NavigateToEdit(42L), awaitItem())
        }
    }

    @Test
    fun `settings click emits NavigateToSettings effect`() = runTest {
        viewModel.effect.test {
            viewModel.onSettingsClick()
            assertEquals(PeopleEffect.NavigateToSettings, awaitItem())
        }
    }

    @Test
    fun `manage groups click emits NavigateToGroups effect`() = runTest {
        viewModel.effect.test {
            viewModel.onManageGroupsClick()
            assertEquals(PeopleEffect.NavigateToGroups, awaitItem())
        }
    }

    @Test
    fun `import contacts click emits NavigateToImportContacts effect`() = runTest {
        viewModel.effect.test {
            viewModel.onImportContactsClick()
            assertEquals(PeopleEffect.NavigateToImportContacts, awaitItem())
        }
    }

    @Test
    fun `query changes are debounced before filtering the list`() = runTest {
        val today = LocalDate.now()
        fakePersonRepo.setAll(
            listOf(
                Person(id = 1L, name = "Alice", birthMonth = today.monthValue, birthDay = today.dayOfMonth),
                Person(id = 2L, name = "Bob", birthMonth = today.monthValue, birthDay = today.dayOfMonth),
            ),
        )

        viewModel.uiState.test {
            assertEquals(2, (awaitItem() as PeopleUiState.Content).people.size)

            viewModel.onQueryChange("ali")
            advanceTimeBy(100)
            expectNoEvents()

            advanceTimeBy(250)
            val filtered = awaitItem() as PeopleUiState.Content
            assertEquals(1, filtered.people.size)
            assertEquals("Alice", filtered.people[0].person.name)
        }
    }

    @Test
    fun `search and group filter compose together`() = runTest {
        val today = LocalDate.now()
        fakePersonRepo.setAll(
            listOf(
                Person(
                    id = 1L,
                    name = "Alice",
                    birthMonth = today.monthValue,
                    birthDay = today.dayOfMonth,
                    groupId = 1L,
                ),
                Person(
                    id = 2L,
                    name = "Alicia",
                    birthMonth = today.monthValue,
                    birthDay = today.dayOfMonth,
                    groupId = 2L,
                ),
            ),
        )
        fakeGroupRepo.setAll(listOf(Group(id = 1L, name = "Family", colorHex = "#F44336")))

        viewModel.uiState.test {
            assertEquals(2, (awaitItem() as PeopleUiState.Content).people.size)

            viewModel.onGroupFilterSelected(1L)
            val groupFiltered = awaitItem() as PeopleUiState.Content
            assertEquals(1, groupFiltered.people.size)

            viewModel.onQueryChange("ali")
            advanceTimeBy(350)
            val combined = awaitItem() as PeopleUiState.Content
            assertEquals(1, combined.people.size)
            assertEquals("Alice", combined.people[0].person.name)
        }
    }
}
