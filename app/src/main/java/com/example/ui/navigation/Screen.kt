package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    // Bottom Bar tabs
    data object Home : Screen("home")
    data object Tools : Screen("tools")
    data object Journal : Screen("journal")
    data object Insights : Screen("insights")

    // Session
    data object FullPauseSession : Screen("pause_session")

    // Standalone Tools
    data object MeditationTool : Screen("tool_meditation")
    data object IdentifyOverthinkingTool : Screen("tool_identify_overthinking")
    data object QuestionThoughtTool : Screen("tool_question_thought")
    data object ChallengeThoughtTool : Screen("tool_challenge_thought")
    data object BellyBreathingTool : Screen("tool_belly_breathing")
    data object Grounding333Tool : Screen("tool_grounding_333")
    data object Grounding54321Tool : Screen("tool_grounding_54321")
    data object LetItPassTool : Screen("tool_let_it_pass")

    // Settings
    data object Settings : Screen("settings")
}

data class BottomNavItem(
    val screen: Screen,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(
        screen = Screen.Home,
        title = "Home",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    ),
    BottomNavItem(
        screen = Screen.Tools,
        title = "Tools",
        selectedIcon = Icons.Filled.Widgets,
        unselectedIcon = Icons.Outlined.Widgets
    ),
    BottomNavItem(
        screen = Screen.Journal,
        title = "Journal",
        selectedIcon = Icons.Filled.Book,
        unselectedIcon = Icons.Outlined.Book
    ),
    BottomNavItem(
        screen = Screen.Insights,
        title = "Insights",
        selectedIcon = Icons.Filled.AutoAwesome,
        unselectedIcon = Icons.Outlined.AutoAwesome
    )
)
