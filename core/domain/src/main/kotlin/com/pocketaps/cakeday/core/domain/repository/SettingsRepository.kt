package com.pocketaps.cakeday.core.domain.repository

import com.pocketaps.cakeday.core.model.ReminderLead
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun observeReminderLead(): Flow<ReminderLead>
    suspend fun setReminderLead(lead: ReminderLead)
}
