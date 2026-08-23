package com.example.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BrightnessAuto
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.datastore.AppThemeMode
import com.example.data.datastore.UserPreferencesRepository
import com.example.data.repository.OverthinkRepository
import com.example.ui.components.CalmCard
import com.example.ui.components.OverthinkTopAppBar
import kotlinx.coroutines.launch

data class ThemeOption(val mode: AppThemeMode, val label: String, val icon: ImageVector, val tag: String)

@Composable
fun SettingsScreen(
    repository: OverthinkRepository,
    preferencesRepository: UserPreferencesRepository,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val themeMode by preferencesRepository.themeMode.collectAsStateWithLifecycle(initialValue = AppThemeMode.SYSTEM)
    val ambientSoundsDefault by preferencesRepository.ambientSoundsDefault.collectAsStateWithLifecycle(initialValue = false)
    val patternTrackingEnabled by preferencesRepository.isPatternTrackingEnabled.collectAsStateWithLifecycle(initialValue = true)

    val coroutineScope = rememberCoroutineScope()
    var showDeleteAllDialog by remember { mutableStateOf(false) }

    val themeOptions = listOf(
        ThemeOption(AppThemeMode.SYSTEM, "System", Icons.Outlined.BrightnessAuto, "theme_option_system"),
        ThemeOption(AppThemeMode.LIGHT, "Light", Icons.Outlined.LightMode, "theme_option_light"),
        ThemeOption(AppThemeMode.DARK, "Dark", Icons.Outlined.DarkMode, "theme_option_dark")
    )

    Scaffold(
        topBar = {
            OverthinkTopAppBar(
                title = "Settings",
                onBackClick = onNavigateBack
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Theme Mode
            item {
                CalmCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Appearance",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Choose how Overthink looks on your device",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        themeOptions.forEach { option ->
                            FilterChip(
                                selected = themeMode == option.mode,
                                onClick = {
                                    coroutineScope.launch {
                                        preferencesRepository.setThemeMode(option.mode)
                                    }
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = option.icon,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                label = { Text(option.label) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag(option.tag)
                            )
                        }
                    }
                }
            }

            // Audio Preferences
            item {
                CalmCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Ambient sound by default",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Play calm tone during breathing exercises",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = ambientSoundsDefault,
                            onCheckedChange = { enabled ->
                                coroutineScope.launch {
                                    preferencesRepository.setAmbientSoundsDefault(enabled)
                                }
                            }
                        )
                    }
                }
            }

            // Privacy & Tracking
            item {
                CalmCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Local pattern summaries",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Calculate local emotion & tool trends on this device",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = patternTrackingEnabled,
                            onCheckedChange = { enabled ->
                                coroutineScope.launch {
                                    preferencesRepository.setPatternTrackingEnabled(enabled)
                                }
                            }
                        )
                    }
                }
            }

            // Storage & Security Info
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Local Storage & Privacy",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "All journal entries, pauses, and insights are stored purely in on-device Room/SQLite storage. There is no cloud sync, user account, telemetry, or third-party advertising.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Delete All Data Option
            item {
                CalmCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Data Management",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Erase all pauses, journal entries, and app preferences permanently from this device.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = { showDeleteAllDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("delete_all_data_button")
                    ) {
                        Icon(imageVector = Icons.Outlined.DeleteOutline, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Delete All App Data")
                    }
                }
            }

            // Philosophy & Disclaimers
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "About Overthink",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Overthink is an offline, self-guided quality-of-life mindfulness app. It is designed to offer gentle pauses when thoughts start spinning. It does not provide psychiatric diagnoses, medical treatment, or crisis intervention.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            title = { Text("Permanently delete all data?") },
            text = {
                Text("This will immediately delete all journal entries, pause session records, and settings. This cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteAllDialog = false
                        coroutineScope.launch {
                            repository.clearAllData()
                            preferencesRepository.clearAllPreferences()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
