package com.pocketaps.cakeday.feature.people

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pocketaps.cakeday.core.designsystem.CakeDayyTheme
import com.pocketaps.cakeday.core.designsystem.components.EmptyState
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
                actions = {
                    IconButton(onClick = actions.onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
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

            PeopleUiState.Empty -> EmptyState(
                title = "No birthdays yet",
                message = "Tap the + button to add someone.",
                modifier = Modifier.padding(contentPadding),
            )

            is PeopleUiState.Content -> PeopleList(
                people = state.people,
                actions = actions,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
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
            state = PeopleUiState.Empty,
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
            ),
            actions = previewActions,
        )
    }
}
