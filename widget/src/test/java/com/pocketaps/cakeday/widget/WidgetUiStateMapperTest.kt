package com.pocketaps.cakeday.widget

import com.pocketaps.cakeday.core.model.Person
import com.pocketaps.cakeday.core.model.UpcomingBirthday
import org.junit.Assert.assertEquals
import org.junit.Test

class WidgetUiStateMapperTest {

    private fun upcoming(name: String, daysUntilNext: Int) = UpcomingBirthday(
        person = Person(name = name, birthMonth = 1, birthDay = 1),
        daysUntilNext = daysUntilNext,
        nextAge = null,
    )

    @Test
    fun `empty list maps to Empty`() {
        assertEquals(WidgetUiState.Empty, emptyList<UpcomingBirthday>().toWidgetUiState())
    }

    @Test
    fun `list smaller than maxItems keeps all items in order`() {
        val upcomingBirthdays = listOf(upcoming("Alice", 1), upcoming("Bob", 5))

        val result = upcomingBirthdays.toWidgetUiState(maxItems = 5)

        assertEquals(
            WidgetUiState.Content(
                listOf(WidgetBirthdayItem("Alice", "Tomorrow"), WidgetBirthdayItem("Bob", "In 5 days")),
            ),
            result,
        )
    }

    @Test
    fun `list larger than maxItems truncates to the first N`() {
        val upcomingBirthdays = (1..10).map { upcoming("Person$it", it) }

        val result = upcomingBirthdays.toWidgetUiState(maxItems = 3)

        assertEquals(
            WidgetUiState.Content(
                listOf(
                    WidgetBirthdayItem("Person1", "Tomorrow"),
                    WidgetBirthdayItem("Person2", "In 2 days"),
                    WidgetBirthdayItem("Person3", "In 3 days"),
                ),
            ),
            result,
        )
    }

    @Test
    fun `maxItems of 1 returns a single item`() {
        val upcomingBirthdays = listOf(upcoming("Alice", 0), upcoming("Bob", 1))

        val result = upcomingBirthdays.toWidgetUiState(maxItems = 1)

        assertEquals(WidgetUiState.Content(listOf(WidgetBirthdayItem("Alice", "Today"))), result)
    }

    @Test
    fun `mapping preserves person name`() {
        val result = listOf(upcoming("Zara", 3)).toWidgetUiState()

        assertEquals("Zara", (result as WidgetUiState.Content).items.single().name)
    }

    @Test
    fun `daysUntil 0 maps to Today`() {
        assertEquals("Today", 0.toDaysUntilLabel())
    }

    @Test
    fun `daysUntil 1 maps to Tomorrow`() {
        assertEquals("Tomorrow", 1.toDaysUntilLabel())
    }

    @Test
    fun `daysUntil 2 maps to In 2 days`() {
        assertEquals("In 2 days", 2.toDaysUntilLabel())
    }

    @Test
    fun `daysUntil 364 maps to In 364 days`() {
        assertEquals("In 364 days", 364.toDaysUntilLabel())
    }
}
