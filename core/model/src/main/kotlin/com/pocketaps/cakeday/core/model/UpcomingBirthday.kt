package com.pocketaps.cakeday.core.model

data class UpcomingBirthday(
    val person: Person,
    val daysUntilNext: Int,
    val nextAge: Int?,
)
