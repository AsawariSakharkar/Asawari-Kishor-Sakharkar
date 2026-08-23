package com.example.ui.session

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ExerciseType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullPauseSessionScreen(
    viewModel: FullPauseSessionViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showEndEarlyDialog by remember { mutableStateOf(false) }

    val currentStepType = when (state.currentStep) {
        SessionStep.Breathing -> ExerciseType.SLOW_BREATHING
        SessionStep.ThoughtCaptureStep -> ExerciseType.THOUGHT_CAPTURE
        SessionStep.LetItPassStep -> ExerciseType.LET_IT_PASS
        SessionStep.GroundingStep -> state.chosenGroundingType
        SessionStep.WalkStep -> ExerciseType.WALK
        else -> null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (state.currentStep != SessionStep.CheckIn &&
                        state.currentStep != SessionStep.EndCheckIn &&
                        state.currentStep != SessionStep.GentleSupport
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Exercise ${state.stepNumber} of 5",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Timer,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(end = 4.dp)
                                    )
                                    Text(
                                        text = "~${state.estimatedMinutesRemaining} min left",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        Text(
                            text = if (state.currentStep == SessionStep.CheckIn) "Pause Check-In" else "Session Complete",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (state.currentStep == SessionStep.CheckIn) {
                                onNavigateBack()
                            } else {
                                showEndEarlyDialog = true
                            }
                        },
                        modifier = Modifier.testTag("session_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "End session",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    if (currentStepType != null) {
                        TextButton(
                            onClick = { viewModel.skipExercise(currentStepType) },
                            modifier = Modifier.testTag("skip_exercise_button")
                        ) {
                            Text(
                                text = "Skip",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Linear Progress Bar during exercises
            if (state.stepNumber in 1..5 && state.currentStep != SessionStep.EndCheckIn && state.currentStep != SessionStep.GentleSupport) {
                LinearProgressIndicator(
                    progress = { state.stepNumber / 5f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            AnimatedContent(
                targetState = state.currentStep,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "session_step_transition",
                modifier = Modifier.fillMaxSize()
            ) { step ->
                when (step) {
                    SessionStep.CheckIn -> {
                        CheckInScreen(
                            selectedEmotion = state.selectedEmotion,
                            selectedIntensity = state.selectedIntensity,
                            selectedTrigger = state.selectedTrigger,
                            onSelectEmotion = viewModel::selectEmotion,
                            onSelectIntensity = viewModel::selectIntensity,
                            onSelectTrigger = viewModel::selectTrigger,
                            onBeginPause = viewModel::beginPause
                        )
                    }
                    SessionStep.Breathing -> {
                        Exercise1BreathingScreen(
                            secondsRemaining = state.breathingSecondsRemaining,
                            isPaused = state.isBreathingPaused,
                            showOneMinuteCheck = state.showOneMinuteCheck,
                            isAmbientSoundPlaying = state.isAmbientSoundPlaying,
                            onTogglePause = viewModel::toggleBreathingPause,
                            onToggleAmbientSound = viewModel::toggleAmbientSound,
                            onDismissOneMinuteCheck = viewModel::dismissOneMinutePrompt,
                            onMoveOn = { viewModel.continueExercise(ExerciseType.SLOW_BREATHING) }
                        )
                    }
                    SessionStep.ThoughtCaptureStep -> {
                        Exercise2ThoughtCaptureScreen(
                            secondsRemaining = state.thoughtCaptureSecondsRemaining,
                            enteredWords = state.enteredWords,
                            currentWordInput = state.currentWordInput,
                            showIncludeInsightsPrompt = state.showIncludeInsightsPrompt,
                            onUpdateWordInput = viewModel::updateWordInput,
                            onAddWord = viewModel::addWord,
                            onRemoveWord = viewModel::removeWord,
                            onFinishThoughtCapture = viewModel::finishThoughtCapture
                        )
                    }
                    SessionStep.LetItPassStep -> {
                        Exercise3LetItPassScreen(
                            thought = state.letItPassThought,
                            isCloudDrifting = state.isCloudDrifting,
                            showLetItPassMessage = state.showLetItPassMessage,
                            onUpdateThought = viewModel::updateLetItPassThought,
                            onTriggerLetItPass = viewModel::triggerLetItPass,
                            onFinishLetItPass = viewModel::finishLetItPass
                        )
                    }
                    SessionStep.GroundingStep -> {
                        Exercise4GroundingScreen(
                            chosenGroundingType = state.chosenGroundingType,
                            currentStepIndex = state.currentGroundingStepIndex,
                            groundingRecommendation = state.groundingRecommendation,
                            onSwitchType = viewModel::switchGroundingType,
                            onAdvanceStep = viewModel::advanceGroundingStep
                        )
                    }
                    SessionStep.WalkStep -> {
                        Exercise5WalkScreen(
                            secondsRemaining = state.walkSecondsRemaining,
                            isPaused = state.isWalkPaused,
                            stepCount = state.stepCount,
                            showAlternatives = state.showWalkAlternatives,
                            chosenAlternative = state.chosenAlternative,
                            onTogglePause = viewModel::toggleWalkPause,
                            onStepIncrement = viewModel::incrementStepCount,
                            onShowAlternatives = viewModel::showWalkAlternatives,
                            onChooseAlternative = viewModel::chooseWalkAlternative,
                            onFinishWalk = { viewModel.continueExercise(ExerciseType.WALK) }
                        )
                    }
                    SessionStep.EndCheckIn -> {
                        EndCheckInScreen(
                            endFeeling = state.endFeeling,
                            mostHelpfulPart = state.mostHelpfulPart,
                            reflectionText = state.reflectionText,
                            helpfulnessRating = state.helpfulnessRating,
                            onSelectEndFeeling = viewModel::setEndFeeling,
                            onSelectHelpfulPart = viewModel::setMostHelpfulPart,
                            onUpdateReflection = viewModel::setReflectionText,
                            onSelectRating = viewModel::setHelpfulnessRating,
                            onFinishSession = { viewModel.completeSession(onNavigateBack) }
                        )
                    }
                    SessionStep.GentleSupport -> {
                        GentleSupportScreen(
                            onTryGrounding = { viewModel.switchGroundingType(ExerciseType.GROUNDING_333) },
                            onCloseAndRest = { viewModel.completeSession(onNavigateBack) }
                        )
                    }
                    SessionStep.Completed -> {
                        // Handled by onNavigateBack
                    }
                }
            }
        }
    }

    if (showEndEarlyDialog) {
        AlertDialog(
            onDismissRequest = { showEndEarlyDialog = false },
            title = { Text("End pause session?") },
            text = {
                Text("You can pause whenever you want. Any time you took for yourself still counts.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showEndEarlyDialog = false
                        viewModel.endSessionEarly(onNavigateBack)
                    },
                    modifier = Modifier.testTag("end_early_confirm")
                ) {
                    Text("End session")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showEndEarlyDialog = false },
                    modifier = Modifier.testTag("end_early_dismiss")
                ) {
                    Text("Continue pause")
                }
            }
        )
    }
}
