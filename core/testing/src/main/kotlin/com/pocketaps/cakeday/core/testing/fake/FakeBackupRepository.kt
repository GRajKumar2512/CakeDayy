package com.pocketaps.cakeday.core.testing.fake

import com.pocketaps.cakeday.core.domain.backup.ImportResult
import com.pocketaps.cakeday.core.domain.repository.BackupRepository

class FakeBackupRepository(
    var exportResult: String = "{}",
    var importResult: ImportResult = ImportResult.Success,
) : BackupRepository {

    var lastImportedJson: String? = null
        private set

    override suspend fun exportToJson(): String = exportResult

    override suspend fun importFromJson(rawJson: String): ImportResult {
        lastImportedJson = rawJson
        return importResult
    }
}
