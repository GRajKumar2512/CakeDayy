package com.pocketaps.cakeday.feature.people

import com.pocketaps.cakeday.core.model.Group
import com.pocketaps.cakeday.core.model.UpcomingBirthday

sealed interface PeopleUiState {
    data object Loading : PeopleUiState
    data class Content(
        val people: List<UpcomingBirthday>,
        val groups: List<Group>,
        val query: String,
        val selectedGroupId: Long?,
    ) : PeopleUiState {
        val isFiltering: Boolean get() = query.isNotBlank() || selectedGroupId != null
    }
}

sealed interface PeopleEffect {
    data object NavigateToAdd : PeopleEffect
    data class NavigateToEdit(val personId: Long) : PeopleEffect
    data object NavigateToSettings : PeopleEffect
    data object NavigateToGroups : PeopleEffect
    data object NavigateToImportContacts : PeopleEffect
}

data class PeopleActions(
    val onAddClick: () -> Unit,
    val onPersonClick: (Long) -> Unit,
    val onDeleteClick: (Long) -> Unit,
    val onSettingsClick: () -> Unit,
    val onManageGroupsClick: () -> Unit,
    val onImportContactsClick: () -> Unit,
    val onQueryChange: (String) -> Unit,
    val onGroupFilterSelected: (Long?) -> Unit,
)
