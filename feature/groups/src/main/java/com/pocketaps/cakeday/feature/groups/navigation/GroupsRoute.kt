package com.pocketaps.cakeday.feature.groups.navigation

import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.pocketaps.cakeday.feature.groups.GroupsActions
import com.pocketaps.cakeday.feature.groups.GroupsScreen
import com.pocketaps.cakeday.feature.groups.GroupsViewModel
import kotlinx.serialization.Serializable

@Serializable
data object GroupsRoute

fun NavGraphBuilder.groupsScreen(onNavigateBack: () -> Unit) {
    composable<GroupsRoute> {
        val viewModel = hiltViewModel<GroupsViewModel>()
        val state by viewModel.uiState.collectAsStateWithLifecycle()

        GroupsScreen(
            state = state,
            actions = GroupsActions(
                onSaveGroup = viewModel::onSaveGroup,
                onDeleteGroup = viewModel::onDeleteGroup,
                onBackClick = onNavigateBack,
            ),
        )
    }
}
