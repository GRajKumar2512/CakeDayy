package com.pocketaps.cakeday.core.domain.backup

sealed interface ImportResult {
    data object Success : ImportResult
    data class Error(val reason: ImportErrorReason) : ImportResult
}

enum class ImportErrorReason {
    MALFORMED_JSON,
    UNSUPPORTED_SCHEMA_VERSION,
    INVALID_PAYLOAD,
}
