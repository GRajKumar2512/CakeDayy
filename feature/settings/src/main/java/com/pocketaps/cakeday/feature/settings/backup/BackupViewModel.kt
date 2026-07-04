package com.pocketaps.cakeday.feature.settings.backup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pocketaps.cakeday.core.domain.backup.ImportErrorReason
import com.pocketaps.cakeday.core.domain.backup.ImportResult
import com.pocketaps.cakeday.core.domain.usecase.ExportDataUseCase
import com.pocketaps.cakeday.core.domain.usecase.ImportDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val exportDataUseCase: ExportDataUseCase,
    private val importDataUseCase: ImportDataUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<BackupUiState>(BackupUiState.Idle)
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

    suspend fun buildExportJson(): String {
        _uiState.value = BackupUiState.Working
        return exportDataUseCase()
    }

    fun onExportResult(success: Boolean) {
        _uiState.value = if (success) {
            BackupUiState.ExportSuccess
        } else {
            BackupUiState.Error(BackupErrorReason.FILE_READ_WRITE_FAILED)
        }
    }

    fun onFileReadFailed() {
        _uiState.value = BackupUiState.Error(BackupErrorReason.FILE_READ_WRITE_FAILED)
    }

    fun onJsonPicked(json: String) {
        viewModelScope.launch {
            _uiState.value = BackupUiState.Working
            _uiState.value = when (val result = importDataUseCase(json)) {
                ImportResult.Success -> BackupUiState.ImportSuccess
                is ImportResult.Error -> BackupUiState.Error(result.reason.toUiReason())
            }
        }
    }
}

private fun ImportErrorReason.toUiReason(): BackupErrorReason = when (this) {
    ImportErrorReason.MALFORMED_JSON -> BackupErrorReason.MALFORMED_JSON
    ImportErrorReason.UNSUPPORTED_SCHEMA_VERSION -> BackupErrorReason.UNSUPPORTED_SCHEMA_VERSION
    ImportErrorReason.INVALID_PAYLOAD -> BackupErrorReason.INVALID_PAYLOAD
}
