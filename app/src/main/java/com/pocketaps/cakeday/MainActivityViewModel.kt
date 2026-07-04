package com.pocketaps.cakeday

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pocketaps.cakeday.core.domain.repository.SettingsRepository
import com.pocketaps.cakeday.core.model.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    settingsRepository: SettingsRepository,
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = settingsRepository.observeThemeMode()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = ThemeMode.SYSTEM,
        )

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
