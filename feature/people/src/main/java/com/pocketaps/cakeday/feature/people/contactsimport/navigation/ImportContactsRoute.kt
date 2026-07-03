package com.pocketaps.cakeday.feature.people.contactsimport.navigation

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.pocketaps.cakeday.feature.people.contactsimport.ContactsPermissionState
import com.pocketaps.cakeday.feature.people.contactsimport.ImportContactsActions
import com.pocketaps.cakeday.feature.people.contactsimport.ImportContactsEffect
import com.pocketaps.cakeday.feature.people.contactsimport.ImportContactsScreen
import com.pocketaps.cakeday.feature.people.contactsimport.ImportContactsViewModel
import kotlinx.serialization.Serializable

@Serializable
data object ImportContactsRoute

fun NavGraphBuilder.importContactsScreen(onNavigateBack: () -> Unit) {
    composable<ImportContactsRoute> {
        val viewModel = hiltViewModel<ImportContactsViewModel>()
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        val context = LocalContext.current

        var permissionState by remember {
            mutableStateOf<ContactsPermissionState>(ContactsPermissionState.Checking)
        }

        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
        ) { granted ->
            val activity = context as? Activity
            permissionState = when {
                granted -> ContactsPermissionState.Granted
                activity != null &&
                    ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_CONTACTS) ->
                    ContactsPermissionState.Rationale
                else -> ContactsPermissionState.PermanentlyDenied
            }
        }

        LaunchedEffect(Unit) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CONTACTS,
            ) == PackageManager.PERMISSION_GRANTED
            permissionState = if (granted) ContactsPermissionState.Granted else ContactsPermissionState.Rationale
        }

        LaunchedEffect(permissionState) {
            if (permissionState == ContactsPermissionState.Granted) {
                viewModel.onPermissionGranted()
            }
        }

        LaunchedEffect(Unit) {
            viewModel.effect.collect { effect ->
                when (effect) {
                    ImportContactsEffect.NavigateBack -> onNavigateBack()
                }
            }
        }

        ImportContactsScreen(
            state = state,
            permissionState = permissionState,
            actions = ImportContactsActions(
                onToggleSelected = viewModel::onToggleSelected,
                onImportClick = viewModel::onImportClick,
                onBackClick = onNavigateBack,
                onRequestPermission = { permissionLauncher.launch(Manifest.permission.READ_CONTACTS) },
                onOpenAppSettings = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                },
            ),
        )
    }
}
