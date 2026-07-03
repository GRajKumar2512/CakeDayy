package com.pocketaps.cakeday.feature.editperson.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.pocketaps.cakeday.feature.editperson.EditPersonActions
import com.pocketaps.cakeday.feature.editperson.EditPersonEffect
import com.pocketaps.cakeday.feature.editperson.EditPersonScreen
import com.pocketaps.cakeday.feature.editperson.EditPersonViewModel
import kotlinx.serialization.Serializable

@Serializable
data class EditPersonRoute(val personId: Long? = null)

fun NavGraphBuilder.editPersonScreen(onNavigateBack: () -> Unit) {
    composable<EditPersonRoute> {
        val viewModel = hiltViewModel<EditPersonViewModel>()
        val state by viewModel.uiState.collectAsStateWithLifecycle()

        LaunchedEffect(Unit) {
            viewModel.effect.collect { effect ->
                when (effect) {
                    EditPersonEffect.NavigateBack -> onNavigateBack()
                }
            }
        }

        EditPersonScreen(
            state = state,
            actions = EditPersonActions(
                onNameChange = viewModel::onNameChange,
                onMonthChange = viewModel::onMonthChange,
                onDayChange = viewModel::onDayChange,
                onYearChange = viewModel::onYearChange,
                onNoteChange = viewModel::onNoteChange,
                onGroupSelected = viewModel::onGroupSelected,
                onSaveClick = viewModel::onSaveClick,
                onBackClick = onNavigateBack,
            ),
        )
    }
}
