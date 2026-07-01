package com.pocketaps.cakeday.core.model

enum class ReminderLead(val days: Int) {
    ON_THE_DAY(0),
    ONE_DAY_BEFORE(1),
    THREE_DAYS_BEFORE(3),
    ONE_WEEK_BEFORE(7),
}
