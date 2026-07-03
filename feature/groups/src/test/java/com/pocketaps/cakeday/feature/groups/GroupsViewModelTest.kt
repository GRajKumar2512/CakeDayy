package com.pocketaps.cakeday.feature.groups

import app.cash.turbine.test
import com.pocketaps.cakeday.core.domain.usecase.DeleteGroupUseCase
import com.pocketaps.cakeday.core.domain.usecase.ObserveGroupsUseCase
import com.pocketaps.cakeday.core.domain.usecase.SaveGroupUseCase
import com.pocketaps.cakeday.core.model.Group
import com.pocketaps.cakeday.core.testing.fake.FakeGroupRepository
import com.pocketaps.cakeday.core.testing.rule.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class GroupsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeRepo: FakeGroupRepository
    private lateinit var viewModel: GroupsViewModel

    @Before
    fun setUp() {
        fakeRepo = FakeGroupRepository()
        viewModel = GroupsViewModel(
            observeGroups = ObserveGroupsUseCase(fakeRepo),
            saveGroup = SaveGroupUseCase(fakeRepo),
            deleteGroup = DeleteGroupUseCase(fakeRepo),
        )
    }

    @Test
    fun `state is empty content when repository has no groups`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is GroupsUiState.Content)
            assertTrue((state as GroupsUiState.Content).groups.isEmpty())
        }
    }

    @Test
    fun `creating a group appears in state`() = runTest {
        viewModel.uiState.test {
            assertTrue((awaitItem() as GroupsUiState.Content).groups.isEmpty())
            viewModel.onSaveGroup(Group(name = "Family", colorHex = "#F44336"))
            val updated = awaitItem() as GroupsUiState.Content
            assertEquals(1, updated.groups.size)
            assertEquals("Family", updated.groups[0].name)
        }
    }

    @Test
    fun `deleting a group removes it from state`() = runTest {
        fakeRepo.setAll(listOf(Group(id = 1L, name = "Family", colorHex = "#F44336")))

        viewModel.uiState.test {
            assertEquals(1, (awaitItem() as GroupsUiState.Content).groups.size)
            viewModel.onDeleteGroup(1L)
            assertTrue((awaitItem() as GroupsUiState.Content).groups.isEmpty())
        }
    }
}
