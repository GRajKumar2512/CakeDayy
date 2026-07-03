package com.pocketaps.cakeday.core.domain.util

data class ParsedBirthday(val month: Int, val day: Int, val year: Int?)

// Mirrors ContactsContract.CommonDataKinds.Event.START_DATE: "yyyy-MM-dd", or "--MM-dd" when the year is unknown.
object ContactEventDateParser {

    private val FULL_DATE = Regex("""^(\d{4})-(\d{2})-(\d{2})$""")
    private val NO_YEAR_DATE = Regex("""^--(\d{2})-(\d{2})$""")

    fun parse(raw: String): ParsedBirthday? {
        val fullMatch = FULL_DATE.matchEntire(raw)
        val noYearMatch = NO_YEAR_DATE.matchEntire(raw)
        return when {
            fullMatch != null -> {
                val (year, month, day) = fullMatch.destructured
                ParsedBirthday(month = month.toInt(), day = day.toInt(), year = year.toInt())
            }
            noYearMatch != null -> {
                val (month, day) = noYearMatch.destructured
                ParsedBirthday(month = month.toInt(), day = day.toInt(), year = null)
            }
            else -> null
        }
    }
}
