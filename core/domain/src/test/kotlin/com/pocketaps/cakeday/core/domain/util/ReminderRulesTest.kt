package com.pocketaps.cakeday.core.domain.util

import com.pocketaps.cakeday.core.model.Person
import com.pocketaps.cakeday.core.model.ReminderLead
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ReminderRulesTest {

    @Test
    fun `effectiveLeadDays uses per-person override when present`() {
        val person = Person(name = "Alice", birthMonth = 5, birthDay = 10, reminderLeadDaysOverride = 3)

        val effective = ReminderRules.effectiveLeadDays(person, ReminderLead.ONE_WEEK_BEFORE)

        assertEquals(3, effective)
    }

    @Test
    fun `effectiveLeadDays falls back to global lead when no override`() {
        val person = Person(name = "Alice", birthMonth = 5, birthDay = 10, reminderLeadDaysOverride = null)

        val effective = ReminderRules.effectiveLeadDays(person, ReminderLead.ONE_WEEK_BEFORE)

        assertEquals(ReminderLead.ONE_WEEK_BEFORE.days, effective)
    }

    @Test
    fun `isReminderDue is true when days until birthday equals the effective lead`() {
        val today = LocalDate.now()
        val target = today.plusDays(3)
        val person = Person(name = "Alice", birthMonth = target.monthValue, birthDay = target.dayOfMonth)

        assertTrue(ReminderRules.isReminderDue(person, today, effectiveLeadDays = 3))
    }

    @Test
    fun `isReminderDue is false when days until birthday differs from the effective lead`() {
        val today = LocalDate.now()
        val target = today.plusDays(4)
        val person = Person(name = "Alice", birthMonth = target.monthValue, birthDay = target.dayOfMonth)

        assertFalse(ReminderRules.isReminderDue(person, today, effectiveLeadDays = 3))
    }

    @Test
    fun `isReminderDue is true on the day of the birthday when lead is zero`() {
        val today = LocalDate.now()
        val person = Person(name = "Alice", birthMonth = today.monthValue, birthDay = today.dayOfMonth)

        assertTrue(ReminderRules.isReminderDue(person, today, effectiveLeadDays = 0))
    }
}
