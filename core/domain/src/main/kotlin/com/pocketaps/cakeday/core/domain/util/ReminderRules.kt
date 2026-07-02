package com.pocketaps.cakeday.core.domain.util

import com.pocketaps.cakeday.core.model.Person
import com.pocketaps.cakeday.core.model.ReminderLead
import java.time.LocalDate

object ReminderRules {

    fun effectiveLeadDays(person: Person, globalLead: ReminderLead): Int =
        person.reminderLeadDaysOverride ?: globalLead.days

    fun isReminderDue(person: Person, today: LocalDate, effectiveLeadDays: Int): Boolean =
        BirthdayDateUtils.daysUntilNextBirthday(person.birthMonth, person.birthDay, today) == effectiveLeadDays
}
