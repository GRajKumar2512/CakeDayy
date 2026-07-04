package com.pocketaps.cakeday.feature.settings.backup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pocketaps.cakeday.core.designsystem.CakeDayyTheme
import com.pocketaps.cakeday.core.designsystem.components.CakeDayyButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    state: BackupUiState,
    onExportClick: () -> Unit,
    onImportConfirmed: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showImportConfirmation by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Backup & Restore") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Export all your people, groups, and settings to a JSON file, " +
                    "or restore from a previously exported file.",
                style = MaterialTheme.typography.bodyMedium,
            )
            CakeDayyButton(text = "Export backup", onClick = onExportClick, modifier = Modifier.fillMaxWidth())
            CakeDayyButton(
                text = "Import backup",
                onClick = { showImportConfirmation = true },
                modifier = Modifier.fillMaxWidth(),
            )
            BackupStatus(state = state)
        }
    }

    if (showImportConfirmation) {
        ImportConfirmationDialog(
            onConfirm = {
                showImportConfirmation = false
                onImportConfirmed()
            },
            onDismiss = { showImportConfirmation = false },
        )
    }
}

@Composable
private fun BackupStatus(state: BackupUiState, modifier: Modifier = Modifier) {
    when (state) {
        BackupUiState.Idle -> Unit
        BackupUiState.Working -> CircularProgressIndicator(modifier = modifier)
        BackupUiState.ExportSuccess -> Text(text = "Backup exported.", modifier = modifier)
        BackupUiState.ImportSuccess -> Text(text = "Backup restored.", modifier = modifier)
        is BackupUiState.Error -> Text(
            text = state.reason.message(),
            color = MaterialTheme.colorScheme.error,
            modifier = modifier,
        )
    }
}

@Composable
private fun ImportConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Replace current data?") },
        text = { Text("Importing will replace all current people and groups. This can't be undone.") },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Import") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

private fun BackupErrorReason.message(): String = when (this) {
    BackupErrorReason.MALFORMED_JSON -> "That file isn't a valid CakeDayy backup."
    BackupErrorReason.UNSUPPORTED_SCHEMA_VERSION -> "This backup was made with a newer version of the app."
    BackupErrorReason.INVALID_PAYLOAD -> "This backup file is corrupted or invalid."
    BackupErrorReason.FILE_READ_WRITE_FAILED -> "Couldn't access the selected file."
}

@Preview(showBackground = true)
@Composable
private fun BackupScreenIdlePreview() {
    CakeDayyTheme {
        BackupScreen(
            state = BackupUiState.Idle,
            onExportClick = {},
            onImportConfirmed = {},
            onBackClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BackupScreenErrorPreview() {
    CakeDayyTheme {
        BackupScreen(
            state = BackupUiState.Error(BackupErrorReason.MALFORMED_JSON),
            onExportClick = {},
            onImportConfirmed = {},
            onBackClick = {},
        )
    }
}
