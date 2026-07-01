package com.pocketaps.cakeday.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pocketaps.cakeday.core.designsystem.CakeDayyTheme
import com.pocketaps.cakeday.core.designsystem.components.CakeDayyCard
import com.pocketaps.cakeday.core.model.Person
import com.pocketaps.cakeday.core.model.UpcomingBirthday

@Composable
fun PersonRow(
    upcomingBirthday: UpcomingBirthday,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CakeDayyCard(modifier = modifier.fillMaxWidth(), onClick = onClick) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = upcomingBirthday.person.name,
                    style = MaterialTheme.typography.titleMedium,
                )
                val age = upcomingBirthday.nextAge
                if (age != null) {
                    Text(
                        text = "Turns $age",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            Text(
                text = daysUntilLabel(upcomingBirthday.daysUntilNext),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

private fun daysUntilLabel(daysUntilNext: Int): String = when (daysUntilNext) {
    0 -> "Today"
    1 -> "Tomorrow"
    else -> "In $daysUntilNext days"
}

@Preview(showBackground = true)
@Composable
private fun PersonRowWithAgePreview() {
    CakeDayyTheme {
        PersonRow(
            upcomingBirthday = UpcomingBirthday(
                person = Person(id = 1L, name = "Alice", birthMonth = 5, birthDay = 10, birthYear = 1994),
                daysUntilNext = 5,
                nextAge = 32,
            ),
            onClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PersonRowWithoutYearPreview() {
    CakeDayyTheme {
        PersonRow(
            upcomingBirthday = UpcomingBirthday(
                person = Person(id = 2L, name = "Bob", birthMonth = 1, birthDay = 1, birthYear = null),
                daysUntilNext = 0,
                nextAge = null,
            ),
            onClick = {},
        )
    }
}
