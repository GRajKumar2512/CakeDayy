package com.pocketaps.cakeday.feature.groups

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.pocketaps.cakeday.core.designsystem.CakeDayyTheme
import com.pocketaps.cakeday.core.model.Group
import org.junit.Rule
import org.junit.Test

class GroupsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun actions(onBackClick: () -> Unit = {}) =
        GroupsActions(onSaveGroup = {}, onDeleteGroup = {}, onBackClick = onBackClick)

    @Test
    fun emptyState_showsEmptyMessage() {
        composeTestRule.setContent {
            CakeDayyTheme {
                GroupsScreen(
                    state = GroupsUiState.Content(groups = emptyList()),
                    actions = actions(),
                )
            }
        }

        composeTestRule.onNodeWithText("No groups yet").assertExists()
    }

    @Test
    fun contentState_showsGroupName() {
        composeTestRule.setContent {
            CakeDayyTheme {
                GroupsScreen(
                    state = GroupsUiState.Content(
                        groups = listOf(Group(id = 1L, name = "Family", colorHex = "#F44336")),
                    ),
                    actions = actions(),
                )
            }
        }

        composeTestRule.onNodeWithText("Family").assertExists()
    }

    @Test
    fun backButton_invokesCallback() {
        var clicked = false
        composeTestRule.setContent {
            CakeDayyTheme {
                GroupsScreen(
                    state = GroupsUiState.Content(groups = emptyList()),
                    actions = actions(onBackClick = { clicked = true }),
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Back").performClick()

        assert(clicked)
    }
}
