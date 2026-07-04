package com.pocketaps.cakeday.core.data.backup.dto

import kotlinx.serialization.Serializable

@Serializable
data class PersonBackupDto(
    val id: Long,
    val remoteId: String? = null,
    val name: String,
    val birthMonth: Int,
    val birthDay: Int,
    val birthYear: Int? = null,
    val note: String? = null,
    val groupId: Long? = null,
    val reminderLeadDaysOverride: Int? = null,
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean = false,
)
