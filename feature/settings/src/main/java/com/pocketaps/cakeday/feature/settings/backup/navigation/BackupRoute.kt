package com.pocketaps.cakeday.feature.settings.backup.navigation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.pocketaps.cakeday.feature.settings.backup.BackupScreen
import com.pocketaps.cakeday.feature.settings.backup.BackupViewModel
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data object BackupRoute

fun NavGraphBuilder.backupScreen(onNavigateBack: () -> Unit) {
    composable<BackupRoute> {
        val viewModel = hiltViewModel<BackupViewModel>()
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        val context = LocalContext.current
        val scope = rememberCoroutineScope()

        val exportLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.CreateDocument("application/json"),
        ) { uri ->
            if (uri == null) return@rememberLauncherForActivityResult
            scope.launch {
                val json = viewModel.buildExportJson()
                val wrote = runCatching {
                    context.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) }
                }.isSuccess
                viewModel.onExportResult(wrote)
            }
        }

        val importLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument(),
        ) { uri ->
            if (uri == null) return@rememberLauncherForActivityResult
            scope.launch {
                val json = runCatching {
                    context.contentResolver.openInputStream(uri)?.use { it.readBytes() }?.decodeToString()
                }.getOrNull()
                if (json == null) viewModel.onFileReadFailed() else viewModel.onJsonPicked(json)
            }
        }

        BackupScreen(
            state = state,
            onExportClick = { exportLauncher.launch("cakeday-backup.json") },
            onImportConfirmed = { importLauncher.launch(arrayOf("application/json")) },
            onBackClick = onNavigateBack,
        )
    }
}
