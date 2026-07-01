package com.pocketaps.cakeday.core.model

data class Person(
    val id: Long = 0,
    val name: String,
    val birthMonth: Int,
    val birthDay: Int,
    val birthYear: Int? = null,
    val note: String? = null,
    val groupId: Long? = null,
    val reminderLeadDaysOverride: Int? = null,
    val createdAt: Long = 0,
)
