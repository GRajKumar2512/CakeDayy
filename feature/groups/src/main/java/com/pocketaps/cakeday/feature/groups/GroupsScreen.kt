package com.pocketaps.cakeday.feature.groups

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pocketaps.cakeday.core.designsystem.CakeDayyTheme
import com.pocketaps.cakeday.core.designsystem.components.CakeDayyCard
import com.pocketaps.cakeday.core.designsystem.components.EmptyState
import com.pocketaps.cakeday.core.model.Group

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupsScreen(
    state: GroupsUiState,
    actions: GroupsActions,
    modifier: Modifier = Modifier,
) {
    var editingGroup by remember { mutableStateOf<Group?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Groups") },
                navigationIcon = {
                    IconButton(onClick = actions.onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "New group")
            }
        },
    ) { contentPadding ->
        GroupsContent(
            state = state,
            onEditClick = { editingGroup = it },
            onDeleteClick = actions.onDeleteGroup,
            modifier = Modifier.padding(contentPadding),
        )
    }

    GroupsDialogs(
        showCreateDialog = showCreateDialog,
        editingGroup = editingGroup,
        onSaveGroup = actions.onSaveGroup,
        onDismissCreate = { showCreateDialog = false },
        onDismissEdit = { editingGroup = null },
    )
}

@Composable
private fun GroupsContent(
    state: GroupsUiState,
    onEditClick: (Group) -> Unit,
    onDeleteClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (state) {
        GroupsUiState.Loading -> Unit

        is GroupsUiState.Content -> if (state.groups.isEmpty()) {
            EmptyState(
                title = "No groups yet",
                message = "Tap the + button to create one.",
                modifier = modifier,
            )
        } else {
            GroupList(
                groups = state.groups,
                onEditClick = onEditClick,
                onDeleteClick = onDeleteClick,
                modifier = modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun GroupsDialogs(
    showCreateDialog: Boolean,
    editingGroup: Group?,
    onSaveGroup: (Group) -> Unit,
    onDismissCreate: () -> Unit,
    onDismissEdit: () -> Unit,
) {
    if (showCreateDialog) {
        GroupEditorDialog(
            initialGroup = null,
            onDismiss = onDismissCreate,
            onSave = { group ->
                onSaveGroup(group)
                onDismissCreate()
            },
        )
    }

    editingGroup?.let { group ->
        GroupEditorDialog(
            initialGroup = group,
            onDismiss = onDismissEdit,
            onSave = { updated ->
                onSaveGroup(updated)
                onDismissEdit()
            },
        )
    }
}

@Composable
private fun GroupList(
    groups: List<Group>,
    onEditClick: (Group) -> Unit,
    onDeleteClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(groups, key = { it.id }) { group ->
            val dismissState = rememberSwipeToDismissBoxState(
                confirmValueChange = { value ->
                    if (value != SwipeToDismissBoxValue.Settled) {
                        onDeleteClick(group.id)
                        true
                    } else {
                        false
                    }
                },
            )
            SwipeToDismissBox(state = dismissState, backgroundContent = {}) {
                GroupRow(group = group, onEditClick = { onEditClick(group) })
            }
        }
    }
}

@Composable
private fun GroupRow(
    group: Group,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CakeDayyCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ColorSwatch(colorHex = group.colorHex, size = 20.dp, selected = false)
            Text(text = group.name, modifier = Modifier.weight(1f))
            IconButton(onClick = onEditClick) {
                Icon(Icons.Default.Edit, contentDescription = "Edit ${group.name}")
            }
        }
    }
}

@Composable
private fun GroupEditorDialog(
    initialGroup: Group?,
    onDismiss: () -> Unit,
    onSave: (Group) -> Unit,
) {
    var name by remember { mutableStateOf(initialGroup?.name.orEmpty()) }
    var colorHex by remember { mutableStateOf(initialGroup?.colorHex ?: GroupColorPalette.first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialGroup == null) "New group" else "Rename group") },
        text = {
            GroupEditorFields(
                name = name,
                onNameChange = { name = it },
                colorHex = colorHex,
                onColorSelected = { colorHex = it },
            )
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = {
                    val base = initialGroup ?: Group(name = "", colorHex = colorHex)
                    onSave(base.copy(name = name.trim(), colorHex = colorHex))
                },
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

@Composable
private fun GroupEditorFields(
    name: String,
    onNameChange: (String) -> Unit,
    colorHex: String,
    onColorSelected: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GroupColorPalette.forEach { swatch ->
                ColorSwatch(
                    colorHex = swatch,
                    size = 32.dp,
                    selected = swatch == colorHex,
                    onClick = { onColorSelected(swatch) },
                )
            }
        }
    }
}

@Composable
private fun ColorSwatch(
    colorHex: String,
    size: Dp,
    selected: Boolean,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val color = remember(colorHex) { Color(android.graphics.Color.parseColor(colorHex)) }
    Box(
        modifier = modifier
            .size(size)
            .background(color, CircleShape)
            .then(if (selected) Modifier.border(2.dp, Color.Black, CircleShape) else Modifier)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
    )
}

private val previewActions = GroupsActions(onSaveGroup = {}, onDeleteGroup = {}, onBackClick = {})

@Preview(showBackground = true)
@Composable
private fun GroupsScreenContentPreview() {
    CakeDayyTheme {
        GroupsScreen(
            state = GroupsUiState.Content(
                groups = listOf(
                    Group(id = 1L, name = "Family", colorHex = "#F44336"),
                    Group(id = 2L, name = "Friends", colorHex = "#2196F3"),
                ),
            ),
            actions = previewActions,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GroupsScreenEmptyPreview() {
    CakeDayyTheme {
        GroupsScreen(
            state = GroupsUiState.Content(groups = emptyList()),
            actions = previewActions,
        )
    }
}
