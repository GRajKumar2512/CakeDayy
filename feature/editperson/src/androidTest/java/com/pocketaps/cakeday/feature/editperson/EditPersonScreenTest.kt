package com.pocketaps.cakeday.feature.editperson

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.pocketaps.cakeday.core.designsystem.CakeDayyTheme
import org.junit.Rule
import org.junit.Test

class EditPersonScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun actions(
        onNameChange: (String) -> Unit = {},
        onSaveClick: () -> Unit = {},
        onBackClick: () -> Unit = {},
    ) = EditPersonActions(
        onNameChange = onNameChange,
        onMonthChange = {},
        onDayChange = {},
        onYearChange = {},
        onNoteChange = {},
        onGroupSelected = {},
        onSaveClick = onSaveClick,
        onBackClick = onBackClick,
    )

    @Test
    fun addMode_showsAddTitle() {
        composeTestRule.setContent {
            CakeDayyTheme {
                EditPersonScreen(
                    state = EditPersonUiState(isLoading = false, personId = null),
                    actions = actions(),
                )
            }
        }

        composeTestRule.onNodeWithText("Add person").assertExists()
    }

    @Test
    fun editMode_showsExistingName() {
        composeTestRule.setContent {
            CakeDayyTheme {
                EditPersonScreen(
                    state = EditPersonUiState(isLoading = false, personId = 1L, name = "Alice"),
                    actions = actions(),
                )
            }
        }

        composeTestRule.onNodeWithText("Edit person").assertExists()
        composeTestRule.onNodeWithText("Alice").assertExists()
    }

    @Test
    fun typingName_invokesOnNameChange() {
        var latest: String? = null
        composeTestRule.setContent {
            CakeDayyTheme {
                EditPersonScreen(
                    state = EditPersonUiState(isLoading = false, personId = null),
                    actions = actions(onNameChange = { latest = it }),
                )
            }
        }

        composeTestRule.onNodeWithText("Name").performTextInput("Bob")

        assert(latest == "Bob")
    }

    @Test
    fun backButton_invokesCallback() {
        var clicked = false
        composeTestRule.setContent {
            CakeDayyTheme {
                EditPersonScreen(
                    state = EditPersonUiState(isLoading = false, personId = null),
                    actions = actions(onBackClick = { clicked = true }),
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Back").performClick()

        assert(clicked)
    }
}
