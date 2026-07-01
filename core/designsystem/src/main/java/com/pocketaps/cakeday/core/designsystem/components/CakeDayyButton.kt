package com.pocketaps.cakeday.core.designsystem.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.pocketaps.cakeday.core.designsystem.CakeDayyTheme

@Composable
fun CakeDayyButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        contentPadding = contentPadding,
    ) {
        Text(text)
    }
}

@Preview(showBackground = true)
@Composable
private fun CakeDayyButtonPreview() {
    CakeDayyTheme {
        CakeDayyButton(text = "Save", onClick = {})
    }
}
