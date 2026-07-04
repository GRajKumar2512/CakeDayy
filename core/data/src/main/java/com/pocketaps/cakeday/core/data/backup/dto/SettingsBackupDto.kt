package com.pocketaps.cakeday.core.data.backup.dto

import kotlinx.serialization.Serializable

@Serializable
data class SettingsBackupDto(
    val reminderLeadDays: Int,
    val themeMode: String,
)
