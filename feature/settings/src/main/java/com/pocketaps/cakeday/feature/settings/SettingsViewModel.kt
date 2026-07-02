package com.pocketaps.cakeday.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pocketaps.cakeday.core.domain.repository.SettingsRepository
import com.pocketaps.cakeday.core.model.ReminderLead
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = settingsRepository.observeReminderLead()
        .map { lead -> SettingsUiState(isLoading = false, selectedLead = lead) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = SettingsUiState(),
        )

    fun onLeadSelected(lead: ReminderLead) {
        viewModelScope.launch { settingsRepository.setReminderLead(lead) }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
