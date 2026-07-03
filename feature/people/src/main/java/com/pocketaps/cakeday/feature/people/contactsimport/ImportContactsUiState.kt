package com.pocketaps.cakeday.feature.people.contactsimport

import com.pocketaps.cakeday.core.model.ContactBirthday

sealed interface ImportContactsUiState {
    data object Loading : ImportContactsUiState
    data object NoneFound : ImportContactsUiState
    data class Content(
        val candidates: List<ContactBirthday>,
        val selectedIds: Set<String>,
    ) : ImportContactsUiState
}

sealed interface ImportContactsEffect {
    data object NavigateBack : ImportContactsEffect
}

sealed interface ContactsPermissionState {
    data object Checking : ContactsPermissionState
    data object Rationale : ContactsPermissionState
    data object PermanentlyDenied : ContactsPermissionState
    data object Granted : ContactsPermissionState
}

data class ImportContactsActions(
    val onToggleSelected: (String) -> Unit,
    val onImportClick: () -> Unit,
    val onBackClick: () -> Unit,
    val onRequestPermission: () -> Unit,
    val onOpenAppSettings: () -> Unit,
)
