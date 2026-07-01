package com.pocketaps.cakeday.core.domain.util

import java.time.LocalDate
import java.time.Year
import java.time.temporal.ChronoUnit

object BirthdayDateUtils {

    /**
     * Returns the next occurrence of the given birthday on or after [today].
     *
     * Feb-29 rule: treated as Feb 28 in non-leap years so the person is never skipped.
     */
    fun nextBirthdayDate(birthMonth: Int, birthDay: Int, today: LocalDate): LocalDate {
        val thisYearDay = effectiveDay(birthMonth, birthDay, today.year)
        val thisYear = LocalDate.of(today.year, birthMonth, thisYearDay)
        if (!thisYear.isBefore(today)) return thisYear
        val nextYear = today.year + 1
        return LocalDate.of(nextYear, birthMonth, effectiveDay(birthMonth, birthDay, nextYear))
    }

    fun daysUntilNextBirthday(birthMonth: Int, birthDay: Int, today: LocalDate): Int =
        ChronoUnit.DAYS.between(today, nextBirthdayDate(birthMonth, birthDay, today)).toInt()

    fun ageOnNextBirthday(birthYear: Int, nextBirthday: LocalDate): Int =
        nextBirthday.year - birthYear

    private fun effectiveDay(month: Int, day: Int, year: Int): Int =
        if (month == 2 && day == 29 && !Year.of(year).isLeap) 28 else day
}
