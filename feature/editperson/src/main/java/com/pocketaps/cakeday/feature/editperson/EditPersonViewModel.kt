package com.pocketaps.cakeday.feature.editperson

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pocketaps.cakeday.core.domain.usecase.ObserveGroupsUseCase
import com.pocketaps.cakeday.core.domain.usecase.ObservePersonUseCase
import com.pocketaps.cakeday.core.domain.usecase.SavePersonUseCase
import com.pocketaps.cakeday.core.model.Person
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class EditPersonViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val observePerson: ObservePersonUseCase,
    observeGroups: ObserveGroupsUseCase,
    private val savePerson: SavePersonUseCase,
) : ViewModel() {

    // Key must match the `personId` property name of navigation.EditPersonRoute.
    private val personId = savedStateHandle.get<Long?>("personId")
    private var loadedPerson: Person? = null

    private val _uiState = MutableStateFlow(EditPersonUiState(personId = personId))
    val uiState: StateFlow<EditPersonUiState> = _uiState.asStateFlow()

    private val effectChannel = Channel<EditPersonEffect>(Channel.BUFFERED)
    val effect: Flow<EditPersonEffect> = effectChannel.receiveAsFlow()

    init {
        observeGroups().onEach { groups -> _uiState.update { it.copy(groups = groups) } }.launchIn(viewModelScope)

        val id = personId
        if (id != null) {
            viewModelScope.launch {
                val person = observePerson(id).firstOrNull()
                if (person != null) {
                    loadedPerson = person
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            name = person.name,
                            birthMonth = person.birthMonth,
                            birthDay = person.birthDay,
                            birthYear = person.birthYear,
                            note = person.note.orEmpty(),
                            selectedGroupId = person.groupId,
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        } else {
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun onNameChange(name: String) {
        _uiState.update { it.copy(name = name, nameError = null) }
    }

    fun onMonthChange(month: Int) {
        _uiState.update { current ->
            val maxDay = daysInMonth(month, current.birthYear)
            current.copy(
                birthMonth = month,
                birthDay = current.birthDay.coerceAtMost(maxDay),
            )
        }
    }

    fun onDayChange(day: Int) {
        _uiState.update { it.copy(birthDay = day) }
    }

    fun onYearChange(yearText: String) {
        val year = yearText.toIntOrNull()
        _uiState.update { current ->
            val maxDay = daysInMonth(current.birthMonth, year)
            current.copy(
                birthYear = year,
                birthDay = current.birthDay.coerceAtMost(maxDay),
            )
        }
    }

    fun onNoteChange(note: String) {
        _uiState.update { it.copy(note = note) }
    }

    fun onGroupSelected(groupId: Long?) {
        _uiState.update { it.copy(selectedGroupId = groupId) }
    }

    fun onSaveClick() {
        val state = _uiState.value
        if (state.name.isBlank()) {
            _uiState.update { it.copy(nameError = "Name is required") }
            return
        }

        val person = (loadedPerson ?: Person(name = "", birthMonth = 1, birthDay = 1)).copy(
            name = state.name.trim(),
            birthMonth = state.birthMonth,
            birthDay = state.birthDay,
            birthYear = state.birthYear,
            note = state.note.ifBlank { null },
            groupId = state.selectedGroupId,
        )

        viewModelScope.launch {
            savePerson(person)
            effectChannel.send(EditPersonEffect.NavigateBack)
        }
    }

    private fun daysInMonth(month: Int, year: Int?): Int =
        YearMonth.of(year ?: REFERENCE_LEAP_YEAR, month).lengthOfMonth()

    private companion object {
        const val REFERENCE_LEAP_YEAR = 2024
    }
}
