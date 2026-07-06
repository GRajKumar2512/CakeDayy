package com.pocketaps.cakeday.feature.settings.backup

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.pocketaps.cakeday.core.designsystem.CakeDayyTheme
import org.junit.Rule
import org.junit.Test

class BackupScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun exportButton_invokesOnExportClick() {
        var clicked = false
        composeTestRule.setContent {
            CakeDayyTheme {
                BackupScreen(
                    state = BackupUiState.Idle,
                    onExportClick = { clicked = true },
                    onImportConfirmed = {},
                    onBackClick = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Export backup").performClick()

        assert(clicked)
    }

    @Test
    fun importButton_showsConfirmationDialog_andConfirmingInvokesCallback() {
        var confirmed = false
        composeTestRule.setContent {
            CakeDayyTheme {
                BackupScreen(
                    state = BackupUiState.Idle,
                    onExportClick = {},
                    onImportConfirmed = { confirmed = true },
                    onBackClick = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Import backup").performClick()
        composeTestRule.onNodeWithText("Replace current data?").assertExists()
        composeTestRule.onNodeWithText("Import").performClick()

        assert(confirmed)
    }

    @Test
    fun errorState_showsErrorMessage() {
        composeTestRule.setContent {
            CakeDayyTheme {
                BackupScreen(
                    state = BackupUiState.Error(BackupErrorReason.MALFORMED_JSON),
                    onExportClick = {},
                    onImportConfirmed = {},
                    onBackClick = {},
                )
            }
        }

        composeTestRule.onNodeWithText("That file isn't a valid CakeDayy backup.").assertExists()
    }
}
