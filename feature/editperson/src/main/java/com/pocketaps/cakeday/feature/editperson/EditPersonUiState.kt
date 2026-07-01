package com.pocketaps.cakeday.feature.editperson

import java.time.LocalDate

data class EditPersonUiState(
    val isLoading: Boolean = true,
    val personId: Long? = null,
    val name: String = "",
    val nameError: String? = null,
    val birthMonth: Int = LocalDate.now().monthValue,
    val birthDay: Int = 1,
    val birthYear: Int? = null,
    val note: String = "",
) {
    val isAddMode: Boolean get() = personId == null
}

sealed interface EditPersonEffect {
    data object NavigateBack : EditPersonEffect
}

data class EditPersonActions(
    val onNameChange: (String) -> Unit,
    val onMonthChange: (Int) -> Unit,
    val onDayChange: (Int) -> Unit,
    val onYearChange: (String) -> Unit,
    val onNoteChange: (String) -> Unit,
    val onSaveClick: () -> Unit,
    val onBackClick: () -> Unit,
)
