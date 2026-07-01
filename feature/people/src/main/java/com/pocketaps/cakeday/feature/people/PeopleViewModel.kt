package com.pocketaps.cakeday.feature.people

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pocketaps.cakeday.core.domain.usecase.DeletePersonUseCase
import com.pocketaps.cakeday.core.domain.usecase.GetUpcomingBirthdaysUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PeopleViewModel @Inject constructor(
    getUpcomingBirthdays: GetUpcomingBirthdaysUseCase,
    private val deletePerson: DeletePersonUseCase,
) : ViewModel() {

    private val effectChannel = Channel<PeopleEffect>(Channel.BUFFERED)
    val effect: Flow<PeopleEffect> = effectChannel.receiveAsFlow()

    val uiState: StateFlow<PeopleUiState> = getUpcomingBirthdays()
        .map { people -> if (people.isEmpty()) PeopleUiState.Empty else PeopleUiState.Content(people) }
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

    fun onDeletePerson(personId: Long) {
        viewModelScope.launch { deletePerson(personId) }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
