package com.pocketaps.cakeday.core.model

data class ContactBirthday(
    val contactId: String,
    val name: String,
    val birthMonth: Int,
    val birthDay: Int,
    val birthYear: Int?,
)
