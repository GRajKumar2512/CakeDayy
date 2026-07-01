package com.pocketaps.cakeday.feature.people

import com.pocketaps.cakeday.core.model.UpcomingBirthday

sealed interface PeopleUiState {
    data object Loading : PeopleUiState
    data object Empty : PeopleUiState
    data class Content(val people: List<UpcomingBirthday>) : PeopleUiState
}

sealed interface PeopleEffect {
    data object NavigateToAdd : PeopleEffect
    data class NavigateToEdit(val personId: Long) : PeopleEffect
}
