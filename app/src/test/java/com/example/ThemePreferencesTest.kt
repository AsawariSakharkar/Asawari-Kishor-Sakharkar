package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.datastore.AppThemeMode
import com.example.data.datastore.UserPreferencesRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ThemePreferencesTest {

    private lateinit var context: Context
    private lateinit var preferencesRepository: UserPreferencesRepository

    @Before
    fun setup() = runBlocking {
        context = ApplicationProvider.getApplicationContext()
        preferencesRepository = UserPreferencesRepository(context)
        preferencesRepository.clearAllPreferences()
    }

    @Test
    fun testDefaultThemeIsSystem() = runBlocking {
        val mode = preferencesRepository.themeMode.first()
        assertEquals(AppThemeMode.SYSTEM, mode)
    }

    @Test
    fun testSetDarkThemePersists() = runBlocking {
        preferencesRepository.setThemeMode(AppThemeMode.DARK)
        val mode = preferencesRepository.themeMode.first()
        assertEquals(AppThemeMode.DARK, mode)
    }

    @Test
    fun testSetLightThemePersists() = runBlocking {
        preferencesRepository.setThemeMode(AppThemeMode.LIGHT)
        val mode = preferencesRepository.themeMode.first()
        assertEquals(AppThemeMode.LIGHT, mode)
    }

    @Test
    fun testSetSystemThemePersists() = runBlocking {
        preferencesRepository.setThemeMode(AppThemeMode.DARK)
        preferencesRepository.setThemeMode(AppThemeMode.SYSTEM)
        val mode = preferencesRepository.themeMode.first()
        assertEquals(AppThemeMode.SYSTEM, mode)
    }
}
