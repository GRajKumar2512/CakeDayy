package com.pocketaps.cakeday.core.data.backup.dto

import kotlinx.serialization.Serializable

@Serializable
data class BackupEnvelopeDto(
    val schemaVersion: Int = CURRENT_SCHEMA_VERSION,
    val exportedAt: Long,
    val people: List<PersonBackupDto>,
    val groups: List<GroupBackupDto>,
    val settings: SettingsBackupDto,
) {
    companion object {
        const val CURRENT_SCHEMA_VERSION = 1
    }
}
