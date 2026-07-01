package com.pocketaps.cakeday

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.pocketaps.cakeday.core.designsystem.CakeDayyTheme
import com.pocketaps.cakeday.navigation.CakeDayyNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CakeDayyTheme {
                CakeDayyNavHost()
            }
        }
    }
}
