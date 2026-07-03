package com.pocketaps.cakeday.feature.groups

import com.pocketaps.cakeday.core.model.Group

sealed interface GroupsUiState {
    data object Loading : GroupsUiState
    data class Content(val groups: List<Group>) : GroupsUiState
}

data class GroupsActions(
    val onSaveGroup: (Group) -> Unit,
    val onDeleteGroup: (Long) -> Unit,
    val onBackClick: () -> Unit,
)

val GroupColorPalette: List<String> = listOf(
    "#F44336",
    "#E91E63",
    "#9C27B0",
    "#3F51B5",
    "#2196F3",
    "#4CAF50",
    "#FF9800",
    "#795548",
)
