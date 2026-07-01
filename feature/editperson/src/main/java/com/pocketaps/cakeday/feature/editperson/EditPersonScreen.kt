package com.pocketaps.cakeday.feature.editperson

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pocketaps.cakeday.core.designsystem.CakeDayyTheme
import com.pocketaps.cakeday.core.designsystem.components.CakeDayyButton
import java.time.Month
import java.time.YearMonth
import java.time.format.TextStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPersonScreen(
    state: EditPersonUiState,
    actions: EditPersonActions,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(if (state.isAddMode) "Add person" else "Edit person") },
                navigationIcon = {
                    IconButton(onClick = actions.onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .padding(contentPadding)
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OutlinedTextField(
                value = state.name,
                onValueChange = actions.onNameChange,
                label = { Text("Name") },
                isError = state.nameError != null,
                supportingText = { state.nameError?.let { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
            )

            BirthDateFields(
                birthMonth = state.birthMonth,
                birthDay = state.birthDay,
                birthYear = state.birthYear,
                actions = actions,
            )

            OutlinedTextField(
                value = state.note,
                onValueChange = actions.onNoteChange,
                label = { Text("Note") },
                modifier = Modifier.fillMaxWidth(),
            )

            CakeDayyButton(
                text = "Save",
                onClick = actions.onSaveClick,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BirthDateFields(
    birthMonth: Int,
    birthDay: Int,
    birthYear: Int?,
    actions: EditPersonActions,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MonthDropdown(
                selectedMonth = birthMonth,
                onMonthSelected = actions.onMonthChange,
                modifier = Modifier.weight(1f),
            )
            DayDropdown(
                selectedDay = birthDay,
                maxDay = maxDayFor(birthMonth, birthYear),
                onDaySelected = actions.onDayChange,
                modifier = Modifier.weight(1f),
            )
        }
        OutlinedTextField(
            value = birthYear?.toString().orEmpty(),
            onValueChange = { text -> actions.onYearChange(text.filter { it.isDigit() }) },
            label = { Text("Year (optional)") },
            supportingText = { Text("Leave blank if unknown") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
        )
    }
}

private fun maxDayFor(month: Int, year: Int?): Int =
    YearMonth.of(year ?: 2024, month).lengthOfMonth()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MonthDropdown(
    selectedMonth: Int,
    onMonthSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val months = remember { Month.entries }
    val locale = LocalConfiguration.current.locales[0]
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = months[selectedMonth - 1].getDisplayName(TextStyle.FULL, locale),
            onValueChange = {},
            readOnly = true,
            label = { Text("Month") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            months.forEach { month ->
                DropdownMenuItem(
                    text = { Text(month.getDisplayName(TextStyle.FULL, locale)) },
                    onClick = {
                        onMonthSelected(month.value)
                        expanded = false
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DayDropdown(
    selectedDay: Int,
    maxDay: Int,
    onDaySelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val days = remember(maxDay) { 1..maxDay }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = selectedDay.toString(),
            onValueChange = {},
            readOnly = true,
            label = { Text("Day") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            days.forEach { day ->
                DropdownMenuItem(
                    text = { Text(day.toString()) },
                    onClick = {
                        onDaySelected(day)
                        expanded = false
                    },
                )
            }
        }
    }
}

private val previewActions = EditPersonActions(
    onNameChange = {},
    onMonthChange = {},
    onDayChange = {},
    onYearChange = {},
    onNoteChange = {},
    onSaveClick = {},
    onBackClick = {},
)

@Preview(showBackground = true)
@Composable
private fun EditPersonScreenAddPreview() {
    CakeDayyTheme {
        EditPersonScreen(
            state = EditPersonUiState(isLoading = false, personId = null),
            actions = previewActions,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EditPersonScreenEditPreview() {
    CakeDayyTheme {
        EditPersonScreen(
            state = EditPersonUiState(
                isLoading = false,
                personId = 1L,
                name = "Alice",
                birthMonth = 5,
                birthDay = 10,
                birthYear = 1994,
                note = "Loves chocolate cake",
            ),
            actions = previewActions,
        )
    }
}
