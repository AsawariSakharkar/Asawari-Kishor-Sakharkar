package com.example.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.datastore.UserPreferencesRepository
import com.example.data.model.ExerciseType
import com.example.data.repository.OverthinkRepository
import com.example.ui.home.HomeScreen
import com.example.ui.home.HomeViewModel
import com.example.ui.insights.InsightsScreen
import com.example.ui.insights.InsightsViewModel
import com.example.ui.journal.JournalScreen
import com.example.ui.journal.JournalViewModel
import com.example.ui.session.Exercise3LetItPassScreen
import com.example.ui.session.Exercise4GroundingScreen
import com.example.ui.session.FullPauseSessionScreen
import com.example.ui.session.FullPauseSessionViewModel
import com.example.ui.settings.SettingsScreen
import com.example.ui.tools.BellyBreathingToolScreen
import com.example.ui.tools.ChallengeThoughtScreen
import com.example.ui.tools.IdentifyOverthinkingScreen
import com.example.ui.tools.MeditationToolScreen
import com.example.ui.tools.QuestionThoughtScreen
import com.example.ui.tools.ToolsScreen
import kotlinx.coroutines.launch

@Composable
fun AppNavigation(
    repository: OverthinkRepository,
    preferencesRepository: UserPreferencesRepository,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val coroutineScope = rememberCoroutineScope()

    val isTopLevelDestination = currentRoute in listOf(
        Screen.Home.route,
        Screen.Tools.route,
        Screen.Journal.route,
        Screen.Insights.route
    )

    Scaffold(
        bottomBar = {
            if (isTopLevelDestination) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    tonalElevation = 0.dp
                ) {
                    bottomNavItems.forEach { item ->
                        val isSelected = currentRoute == item.screen.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                if (currentRoute != item.screen.route) {
                                    navController.navigate(item.screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.title
                                )
                            },
                            label = {
                                Text(
                                    text = item.title,
                                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedTextColor = MaterialTheme.colorScheme.onSurface,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.testTag("nav_tab_${item.screen.route}")
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (isTopLevelDestination) {
                ExtendedFloatingActionButton(
                    onClick = { navController.navigate(Screen.FullPauseSession.route) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null
                        )
                    },
                    text = {
                        Text(
                            text = "Start a pause",
                            style = MaterialTheme.typography.labelLarge
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.testTag("global_start_pause_fab")
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // 1. Home
            composable(Screen.Home.route) {
                val homeViewModel: HomeViewModel = viewModel(
                    factory = HomeViewModel.Factory(repository)
                )
                HomeScreen(
                    viewModel = homeViewModel,
                    onStartPauseSession = {
                        navController.navigate(Screen.FullPauseSession.route)
                    },
                    onOpenTool = { toolId ->
                        when (toolId) {
                            "slow_breathing", "belly_breathing" -> navController.navigate(Screen.BellyBreathingTool.route)
                            "meditation" -> navController.navigate(Screen.MeditationTool.route)
                            "grounding_333" -> navController.navigate(Screen.Grounding333Tool.route)
                            "grounding_54321" -> navController.navigate(Screen.Grounding54321Tool.route)
                            "let_it_pass" -> navController.navigate(Screen.LetItPassTool.route)
                            "identify_loop" -> navController.navigate(Screen.IdentifyOverthinkingTool.route)
                            "question_thought" -> navController.navigate(Screen.QuestionThoughtTool.route)
                            "challenge_thought" -> navController.navigate(Screen.ChallengeThoughtTool.route)
                            else -> navController.navigate(Screen.FullPauseSession.route)
                        }
                    },
                    onOpenSettings = {
                        navController.navigate(Screen.Settings.route)
                    }
                )
            }

            // 2. Tools
            composable(Screen.Tools.route) {
                ToolsScreen(
                    onOpenTool = { toolId ->
                        when (toolId) {
                            "belly_breathing" -> navController.navigate(Screen.BellyBreathingTool.route)
                            "meditation" -> navController.navigate(Screen.MeditationTool.route)
                            "grounding_333" -> navController.navigate(Screen.Grounding333Tool.route)
                            "grounding_54321" -> navController.navigate(Screen.Grounding54321Tool.route)
                            "let_it_pass" -> navController.navigate(Screen.LetItPassTool.route)
                            "identify_loop" -> navController.navigate(Screen.IdentifyOverthinkingTool.route)
                            "question_thought" -> navController.navigate(Screen.QuestionThoughtTool.route)
                            "challenge_thought" -> navController.navigate(Screen.ChallengeThoughtTool.route)
                        }
                    },
                    onOpenSettings = {
                        navController.navigate(Screen.Settings.route)
                    }
                )
            }

            // 3. Journal
            composable(Screen.Journal.route) {
                val journalViewModel: JournalViewModel = viewModel(
                    factory = JournalViewModel.Factory(repository)
                )
                JournalScreen(
                    viewModel = journalViewModel,
                    onOpenSettings = {
                        navController.navigate(Screen.Settings.route)
                    }
                )
            }

            // 4. Insights
            composable(Screen.Insights.route) {
                val insightsViewModel: InsightsViewModel = viewModel(
                    factory = InsightsViewModel.Factory(repository, preferencesRepository)
                )
                InsightsScreen(
                    viewModel = insightsViewModel,
                    onOpenSettings = {
                        navController.navigate(Screen.Settings.route)
                    }
                )
            }

            // Full Pause Session Flow
            composable(Screen.FullPauseSession.route) {
                val sessionViewModel: FullPauseSessionViewModel = viewModel(
                    factory = FullPauseSessionViewModel.Factory(repository)
                )
                FullPauseSessionScreen(
                    viewModel = sessionViewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // Standalone Tools
            composable(Screen.BellyBreathingTool.route) {
                BellyBreathingToolScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onRecordCompletion = { duration ->
                        coroutineScope.launch {
                            repository.recordExerciseCompletion(
                                sessionId = 0,
                                exerciseType = "belly_breathing",
                                durationSeconds = duration,
                                isSkipped = false,
                                helpfulnessRating = 4,
                                emotion = "",
                                trigger = ""
                            )
                        }
                    }
                )
            }

            composable(Screen.MeditationTool.route) {
                MeditationToolScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onRecordCompletion = { duration ->
                        coroutineScope.launch {
                            repository.recordExerciseCompletion(
                                sessionId = 0,
                                exerciseType = "meditation",
                                durationSeconds = duration,
                                isSkipped = false,
                                helpfulnessRating = 4,
                                emotion = "",
                                trigger = ""
                            )
                        }
                    }
                )
            }

            composable(Screen.IdentifyOverthinkingTool.route) {
                IdentifyOverthinkingScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onOpenTool = { toolId ->
                        when (toolId) {
                            "let_it_pass" -> navController.navigate(Screen.LetItPassTool.route)
                            "question_thought" -> navController.navigate(Screen.QuestionThoughtTool.route)
                            "challenge_thought" -> navController.navigate(Screen.ChallengeThoughtTool.route)
                            "grounding_333" -> navController.navigate(Screen.Grounding333Tool.route)
                        }
                    }
                )
            }

            composable(Screen.QuestionThoughtTool.route) {
                QuestionThoughtScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.ChallengeThoughtTool.route) {
                ChallengeThoughtScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Grounding333Tool.route) {
                Exercise4GroundingScreen(
                    chosenGroundingType = ExerciseType.GROUNDING_333,
                    currentStepIndex = 0,
                    groundingRecommendation = null,
                    onSwitchType = {},
                    onAdvanceStep = { navController.popBackStack() }
                )
            }

            composable(Screen.Grounding54321Tool.route) {
                Exercise4GroundingScreen(
                    chosenGroundingType = ExerciseType.GROUNDING_54321,
                    currentStepIndex = 0,
                    groundingRecommendation = null,
                    onSwitchType = {},
                    onAdvanceStep = { navController.popBackStack() }
                )
            }

            composable(Screen.LetItPassTool.route) {
                Exercise3LetItPassScreen(
                    thought = "",
                    isCloudDrifting = false,
                    showLetItPassMessage = false,
                    onUpdateThought = {},
                    onTriggerLetItPass = {},
                    onFinishLetItPass = { navController.popBackStack() }
                )
            }

            // Settings
            composable(Screen.Settings.route) {
                SettingsScreen(
                    repository = repository,
                    preferencesRepository = preferencesRepository,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
