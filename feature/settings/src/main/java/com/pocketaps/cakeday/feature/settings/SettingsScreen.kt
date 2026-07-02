package com.pocketaps.cakeday.feature.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pocketaps.cakeday.core.designsystem.CakeDayyTheme
import com.pocketaps.cakeday.core.model.ReminderLead

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: SettingsUiState,
    showPermissionRationale: Boolean,
    onLeadSelected: (ReminderLead) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
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
                .padding(contentPadding)
                .padding(16.dp)
                .selectableGroup(),
        ) {
            Text(
                text = "Remind me",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            ReminderLead.entries.forEach { lead ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = state.selectedLead == lead,
                            onClick = { onLeadSelected(lead) },
                            role = Role.RadioButton,
                        )
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(selected = state.selectedLead == lead, onClick = null)
                    Text(text = lead.label(), modifier = Modifier.padding(start = 8.dp))
                }
            }
            if (showPermissionRationale) {
                Text(
                    text = "Notifications are turned off for CakeDayy. " +
                        "Enable them in system settings to get birthday reminders.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 16.dp),
                )
            }
        }
    }
}

private fun ReminderLead.label(): String = when (this) {
    ReminderLead.ON_THE_DAY -> "On the day"
    ReminderLead.ONE_DAY_BEFORE -> "1 day before"
    ReminderLead.THREE_DAYS_BEFORE -> "3 days before"
    ReminderLead.ONE_WEEK_BEFORE -> "1 week before"
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    CakeDayyTheme {
        SettingsScreen(
            state = SettingsUiState(isLoading = false, selectedLead = ReminderLead.ONE_DAY_BEFORE),
            showPermissionRationale = false,
            onLeadSelected = {},
            onBackClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenRationalePreview() {
    CakeDayyTheme {
        SettingsScreen(
            state = SettingsUiState(isLoading = false, selectedLead = ReminderLead.ON_THE_DAY),
            showPermissionRationale = true,
            onLeadSelected = {},
            onBackClick = {},
        )
    }
}
