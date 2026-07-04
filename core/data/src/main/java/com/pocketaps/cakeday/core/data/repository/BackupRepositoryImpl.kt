package com.pocketaps.cakeday.core.data.repository

import com.pocketaps.cakeday.core.common.dispatcher.DispatcherProvider
import com.pocketaps.cakeday.core.data.backup.dto.BackupEnvelopeDto
import com.pocketaps.cakeday.core.data.backup.dto.GroupBackupDto
import com.pocketaps.cakeday.core.data.backup.dto.PersonBackupDto
import com.pocketaps.cakeday.core.data.backup.dto.SettingsBackupDto
import com.pocketaps.cakeday.core.database.CakeDayyDatabase
import com.pocketaps.cakeday.core.database.entity.GroupEntity
import com.pocketaps.cakeday.core.database.entity.PersonEntity
import com.pocketaps.cakeday.core.domain.backup.ImportErrorReason
import com.pocketaps.cakeday.core.domain.backup.ImportResult
import com.pocketaps.cakeday.core.domain.repository.BackupRepository
import com.pocketaps.cakeday.core.domain.repository.SettingsRepository
import com.pocketaps.cakeday.core.model.ReminderLead
import com.pocketaps.cakeday.core.model.ThemeMode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject

class BackupRepositoryImpl @Inject constructor(
    private val database: CakeDayyDatabase,
    private val settingsRepository: SettingsRepository,
    private val dispatchers: DispatcherProvider,
    private val json: Json,
) : BackupRepository {

    override suspend fun exportToJson(): String = withContext(dispatchers.io) {
        val envelope = BackupEnvelopeDto(
            exportedAt = System.currentTimeMillis(),
            people = database.personDao().getAllIncludingDeleted().map { it.toBackupDto() },
            groups = database.groupDao().getAllIncludingDeleted().map { it.toBackupDto() },
            settings = SettingsBackupDto(
                reminderLeadDays = settingsRepository.observeReminderLead().first().days,
                themeMode = settingsRepository.observeThemeMode().first().name,
            ),
        )
        json.encodeToString(envelope)
    }

    override suspend fun importFromJson(rawJson: String): ImportResult = withContext(dispatchers.io) {
        val envelope = runCatching { json.decodeFromString<BackupEnvelopeDto>(rawJson) }.getOrNull()
            ?: return@withContext ImportResult.Error(ImportErrorReason.MALFORMED_JSON)
        val validationError = validate(envelope)
        if (validationError != null) return@withContext ImportResult.Error(validationError)
        applyImport(envelope)
    }

    private fun validate(envelope: BackupEnvelopeDto): ImportErrorReason? = when {
        envelope.schemaVersion > BackupEnvelopeDto.CURRENT_SCHEMA_VERSION ->
            ImportErrorReason.UNSUPPORTED_SCHEMA_VERSION
        envelope.people.any { it.name.isBlank() } -> ImportErrorReason.INVALID_PAYLOAD
        envelope.groups.any { it.name.isBlank() } -> ImportErrorReason.INVALID_PAYLOAD
        else -> null
    }

    private suspend fun applyImport(envelope: BackupEnvelopeDto): ImportResult = runCatching {
        database.replaceAllData(
            people = envelope.people.map { it.toEntity() },
            groups = envelope.groups.map { it.toEntity() },
        )
        settingsRepository.setReminderLead(
            ReminderLead.entries.first { it.days == envelope.settings.reminderLeadDays },
        )
        settingsRepository.setThemeMode(ThemeMode.valueOf(envelope.settings.themeMode))
    }.fold(
        onSuccess = { ImportResult.Success },
        onFailure = { ImportResult.Error(ImportErrorReason.INVALID_PAYLOAD) },
    )
}

private fun PersonEntity.toBackupDto() = PersonBackupDto(
    id = id,
    remoteId = remoteId,
    name = name,
    birthMonth = birthMonth,
    birthDay = birthDay,
    birthYear = birthYear,
    note = note,
    groupId = groupId,
    reminderLeadDaysOverride = reminderLeadDaysOverride,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isDeleted = isDeleted,
)

private fun PersonBackupDto.toEntity() = PersonEntity(
    id = id,
    remoteId = remoteId,
    name = name,
    birthMonth = birthMonth,
    birthDay = birthDay,
    birthYear = birthYear,
    note = note,
    groupId = groupId,
    reminderLeadDaysOverride = reminderLeadDaysOverride,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isDeleted = isDeleted,
)

private fun GroupEntity.toBackupDto() = GroupBackupDto(
    id = id,
    remoteId = remoteId,
    name = name,
    colorHex = colorHex,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isDeleted = isDeleted,
)

private fun GroupBackupDto.toEntity() = GroupEntity(
    id = id,
    remoteId = remoteId,
    name = name,
    colorHex = colorHex,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isDeleted = isDeleted,
)
