package com.pocketaps.cakeday.core.domain.repository

import com.pocketaps.cakeday.core.domain.backup.ImportResult

interface BackupRepository {
    suspend fun exportToJson(): String
    suspend fun importFromJson(rawJson: String): ImportResult
}
