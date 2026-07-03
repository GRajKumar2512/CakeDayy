package com.pocketaps.cakeday.feature.people

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pocketaps.cakeday.core.designsystem.CakeDayyTheme
import com.pocketaps.cakeday.core.designsystem.components.EmptyState
import com.pocketaps.cakeday.core.model.Group
import com.pocketaps.cakeday.core.model.Person
import com.pocketaps.cakeday.core.model.UpcomingBirthday
import com.pocketaps.cakeday.core.ui.PersonRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeopleScreen(
    state: PeopleUiState,
    actions: PeopleActions,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("CakeDayy") },
                actions = { PeopleOverflowMenu(actions = actions) },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = actions.onAddClick) {
                Icon(Icons.Default.Add, contentDescription = "Add person")
            }
        },
    ) { contentPadding ->
        when (state) {
            PeopleUiState.Loading -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }

            is PeopleUiState.Content -> PeopleContent(
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
private fun PeopleOverflowMenu(
    actions: PeopleActions,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    IconButton(onClick = { expanded = true }, modifier = modifier) {
        Icon(Icons.Default.MoreVert, contentDescription = "More options")
    }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        DropdownMenuItem(
            text = { Text("Import from contacts") },
            onClick = {
                expanded = false
                actions.onImportContactsClick()
            },
        )
        DropdownMenuItem(
            text = { Text("Manage groups") },
            onClick = {
                expanded = false
                actions.onManageGroupsClick()
            },
        )
        DropdownMenuItem(
            text = { Text("Settings") },
            onClick = {
                expanded = false
                actions.onSettingsClick()
            },
        )
    }
}

@Composable
private fun PeopleContent(
    state: PeopleUiState.Content,
    actions: PeopleActions,
    modifier: Modifier = Modifier,
) {
    if (!state.isFiltering && state.people.isEmpty()) {
        EmptyState(
            title = "No birthdays yet",
            message = "Tap the + button to add someone.",
            modifier = modifier,
        )
        return
    }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = state.query,
            onValueChange = actions.onQueryChange,
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            placeholder = { Text("Search name or note") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        )

        if (state.groups.isNotEmpty()) {
            GroupFilterRow(
                groups = state.groups,
                selectedGroupId = state.selectedGroupId,
                onGroupFilterSelected = actions.onGroupFilterSelected,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }

        if (state.people.isEmpty()) {
            Text(
                text = "No matches",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(16.dp),
            )
        } else {
            PeopleList(people = state.people, actions = actions, modifier = Modifier.fillMaxSize())
        }
    }
}

@Composable
private fun GroupFilterRow(
    groups: List<Group>,
    selectedGroupId: Long?,
    onGroupFilterSelected: (Long?) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            FilterChip(
                selected = selectedGroupId == null,
                onClick = { onGroupFilterSelected(null) },
                label = { Text("All") },
            )
        }
        items(groups, key = { it.id }) { group ->
            FilterChip(
                selected = selectedGroupId == group.id,
                onClick = { onGroupFilterSelected(if (selectedGroupId == group.id) null else group.id) },
                label = { Text(group.name) },
            )
        }
    }
}

@Composable
private fun PeopleList(
    people: List<UpcomingBirthday>,
    actions: PeopleActions,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(people, key = { it.person.id }) { upcomingBirthday ->
            val dismissState = rememberSwipeToDismissBoxState(
                confirmValueChange = { value ->
                    if (value != SwipeToDismissBoxValue.Settled) {
                        actions.onDeleteClick(upcomingBirthday.person.id)
                        true
                    } else {
                        false
                    }
                },
            )
            SwipeToDismissBox(
                state = dismissState,
                backgroundContent = {},
            ) {
                PersonRow(
                    upcomingBirthday = upcomingBirthday,
                    onClick = { actions.onPersonClick(upcomingBirthday.person.id) },
                )
            }
        }
    }
}

private val previewActions = PeopleActions(
    onAddClick = {},
    onPersonClick = {},
    onDeleteClick = {},
    onSettingsClick = {},
    onManageGroupsClick = {},
    onImportContactsClick = {},
    onQueryChange = {},
    onGroupFilterSelected = {},
)

@Preview(showBackground = true)
@Composable
private fun PeopleScreenLoadingPreview() {
    CakeDayyTheme {
        PeopleScreen(
            state = PeopleUiState.Loading,
            actions = previewActions,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PeopleScreenEmptyPreview() {
    CakeDayyTheme {
        PeopleScreen(
            state = PeopleUiState.Content(
                people = emptyList(),
                groups = emptyList(),
                query = "",
                selectedGroupId = null,
            ),
            actions = previewActions,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PeopleScreenContentPreview() {
    CakeDayyTheme {
        PeopleScreen(
            state = PeopleUiState.Content(
                people = listOf(
                    UpcomingBirthday(
                        person = Person(id = 1L, name = "Alice", birthMonth = 5, birthDay = 10, birthYear = 1994),
                        daysUntilNext = 5,
                        nextAge = 32,
                    ),
                    UpcomingBirthday(
                        person = Person(id = 2L, name = "Bob", birthMonth = 1, birthDay = 1, birthYear = null),
                        daysUntilNext = 40,
                        nextAge = null,
                    ),
                ),
                groups = listOf(Group(id = 1L, name = "Family", colorHex = "#F44336")),
                query = "",
                selectedGroupId = null,
            ),
            actions = previewActions,
        )
    }
}
