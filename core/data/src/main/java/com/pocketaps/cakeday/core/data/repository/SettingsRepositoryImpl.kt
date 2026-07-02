package com.pocketaps.cakeday.core.data.repository

import com.pocketaps.cakeday.core.datastore.UserPreferencesDataStore
import com.pocketaps.cakeday.core.domain.repository.SettingsRepository
import com.pocketaps.cakeday.core.model.ReminderLead
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val userPreferencesDataStore: UserPreferencesDataStore,
) : SettingsRepository {

    override fun observeReminderLead(): Flow<ReminderLead> = userPreferencesDataStore.reminderLead

    override suspend fun setReminderLead(lead: ReminderLead) {
        userPreferencesDataStore.setReminderLead(lead)
    }
}
