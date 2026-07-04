package com.pocketaps.cakeday.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.pocketaps.cakeday.core.model.ReminderLead
import com.pocketaps.cakeday.core.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserPreferencesDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {

    val reminderLead: Flow<ReminderLead> = dataStore.data.map { preferences ->
        val storedDays = preferences[REMINDER_LEAD_DAYS] ?: ReminderLead.ON_THE_DAY.days
        ReminderLead.entries.firstOrNull { it.days == storedDays } ?: ReminderLead.ON_THE_DAY
    }

    suspend fun setReminderLead(lead: ReminderLead) {
        dataStore.edit { preferences -> preferences[REMINDER_LEAD_DAYS] = lead.days }
    }

    val themeMode: Flow<ThemeMode> = dataStore.data.map { preferences ->
        val stored = preferences[THEME_MODE]
        ThemeMode.entries.firstOrNull { it.name == stored } ?: ThemeMode.SYSTEM
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { preferences -> preferences[THEME_MODE] = mode.name }
    }

    private companion object {
        val REMINDER_LEAD_DAYS = intPreferencesKey("reminder_lead_days")
        val THEME_MODE = stringPreferencesKey("theme_mode")
    }
}
