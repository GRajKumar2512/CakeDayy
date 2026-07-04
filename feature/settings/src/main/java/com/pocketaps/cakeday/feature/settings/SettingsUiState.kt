package com.pocketaps.cakeday.feature.settings

import com.pocketaps.cakeday.core.model.ReminderLead
import com.pocketaps.cakeday.core.model.ThemeMode

data class SettingsUiState(
    val isLoading: Boolean = true,
    val selectedLead: ReminderLead = ReminderLead.ON_THE_DAY,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
)

data class SettingsActions(
    val onLeadSelected: (ReminderLead) -> Unit,
    val onThemeModeSelected: (ThemeMode) -> Unit,
    val onOpenBackup: () -> Unit,
    val onBackClick: () -> Unit,
)
