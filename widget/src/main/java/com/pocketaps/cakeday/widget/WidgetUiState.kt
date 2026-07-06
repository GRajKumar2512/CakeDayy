package com.pocketaps.cakeday.widget

import com.pocketaps.cakeday.core.model.UpcomingBirthday

sealed interface WidgetUiState {
    data object Empty : WidgetUiState
    data object Error : WidgetUiState
    data class Content(val items: List<WidgetBirthdayItem>) : WidgetUiState
}

data class WidgetBirthdayItem(
    val name: String,
    val daysUntilLabel: String,
)

private const val WIDGET_MAX_ITEMS = 5

fun List<UpcomingBirthday>.toWidgetUiState(maxItems: Int = WIDGET_MAX_ITEMS): WidgetUiState {
    if (isEmpty()) return WidgetUiState.Empty
    return WidgetUiState.Content(
        items = take(maxItems).map { upcoming ->
            WidgetBirthdayItem(name = upcoming.person.name, daysUntilLabel = upcoming.daysUntilNext.toDaysUntilLabel())
        },
    )
}

fun Int.toDaysUntilLabel(): String = when (this) {
    0 -> "Today"
    1 -> "Tomorrow"
    else -> "In $this days"
}
