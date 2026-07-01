package com.pocketaps.cakeday.core.domain.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class BirthdayDateUtilsTest {

    @Test
    fun `birthday is today returns daysUntil 0`() {
        val today = LocalDate.of(2024, 6, 15)
        assertEquals(0, BirthdayDateUtils.daysUntilNextBirthday(6, 15, today))
    }

    @Test
    fun `birthday later this year returns date in current year`() {
        val today = LocalDate.of(2024, 6, 15)
        val result = BirthdayDateUtils.nextBirthdayDate(12, 25, today)
        assertEquals(LocalDate.of(2024, 12, 25), result)
    }

    @Test
    fun `birthday already passed wraps to next year`() {
        val today = LocalDate.of(2024, 6, 15)
        val result = BirthdayDateUtils.nextBirthdayDate(3, 10, today)
        assertEquals(LocalDate.of(2025, 3, 10), result)
    }

    @Test
    fun `Jan 1 birthday today is Dec 31 returns 1 day`() {
        val today = LocalDate.of(2024, 12, 31)
        assertEquals(1, BirthdayDateUtils.daysUntilNextBirthday(1, 1, today))
        assertEquals(LocalDate.of(2025, 1, 1), BirthdayDateUtils.nextBirthdayDate(1, 1, today))
    }

    @Test
    fun `Dec 31 birthday today is Jan 1 returns correct days`() {
        val today = LocalDate.of(2024, 1, 1)
        assertEquals(LocalDate.of(2024, 12, 31), BirthdayDateUtils.nextBirthdayDate(12, 31, today))
        assertEquals(365, BirthdayDateUtils.daysUntilNextBirthday(12, 31, today))
    }

    @Test
    fun `Feb 29 birthday in leap year returns Feb 29`() {
        val today = LocalDate.of(2024, 2, 1)
        assertEquals(LocalDate.of(2024, 2, 29), BirthdayDateUtils.nextBirthdayDate(2, 29, today))
    }

    @Test
    fun `Feb 29 birthday upcoming in non-leap year returns Feb 28`() {
        val today = LocalDate.of(2025, 2, 1)
        assertEquals(LocalDate.of(2025, 2, 28), BirthdayDateUtils.nextBirthdayDate(2, 29, today))
    }

    @Test
    fun `Feb 29 birthday passed in non-leap year wraps to next non-leap year Feb 28`() {
        val today = LocalDate.of(2025, 3, 1)
        assertEquals(LocalDate.of(2026, 2, 28), BirthdayDateUtils.nextBirthdayDate(2, 29, today))
    }

    @Test
    fun `Feb 29 birthday wraps to leap year returns Feb 29`() {
        val today = LocalDate.of(2027, 3, 1)
        assertEquals(LocalDate.of(2028, 2, 29), BirthdayDateUtils.nextBirthdayDate(2, 29, today))
    }

    @Test
    fun `ageOnNextBirthday computes correctly`() {
        val nextBirthday = LocalDate.of(2025, 6, 15)
        assertEquals(30, BirthdayDateUtils.ageOnNextBirthday(1995, nextBirthday))
    }
}
