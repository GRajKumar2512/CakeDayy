package com.pocketaps.cakeday.feature.people.contactsimport

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pocketaps.cakeday.core.domain.usecase.GetImportableContactsUseCase
import com.pocketaps.cakeday.core.domain.usecase.SavePersonUseCase
import com.pocketaps.cakeday.core.model.ContactBirthday
import com.pocketaps.cakeday.core.model.Person
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ImportContactsViewModel @Inject constructor(
    private val getImportableContacts: GetImportableContactsUseCase,
    private val savePerson: SavePersonUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ImportContactsUiState>(ImportContactsUiState.Loading)
    val uiState: StateFlow<ImportContactsUiState> = _uiState.asStateFlow()

    private val effectChannel = Channel<ImportContactsEffect>(Channel.BUFFERED)
    val effect: Flow<ImportContactsEffect> = effectChannel.receiveAsFlow()

    fun onPermissionGranted() {
        viewModelScope.launch {
            _uiState.value = ImportContactsUiState.Loading
            val candidates = getImportableContacts()
            _uiState.value = if (candidates.isEmpty()) {
                ImportContactsUiState.NoneFound
            } else {
                ImportContactsUiState.Content(
                    candidates = candidates,
                    selectedIds = candidates.map { it.contactId }.toSet(),
                )
            }
        }
    }

    fun onToggleSelected(contactId: String) {
        val state = _uiState.value
        if (state !is ImportContactsUiState.Content) return
        val selected = state.selectedIds
        _uiState.value = state.copy(
            selectedIds = if (contactId in selected) selected - contactId else selected + contactId,
        )
    }

    fun onImportClick() {
        val state = _uiState.value
        if (state !is ImportContactsUiState.Content) return
        viewModelScope.launch {
            state.candidates
                .filter { it.contactId in state.selectedIds }
                .forEach { savePerson(it.toPerson()) }
            effectChannel.send(ImportContactsEffect.NavigateBack)
        }
    }

    private fun ContactBirthday.toPerson() = Person(
        name = name,
        birthMonth = birthMonth,
        birthDay = birthDay,
        birthYear = birthYear,
    )
}
