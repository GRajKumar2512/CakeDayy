package com.pocketaps.cakeday.feature.editperson

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.pocketaps.cakeday.core.domain.usecase.ObserveGroupsUseCase
import com.pocketaps.cakeday.core.domain.usecase.ObservePersonUseCase
import com.pocketaps.cakeday.core.domain.usecase.SavePersonUseCase
import com.pocketaps.cakeday.core.model.Group
import com.pocketaps.cakeday.core.model.Person
import com.pocketaps.cakeday.core.testing.fake.FakeGroupRepository
import com.pocketaps.cakeday.core.testing.fake.FakePersonRepository
import com.pocketaps.cakeday.core.testing.rule.MainDispatcherRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class EditPersonViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun createViewModel(
        personId: Long? = null,
        fakeRepo: FakePersonRepository = FakePersonRepository(),
        fakeGroupRepo: FakeGroupRepository = FakeGroupRepository(),
    ): EditPersonViewModel {
        val savedStateHandle = SavedStateHandle(mapOf("personId" to personId))
        return EditPersonViewModel(
            savedStateHandle = savedStateHandle,
            observePerson = ObservePersonUseCase(fakeRepo),
            observeGroups = ObserveGroupsUseCase(fakeGroupRepo),
            savePerson = SavePersonUseCase(fakeRepo),
        )
    }

    @Test
    fun `add mode starts with blank defaults`() = runTest {
        val viewModel = createViewModel(personId = null)

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.isAddMode)
            assertEquals("", state.name)
        }
    }

    @Test
    fun `edit mode loads the existing person`() = runTest {
        val fakeRepo = FakePersonRepository()
        fakeRepo.setAll(
            listOf(Person(id = 1L, name = "Alice", birthMonth = 5, birthDay = 10, birthYear = 1994, note = "Cake")),
        )
        val viewModel = createViewModel(personId = 1L, fakeRepo = fakeRepo)

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(!state.isLoading)
            assertEquals("Alice", state.name)
            assertEquals(1994, state.birthYear)
            assertEquals("Cake", state.note)
        }
    }

    @Test
    fun `blank name blocks save and sets nameError`() = runTest {
        val viewModel = createViewModel(personId = null)

        viewModel.onNameChange("")
        viewModel.onSaveClick()

        viewModel.uiState.test {
            assertEquals("Name is required", awaitItem().nameError)
        }
    }

    @Test
    fun `changing to a non-leap year clamps day 29 down to 28`() = runTest {
        val viewModel = createViewModel(personId = null)

        viewModel.onMonthChange(2)
        viewModel.onDayChange(29)
        viewModel.onYearChange("2023")

        viewModel.uiState.test {
            assertEquals(28, awaitItem().birthDay)
        }
    }

    @Test
    fun `successful save triggers NavigateBack effect and persists the person`() = runTest {
        val fakeRepo = FakePersonRepository()
        val viewModel = createViewModel(personId = null, fakeRepo = fakeRepo)
        viewModel.onNameChange("Alice")
        viewModel.onMonthChange(5)
        viewModel.onDayChange(10)

        viewModel.effect.test {
            viewModel.onSaveClick()
            assertEquals(EditPersonEffect.NavigateBack, awaitItem())
        }

        val saved = fakeRepo.observeAll().first()
        assertEquals(1, saved.size)
        assertEquals("Alice", saved[0].name)
    }

    @Test
    fun `selecting a group persists it on save`() = runTest {
        val fakeRepo = FakePersonRepository()
        val fakeGroupRepo = FakeGroupRepository().apply {
            setAll(listOf(Group(id = 1L, name = "Family", colorHex = "#F44336")))
        }
        val viewModel = createViewModel(personId = null, fakeRepo = fakeRepo, fakeGroupRepo = fakeGroupRepo)
        viewModel.onNameChange("Alice")
        viewModel.onMonthChange(5)
        viewModel.onDayChange(10)
        viewModel.onGroupSelected(1L)

        viewModel.onSaveClick()

        val saved = fakeRepo.observeAll().first()
        assertEquals(1L, saved[0].groupId)
    }
}
