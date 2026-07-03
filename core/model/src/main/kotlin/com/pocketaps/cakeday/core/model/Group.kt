package com.pocketaps.cakeday.core.model

data class Group(
    val id: Long = 0,
    val name: String,
    val colorHex: String,
    val createdAt: Long = 0,
)
