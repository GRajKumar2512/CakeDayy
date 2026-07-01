package com.pocketaps.cakeday.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "person")
data class PersonEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
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
