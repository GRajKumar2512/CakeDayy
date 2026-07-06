package com.pocketaps.cakeday.feature.settings

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.pocketaps.cakeday.core.designsystem.CakeDayyTheme
import com.pocketaps.cakeday.core.model.ReminderLead
import org.junit.Rule
import org.junit.Test

class SettingsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun actions(onOpenBackup: () -> Unit = {}, onBackClick: () -> Unit = {}) = SettingsActions(
        onLeadSelected = {},
        onThemeModeSelected = {},
        onOpenBackup = onOpenBackup,
        onBackClick = onBackClick,
    )

    @Test
    fun reminderOptions_areDisplayed() {
        composeTestRule.setContent {
            CakeDayyTheme {
                SettingsScreen(
                    state = SettingsUiState(isLoading = false, selectedLead = ReminderLead.ONE_DAY_BEFORE),
                    showPermissionRationale = false,
                    actions = actions(),
                )
            }
        }

        composeTestRule.onNodeWithText("1 day before").assertExists()
    }

    @Test
    fun permissionRationale_shownOnlyWhenRequested() {
        composeTestRule.setContent {
            CakeDayyTheme {
                SettingsScreen(
                    state = SettingsUiState(isLoading = false, selectedLead = ReminderLead.ON_THE_DAY),
                    showPermissionRationale = true,
                    actions = actions(),
                )
            }
        }

        composeTestRule.onNodeWithText(
            "Notifications are turned off for CakeDayy. Enable them in system settings to get birthday reminders.",
        ).assertExists()
    }

    @Test
    fun backupCard_invokesOnOpenBackup() {
        var clicked = false
        composeTestRule.setContent {
            CakeDayyTheme {
                SettingsScreen(
                    state = SettingsUiState(isLoading = false, selectedLead = ReminderLead.ON_THE_DAY),
                    showPermissionRationale = false,
                    actions = actions(onOpenBackup = { clicked = true }),
                )
            }
        }

        composeTestRule.onNodeWithText("Backup & Restore").performClick()

        assert(clicked)
    }

    @Test
    fun backButton_invokesCallback() {
        var clicked = false
        composeTestRule.setContent {
            CakeDayyTheme {
                SettingsScreen(
                    state = SettingsUiState(isLoading = false, selectedLead = ReminderLead.ON_THE_DAY),
                    showPermissionRationale = false,
                    actions = actions(onBackClick = { clicked = true }),
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Back").performClick()

        assert(clicked)
    }
}
