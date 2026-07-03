package com.pocketaps.cakeday.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "groups")
data class GroupEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val remoteId: String? = null,
    val name: String,
    val colorHex: String,
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean = false,
)
