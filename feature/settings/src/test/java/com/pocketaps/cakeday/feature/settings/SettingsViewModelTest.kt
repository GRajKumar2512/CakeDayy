package com.pocketaps.cakeday.feature.settings

import app.cash.turbine.test
import com.pocketaps.cakeday.core.model.ReminderLead
import com.pocketaps.cakeday.core.model.ThemeMode
import com.pocketaps.cakeday.core.testing.fake.FakeSettingsRepository
import com.pocketaps.cakeday.core.testing.rule.MainDispatcherRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeRepo: FakeSettingsRepository
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setUp() {
        fakeRepo = FakeSettingsRepository(initialLead = ReminderLead.ON_THE_DAY)
        viewModel = SettingsViewModel(fakeRepo)
    }

    @Test
    fun `initial state reflects the persisted lead`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(false, state.isLoading)
            assertEquals(ReminderLead.ON_THE_DAY, state.selectedLead)
        }
    }

    @Test
    fun `selecting a lead persists it and re-emits`() = runTest {
        viewModel.uiState.test {
            assertEquals(ReminderLead.ON_THE_DAY, awaitItem().selectedLead)

            viewModel.onLeadSelected(ReminderLead.ONE_WEEK_BEFORE)

            assertEquals(ReminderLead.ONE_WEEK_BEFORE, awaitItem().selectedLead)
        }
        assertEquals(ReminderLead.ONE_WEEK_BEFORE, fakeRepo.observeReminderLead().first())
    }

    @Test
    fun `selecting a theme mode persists it and re-emits`() = runTest {
        viewModel.uiState.test {
            assertEquals(ThemeMode.SYSTEM, awaitItem().themeMode)

            viewModel.onThemeModeSelected(ThemeMode.DARK)

            assertEquals(ThemeMode.DARK, awaitItem().themeMode)
        }
        assertEquals(ThemeMode.DARK, fakeRepo.observeThemeMode().first())
    }
}
