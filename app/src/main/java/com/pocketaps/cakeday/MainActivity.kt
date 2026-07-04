package com.pocketaps.cakeday

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pocketaps.cakeday.core.designsystem.CakeDayyTheme
import com.pocketaps.cakeday.core.model.ThemeMode
import com.pocketaps.cakeday.navigation.CakeDayyNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel = hiltViewModel<MainActivityViewModel>()
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val darkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            CakeDayyTheme(darkTheme = darkTheme) {
                CakeDayyNavHost()
            }
        }
    }
}
