package com.pocketaps.cakeday.core.domain.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ContactEventDateParserTest {

    @Test
    fun `parses a full date with a known year`() {
        val result = ContactEventDateParser.parse("1994-05-10")

        assertEquals(ParsedBirthday(month = 5, day = 10, year = 1994), result)
    }

    @Test
    fun `parses a year-less date using the double-dash convention`() {
        val result = ContactEventDateParser.parse("--05-10")

        assertEquals(ParsedBirthday(month = 5, day = 10, year = null), result)
    }

    @Test
    fun `returns null for malformed input`() {
        assertNull(ContactEventDateParser.parse("not-a-date"))
        assertNull(ContactEventDateParser.parse(""))
        assertNull(ContactEventDateParser.parse("2024/05/10"))
    }
}
