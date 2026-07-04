package com.pocketaps.cakeday.core.data.backup.dto

import kotlinx.serialization.Serializable

@Serializable
data class GroupBackupDto(
    val id: Long,
    val remoteId: String? = null,
    val name: String,
    val colorHex: String,
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean = false,
)
