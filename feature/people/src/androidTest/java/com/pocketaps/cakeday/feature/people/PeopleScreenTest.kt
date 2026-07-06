package com.pocketaps.cakeday.feature.people

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.pocketaps.cakeday.core.designsystem.CakeDayyTheme
import com.pocketaps.cakeday.core.model.Person
import com.pocketaps.cakeday.core.model.UpcomingBirthday
import org.junit.Rule
import org.junit.Test

class PeopleScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun actions(onAddClick: () -> Unit = {}) = PeopleActions(
        onAddClick = onAddClick,
        onPersonClick = {},
        onDeleteClick = {},
        onSettingsClick = {},
        onManageGroupsClick = {},
        onImportContactsClick = {},
        onQueryChange = {},
        onGroupFilterSelected = {},
    )

    @Test
    fun emptyState_showsEmptyMessage() {
        composeTestRule.setContent {
            CakeDayyTheme {
                PeopleScreen(
                    state = PeopleUiState.Content(
                        people = emptyList(),
                        groups = emptyList(),
                        query = "",
                        selectedGroupId = null,
                    ),
                    actions = actions(),
                )
            }
        }

        composeTestRule.onNodeWithText("No birthdays yet").assertExists()
    }

    @Test
    fun contentState_showsPersonName() {
        composeTestRule.setContent {
            CakeDayyTheme {
                PeopleScreen(
                    state = PeopleUiState.Content(
                        people = listOf(
                            UpcomingBirthday(
                                person = Person(id = 1L, name = "Alice", birthMonth = 5, birthDay = 10),
                                daysUntilNext = 5,
                                nextAge = null,
                            ),
                        ),
                        groups = emptyList(),
                        query = "",
                        selectedGroupId = null,
                    ),
                    actions = actions(),
                )
            }
        }

        composeTestRule.onNodeWithText("Alice").assertExists()
    }

    @Test
    fun addButton_invokesCallback() {
        var clicked = false
        composeTestRule.setContent {
            CakeDayyTheme {
                PeopleScreen(
                    state = PeopleUiState.Content(
                        people = emptyList(),
                        groups = emptyList(),
                        query = "",
                        selectedGroupId = null,
                    ),
                    actions = actions(onAddClick = { clicked = true }),
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Add person").performClick()

        assert(clicked)
    }
}
