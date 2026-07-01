package com.pocketaps.cakeday.feature.people

import app.cash.turbine.test
import com.pocketaps.cakeday.core.domain.usecase.DeletePersonUseCase
import com.pocketaps.cakeday.core.domain.usecase.GetUpcomingBirthdaysUseCase
import com.pocketaps.cakeday.core.model.Person
import com.pocketaps.cakeday.core.testing.fake.FakePersonRepository
import com.pocketaps.cakeday.core.testing.rule.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class PeopleViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeRepo: FakePersonRepository
    private lateinit var viewModel: PeopleViewModel

    @Before
    fun setUp() {
        fakeRepo = FakePersonRepository()
        viewModel = PeopleViewModel(
            getUpcomingBirthdays = GetUpcomingBirthdaysUseCase(fakeRepo),
            deletePerson = DeletePersonUseCase(fakeRepo),
        )
    }

    @Test
    fun `state is empty when repository has no people`() = runTest {
        viewModel.uiState.test {
            assertEquals(PeopleUiState.Empty, awaitItem())
        }
    }

    @Test
    fun `state is content when repository has people`() = runTest {
        val today = LocalDate.now()
        fakeRepo.setAll(
            listOf(Person(id = 1L, name = "Alice", birthMonth = today.monthValue, birthDay = today.dayOfMonth)),
        )

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is PeopleUiState.Content)
            assertEquals(1, (state as PeopleUiState.Content).people.size)
        }
    }

    @Test
    fun `deleting a person removes it and content transitions to empty`() = runTest {
        val today = LocalDate.now()
        fakeRepo.setAll(
            listOf(Person(id = 1L, name = "Alice", birthMonth = today.monthValue, birthDay = today.dayOfMonth)),
        )

        viewModel.uiState.test {
            assertTrue(awaitItem() is PeopleUiState.Content)
            viewModel.onDeletePerson(1L)
            assertEquals(PeopleUiState.Empty, awaitItem())
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
}
