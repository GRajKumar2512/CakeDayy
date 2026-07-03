package com.pocketaps.cakeday.feature.people

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pocketaps.cakeday.core.domain.usecase.DeletePersonUseCase
import com.pocketaps.cakeday.core.domain.usecase.ObserveGroupsUseCase
import com.pocketaps.cakeday.core.domain.usecase.SearchPeopleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class PeopleViewModel @Inject constructor(
    searchPeople: SearchPeopleUseCase,
    observeGroups: ObserveGroupsUseCase,
    private val deletePerson: DeletePersonUseCase,
) : ViewModel() {

    private val effectChannel = Channel<PeopleEffect>(Channel.BUFFERED)
    val effect: Flow<PeopleEffect> = effectChannel.receiveAsFlow()

    private val query = MutableStateFlow("")
    private val selectedGroupId = MutableStateFlow<Long?>(null)

    // The first query emission is not delayed, avoiding a debounce-length blank flash on open;
    // only subsequent user edits are debounced.
    private val debouncedQuery = query
        .drop(1)
        .debounce(SEARCH_DEBOUNCE_MILLIS)
        .onStart { emit(query.value) }

    val uiState: StateFlow<PeopleUiState> =
        combine(debouncedQuery, selectedGroupId) { q, groupId -> q to groupId }
            .distinctUntilChanged()
            .flatMapLatest { (q, groupId) ->
                combine(searchPeople(q, groupId), observeGroups()) { people, groups ->
                    PeopleUiState.Content(
                        people = people,
                        groups = groups,
                        query = q,
                        selectedGroupId = groupId,
                    )
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = PeopleUiState.Loading,
            )

    fun onAddClick() {
        viewModelScope.launch { effectChannel.send(PeopleEffect.NavigateToAdd) }
    }

    fun onPersonClick(personId: Long) {
        viewModelScope.launch { effectChannel.send(PeopleEffect.NavigateToEdit(personId)) }
    }

    fun onSettingsClick() {
        viewModelScope.launch { effectChannel.send(PeopleEffect.NavigateToSettings) }
    }

    fun onManageGroupsClick() {
        viewModelScope.launch { effectChannel.send(PeopleEffect.NavigateToGroups) }
    }

    fun onImportContactsClick() {
        viewModelScope.launch { effectChannel.send(PeopleEffect.NavigateToImportContacts) }
    }

    fun onDeletePerson(personId: Long) {
        viewModelScope.launch { deletePerson(personId) }
    }

    fun onQueryChange(newQuery: String) {
        query.value = newQuery
    }

    fun onGroupFilterSelected(groupId: Long?) {
        selectedGroupId.value = groupId
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
        const val SEARCH_DEBOUNCE_MILLIS = 300L
    }
}
