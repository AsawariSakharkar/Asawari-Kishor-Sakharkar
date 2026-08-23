package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.datastore.AppThemeMode
import com.example.data.datastore.UserPreferencesRepository
import com.example.data.local.OverthinkDatabase
import com.example.data.repository.OverthinkRepository
import com.example.ui.navigation.AppNavigation
import com.example.ui.theme.OverthinkTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = OverthinkDatabase.getDatabase(applicationContext)
        val preferencesRepository = UserPreferencesRepository(applicationContext)
        val repository = OverthinkRepository(database = database)

        setContent {
            val themeMode by preferencesRepository.themeMode.collectAsStateWithLifecycle(
                initialValue = AppThemeMode.SYSTEM
            )
            val isDarkTheme = when (themeMode) {
                AppThemeMode.SYSTEM -> isSystemInDarkTheme()
                AppThemeMode.LIGHT -> false
                AppThemeMode.DARK -> true
            }

            OverthinkTheme(darkTheme = isDarkTheme) {
                AppNavigation(
                    repository = repository,
                    preferencesRepository = preferencesRepository
                )
            }
        }
    }
}
