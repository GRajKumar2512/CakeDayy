package com.pocketaps.cakeday.feature.settings

import com.pocketaps.cakeday.core.model.ReminderLead

data class SettingsUiState(
    val isLoading: Boolean = true,
    val selectedLead: ReminderLead = ReminderLead.ON_THE_DAY,
)
