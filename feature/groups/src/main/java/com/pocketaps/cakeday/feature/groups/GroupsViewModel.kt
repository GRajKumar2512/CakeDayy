package com.pocketaps.cakeday.feature.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pocketaps.cakeday.core.domain.usecase.DeleteGroupUseCase
import com.pocketaps.cakeday.core.domain.usecase.ObserveGroupsUseCase
import com.pocketaps.cakeday.core.domain.usecase.SaveGroupUseCase
import com.pocketaps.cakeday.core.model.Group
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupsViewModel @Inject constructor(
    observeGroups: ObserveGroupsUseCase,
    private val saveGroup: SaveGroupUseCase,
    private val deleteGroup: DeleteGroupUseCase,
) : ViewModel() {

    val uiState: StateFlow<GroupsUiState> = observeGroups()
        .map { groups -> GroupsUiState.Content(groups) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = GroupsUiState.Loading,
        )

    fun onSaveGroup(group: Group) {
        viewModelScope.launch { saveGroup(group) }
    }

    fun onDeleteGroup(id: Long) {
        viewModelScope.launch { deleteGroup(id) }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
