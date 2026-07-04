package com.pocketaps.cakeday.core.domain.usecase

import com.pocketaps.cakeday.core.domain.backup.ImportResult
import com.pocketaps.cakeday.core.domain.repository.BackupRepository
import javax.inject.Inject

class ImportDataUseCase @Inject constructor(
    private val backupRepository: BackupRepository,
) {
    suspend operator fun invoke(rawJson: String): ImportResult = backupRepository.importFromJson(rawJson)
}
