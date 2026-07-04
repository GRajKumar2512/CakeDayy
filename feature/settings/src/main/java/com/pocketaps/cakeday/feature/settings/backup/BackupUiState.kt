package com.pocketaps.cakeday.feature.settings.backup

sealed interface BackupUiState {
    data object Idle : BackupUiState
    data object Working : BackupUiState
    data object ExportSuccess : BackupUiState
    data object ImportSuccess : BackupUiState
    data class Error(val reason: BackupErrorReason) : BackupUiState
}

enum class BackupErrorReason {
    MALFORMED_JSON,
    UNSUPPORTED_SCHEMA_VERSION,
    INVALID_PAYLOAD,
    FILE_READ_WRITE_FAILED,
}
