package com.example.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

enum class AppThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

class UserPreferencesRepository(private val context: Context) {
    companion object {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val AMBIENT_SOUNDS_DEFAULT = booleanPreferencesKey("ambient_sounds_default")
        val PATTERN_TRACKING_ENABLED = booleanPreferencesKey("pattern_tracking_enabled")
        val STEP_COUNTER_ENABLED = booleanPreferencesKey("step_counter_enabled")
    }

    val themeMode: Flow<AppThemeMode> = context.dataStore.data.map { preferences ->
        val modeStr = preferences[THEME_MODE] ?: AppThemeMode.SYSTEM.name
        try {
            AppThemeMode.valueOf(modeStr)
        } catch (e: Exception) {
            AppThemeMode.SYSTEM
        }
    }

    val ambientSoundsDefault: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[AMBIENT_SOUNDS_DEFAULT] ?: false
    }

    val isPatternTrackingEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PATTERN_TRACKING_ENABLED] ?: true
    }

    val stepCounterEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[STEP_COUNTER_ENABLED] ?: false
    }

    suspend fun setThemeMode(mode: AppThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE] = mode.name
        }
    }

    suspend fun setAmbientSoundsDefault(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[AMBIENT_SOUNDS_DEFAULT] = enabled
        }
    }

    suspend fun setPatternTrackingEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PATTERN_TRACKING_ENABLED] = enabled
        }
    }

    suspend fun setStepCounterEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[STEP_COUNTER_ENABLED] = enabled
        }
    }

    suspend fun clearAllPreferences() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
