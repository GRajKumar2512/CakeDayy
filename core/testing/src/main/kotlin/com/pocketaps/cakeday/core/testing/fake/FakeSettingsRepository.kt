package com.pocketaps.cakeday.core.testing.fake

import com.pocketaps.cakeday.core.domain.repository.SettingsRepository
import com.pocketaps.cakeday.core.model.ReminderLead
import com.pocketaps.cakeday.core.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeSettingsRepository(
    initialLead: ReminderLead = ReminderLead.ON_THE_DAY,
    initialThemeMode: ThemeMode = ThemeMode.SYSTEM,
) : SettingsRepository {

    private val leadFlow = MutableStateFlow(initialLead)
    private val themeModeFlow = MutableStateFlow(initialThemeMode)

    override fun observeReminderLead(): Flow<ReminderLead> = leadFlow

    override suspend fun setReminderLead(lead: ReminderLead) {
        leadFlow.value = lead
    }

    override fun observeThemeMode(): Flow<ThemeMode> = themeModeFlow

    override suspend fun setThemeMode(mode: ThemeMode) {
        themeModeFlow.value = mode
    }
}
