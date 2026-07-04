package com.pocketaps.cakeday.feature.settings.navigation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.pocketaps.cakeday.feature.settings.SettingsActions
import com.pocketaps.cakeday.feature.settings.SettingsScreen
import com.pocketaps.cakeday.feature.settings.SettingsViewModel
import kotlinx.serialization.Serializable

@Serializable
data object SettingsRoute

fun NavGraphBuilder.settingsScreen(onNavigateBack: () -> Unit, onOpenBackup: () -> Unit) {
    composable<SettingsRoute> {
        val viewModel = hiltViewModel<SettingsViewModel>()
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        val context = LocalContext.current
        var showPermissionRationale by remember { mutableStateOf(false) }

        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
        ) { granted -> showPermissionRationale = !granted }

        SettingsScreen(
            state = state,
            showPermissionRationale = showPermissionRationale,
            actions = SettingsActions(
                onLeadSelected = { lead ->
                    viewModel.onLeadSelected(lead)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS,
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                },
                onThemeModeSelected = viewModel::onThemeModeSelected,
                onOpenBackup = onOpenBackup,
                onBackClick = onNavigateBack,
            ),
        )
    }
}
