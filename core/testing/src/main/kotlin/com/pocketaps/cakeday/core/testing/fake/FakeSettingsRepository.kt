package com.pocketaps.cakeday.core.testing.fake

import com.pocketaps.cakeday.core.domain.repository.SettingsRepository
import com.pocketaps.cakeday.core.model.ReminderLead
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeSettingsRepository(
    initialLead: ReminderLead = ReminderLead.ON_THE_DAY,
) : SettingsRepository {

    private val leadFlow = MutableStateFlow(initialLead)

    override fun observeReminderLead(): Flow<ReminderLead> = leadFlow

    override suspend fun setReminderLead(lead: ReminderLead) {
        leadFlow.value = lead
    }
}
