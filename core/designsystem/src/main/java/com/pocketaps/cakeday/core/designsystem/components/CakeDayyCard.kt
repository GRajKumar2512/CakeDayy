package com.pocketaps.cakeday.core.designsystem.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pocketaps.cakeday.core.designsystem.CakeDayyTheme

@Composable
fun CakeDayyCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    if (onClick != null) {
        Card(onClick = onClick, modifier = modifier) { content() }
    } else {
        Card(modifier = modifier) { content() }
    }
}

@Preview(showBackground = true)
@Composable
private fun CakeDayyCardPreview() {
    CakeDayyTheme {
        CakeDayyCard {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Sample card content")
            }
        }
    }
}
