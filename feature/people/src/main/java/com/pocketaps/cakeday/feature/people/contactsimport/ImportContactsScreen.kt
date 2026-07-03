package com.pocketaps.cakeday.feature.people.contactsimport

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pocketaps.cakeday.core.designsystem.CakeDayyTheme
import com.pocketaps.cakeday.core.designsystem.components.CakeDayyButton
import com.pocketaps.cakeday.core.designsystem.components.EmptyState
import com.pocketaps.cakeday.core.model.ContactBirthday

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportContactsScreen(
    state: ImportContactsUiState,
    permissionState: ContactsPermissionState,
    actions: ImportContactsActions,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Import from contacts") },
                navigationIcon = {
                    IconButton(onClick = actions.onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { contentPadding ->
        when (permissionState) {
            ContactsPermissionState.Checking -> Unit

            ContactsPermissionState.Rationale -> PermissionMessage(
                title = "Contacts access needed",
                message = "CakeDayy reads birthdays already saved on your device contacts " +
                    "so you can quickly add them here.",
                buttonText = "Grant access",
                onButtonClick = actions.onRequestPermission,
                modifier = Modifier.padding(contentPadding),
            )

            ContactsPermissionState.PermanentlyDenied -> PermissionMessage(
                title = "Contacts access denied",
                message = "Enable the Contacts permission in system settings to import birthdays.",
                buttonText = "Open settings",
                onButtonClick = actions.onOpenAppSettings,
                modifier = Modifier.padding(contentPadding),
            )

            ContactsPermissionState.Granted -> ImportContent(
                state = state,
                actions = actions,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
            )
        }
    }
}

@Composable
private fun PermissionMessage(
    title: String,
    message: String,
    buttonText: String,
    onButtonClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    EmptyState(
        title = title,
        message = message,
        action = { CakeDayyButton(text = buttonText, onClick = onButtonClick) },
        modifier = modifier,
    )
}

@Composable
private fun ImportContent(
    state: ImportContactsUiState,
    actions: ImportContactsActions,
    modifier: Modifier = Modifier,
) {
    when (state) {
        ImportContactsUiState.Loading -> Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            CircularProgressIndicator()
        }

        ImportContactsUiState.NoneFound -> EmptyState(
            title = "Nothing new to import",
            message = "Every contact birthday is already in your list.",
            modifier = modifier,
        )

        is ImportContactsUiState.Content -> Column(modifier = modifier) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(state.candidates, key = { it.contactId }) { candidate ->
                    CandidateRow(
                        candidate = candidate,
                        selected = candidate.contactId in state.selectedIds,
                        onToggle = { actions.onToggleSelected(candidate.contactId) },
                    )
                }
            }
            CakeDayyButton(
                text = "Import ${state.selectedIds.size} selected",
                onClick = actions.onImportClick,
                enabled = state.selectedIds.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            )
        }
    }
}

@Composable
private fun CandidateRow(
    candidate: ContactBirthday,
    selected: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = selected, onCheckedChange = { onToggle() })
        Column {
            Text(text = candidate.name, style = MaterialTheme.typography.titleMedium)
            Text(
                text = "${candidate.birthMonth}/${candidate.birthDay}" +
                    (candidate.birthYear?.let { "/$it" } ?: ""),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

private val previewActions = ImportContactsActions(
    onToggleSelected = {},
    onImportClick = {},
    onBackClick = {},
    onRequestPermission = {},
    onOpenAppSettings = {},
)

@Preview(showBackground = true)
@Composable
private fun ImportContactsScreenRationalePreview() {
    CakeDayyTheme {
        ImportContactsScreen(
            state = ImportContactsUiState.Loading,
            permissionState = ContactsPermissionState.Rationale,
            actions = previewActions,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ImportContactsScreenContentPreview() {
    CakeDayyTheme {
        ImportContactsScreen(
            state = ImportContactsUiState.Content(
                candidates = listOf(
                    ContactBirthday(contactId = "1", name = "Alice", birthMonth = 5, birthDay = 10, birthYear = 1994),
                    ContactBirthday(contactId = "2", name = "Bob", birthMonth = 1, birthDay = 1, birthYear = null),
                ),
                selectedIds = setOf("1", "2"),
            ),
            permissionState = ContactsPermissionState.Granted,
            actions = previewActions,
        )
    }
}
