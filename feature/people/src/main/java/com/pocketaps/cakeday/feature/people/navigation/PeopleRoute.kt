package com.pocketaps.cakeday.feature.people.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.pocketaps.cakeday.feature.people.PeopleActions
import com.pocketaps.cakeday.feature.people.PeopleEffect
import com.pocketaps.cakeday.feature.people.PeopleScreen
import com.pocketaps.cakeday.feature.people.PeopleViewModel
import kotlinx.serialization.Serializable

@Serializable
data object PeopleRoute

fun NavGraphBuilder.peopleScreen(
    onAddPerson: () -> Unit,
    onEditPerson: (Long) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenGroups: () -> Unit,
    onOpenImportContacts: () -> Unit,
) {
    composable<PeopleRoute> {
        val viewModel = hiltViewModel<PeopleViewModel>()
        val state by viewModel.uiState.collectAsStateWithLifecycle()

        LaunchedEffect(Unit) {
            viewModel.effect.collect { effect ->
                when (effect) {
                    PeopleEffect.NavigateToAdd -> onAddPerson()
                    is PeopleEffect.NavigateToEdit -> onEditPerson(effect.personId)
                    PeopleEffect.NavigateToSettings -> onOpenSettings()
                    PeopleEffect.NavigateToGroups -> onOpenGroups()
                    PeopleEffect.NavigateToImportContacts -> onOpenImportContacts()
                }
            }
        }

        PeopleScreen(
            state = state,
            actions = PeopleActions(
                onAddClick = viewModel::onAddClick,
                onPersonClick = viewModel::onPersonClick,
                onDeleteClick = viewModel::onDeletePerson,
                onSettingsClick = viewModel::onSettingsClick,
                onManageGroupsClick = viewModel::onManageGroupsClick,
                onImportContactsClick = viewModel::onImportContactsClick,
                onQueryChange = viewModel::onQueryChange,
                onGroupFilterSelected = viewModel::onGroupFilterSelected,
            ),
        )
    }
}
