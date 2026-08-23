package com.example.ui.tools

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Hearing
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ui.components.BreathingVisualizer
import com.example.ui.components.CalmCard
import com.example.ui.components.OverthinkTopAppBar
import com.example.ui.components.SelectableChip
import kotlinx.coroutines.delay

// ----------------------------------------------------
// 1. Mindful Meditation Tool Screen
// ----------------------------------------------------
enum class MeditationFocus(val title: String, val prompt: String) {
    BREATH("Breath Focus", "Rest attention on the sensation of air moving in and out."),
    BODY("Body Scan", "Notice areas of tension and allow them to soften."),
    SOUNDS("Sound Awareness", "Listen to ambient sounds coming and going without judgment.")
}

@Composable
fun MeditationToolScreen(
    onNavigateBack: () -> Unit,
    onRecordCompletion: (durationSeconds: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedMinutes by remember { mutableIntStateOf(5) }
    var selectedFocus by remember { mutableStateOf(MeditationFocus.BREATH) }
    var secondsRemaining by remember { mutableIntStateOf(selectedMinutes * 60) }
    var isRunning by remember { mutableStateOf(false) }
    var isCompleted by remember { mutableStateOf(false) }

    LaunchedEffect(isRunning, secondsRemaining) {
        if (isRunning && secondsRemaining > 0) {
            delay(1000L)
            secondsRemaining -= 1
            if (secondsRemaining <= 0) {
                isRunning = false
                isCompleted = true
                onRecordCompletion(selectedMinutes * 60)
            }
        }
    }

    Scaffold(
        topBar = {
            OverthinkTopAppBar(
                title = "Mindful Meditation",
                onBackClick = onNavigateBack
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 12.dp)
            ) {
                if (!isRunning && !isCompleted) {
                    Text(
                        text = "Choose duration & anchor",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Duration Chips
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf(3, 5, 10).forEach { mins ->
                            SelectableChip(
                                label = "$mins minutes",
                                isSelected = selectedMinutes == mins,
                                onClick = {
                                    selectedMinutes = mins
                                    secondsRemaining = mins * 60
                                },
                                modifier = Modifier.weight(1f),
                                testTag = "meditation_${mins}_mins"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Focus Anchors
                    Text(
                        text = "Focus Anchor",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        MeditationFocus.entries.forEach { focus ->
                            SelectableChip(
                                label = "${focus.title} — ${focus.prompt}",
                                isSelected = selectedFocus == focus,
                                onClick = { selectedFocus = focus },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                } else {
                    // Active meditation view
                    Text(
                        text = selectedFocus.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = selectedFocus.prompt,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    val min = secondsRemaining / 60
                    val sec = secondsRemaining % 60
                    Text(
                        text = String.format("%d:%02d", min, sec),
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Light,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    BreathingVisualizer(
                        size = 180.dp,
                        showText = false,
                        isPaused = !isRunning
                    )
                }
            }

            // Bottom Buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                if (!isRunning && !isCompleted) {
                    Button(
                        onClick = { isRunning = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("start_meditation_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Begin Meditation")
                    }
                } else if (isRunning) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { isRunning = false },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(imageVector = Icons.Outlined.Pause, contentDescription = "Pause")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pause")
                        }
                        Button(
                            onClick = {
                                isRunning = false
                                isCompleted = true
                                onRecordCompletion(selectedMinutes * 60 - secondsRemaining)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Finish")
                        }
                    }
                } else {
                    Button(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Done")
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// 2. Identify Your Overthinking Screen
// ----------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IdentifyOverthinkingScreen(
    onNavigateBack: () -> Unit,
    onOpenTool: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val themes = listOf(
        "Replaying past interactions",
        "Catastrophizing the future",
        "Trying to read minds / assume motives",
        "Perfectionism / all-or-nothing",
        "Decision paralysis / what-if loops"
    )

    val physicalSigns = listOf(
        "Clenched jaw / tight shoulders",
        "Shallow breathing",
        "Restless pacing or fidgeting",
        "Stomach churning / racing heart"
    )

    var selectedTheme by remember { mutableStateOf<String?>(null) }
    var selectedSign by remember { mutableStateOf<String?>(null) }
    var showRecommendation by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            OverthinkTopAppBar(
                title = "Identify Overthinking",
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
            item {
                Text(
                    text = "Map the pattern of your thoughts to find the most fitting pause.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Step 1: Theme
            item {
                CalmCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "1. What is the main flavor of the loop?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        themes.forEach { theme ->
                            SelectableChip(
                                label = theme,
                                isSelected = selectedTheme == theme,
                                onClick = {
                                    selectedTheme = theme
                                    showRecommendation = true
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // Step 2: Physical Signs
            item {
                CalmCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "2. How is it showing up in your body? (Optional)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        physicalSigns.forEach { sign ->
                            SelectableChip(
                                label = sign,
                                isSelected = selectedSign == sign,
                                onClick = { selectedSign = if (selectedSign == sign) null else sign },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // Recommendation Result
            if (showRecommendation && selectedTheme != null) {
                item {
                    val (suggestedToolId, suggestedToolName, suggestionReason) = when (selectedTheme) {
                        "Replaying past interactions" -> Triple("let_it_pass", "Let It Pass", "Release the urge to re-script what already happened.")
                        "Catastrophizing the future" -> Triple("question_thought", "Question the Thought", "Test whether the catastrophe is a fact or an unproven prediction.")
                        "Trying to read minds / assume motives" -> Triple("challenge_thought", "Challenge a Thought", "Find balanced alternative perspectives for what others might be thinking.")
                        "Perfectionism / all-or-nothing" -> Triple("challenge_thought", "Challenge a Thought", "Create space between 0% and 100% outcomes.")
                        else -> Triple("grounding_333", "3-3-3 Grounding", "Drop into your immediate sensory environment to stop the spiral.")
                    }

                    CalmCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Suggested Tool",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = suggestedToolName,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = suggestionReason,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { onOpenTool(suggestedToolId) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("start_suggested_tool_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Open $suggestedToolName")
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}

// ----------------------------------------------------
// 3. Question the Thought Screen
// ----------------------------------------------------
@Composable
fun QuestionThoughtScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var thoughtText by remember { mutableStateOf("") }
    var classification by remember { mutableStateOf<String?>(null) }
    var isHelpfulNow by remember { mutableStateOf<String?>(null) }
    var nextAction by remember { mutableStateOf("") }
    var isFinished by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            OverthinkTopAppBar(
                title = "Question the Thought",
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "A gentle inquiry to separate facts from runaway interpretations.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Step 1: The Thought
            item {
                CalmCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "1. What is the specific thought?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = thoughtText,
                        onValueChange = { thoughtText = it },
                        placeholder = { Text("e.g., Everyone noticed I made a mistake.") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )
                }
            }

            // Step 2: Classification
            item {
                CalmCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "2. What is this primarily?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    val categories = listOf("Fact (Observable truth)", "Prediction (Guessing the future)", "Memory (Past replay)", "Assumption (Mind-reading)")
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        categories.forEach { cat ->
                            SelectableChip(
                                label = cat,
                                isSelected = classification == cat,
                                onClick = { classification = cat },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // Step 3: Is it helpful right now?
            item {
                CalmCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "3. Is holding onto this helpful right now?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Yes, there is an immediate action", "No, it is just spinning energy").forEach { opt ->
                            SelectableChip(
                                label = opt,
                                isSelected = isHelpfulNow == opt,
                                onClick = { isHelpfulNow = opt },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Step 4: Next Small Action
            item {
                CalmCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "4. What is one small step or kind anchor?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = nextAction,
                        onValueChange = { nextAction = it },
                        placeholder = { Text("e.g., Take a break, send a clarifying text, or let it rest.") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )
                }
            }

            item {
                Button(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("finish_question_thought_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Complete Inquiry")
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

// ----------------------------------------------------
// 4. Challenge a Thought Screen
// ----------------------------------------------------
@Composable
fun ChallengeThoughtScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var thought by remember { mutableStateOf("") }
    var evidenceFor by remember { mutableStateOf("") }
    var evidenceAgainst by remember { mutableStateOf("") }
    var balancedAlternative by remember { mutableStateOf("") }
    var nextAction by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            OverthinkTopAppBar(
                title = "Challenge a Thought",
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Look at the evidence and develop a balanced, compassionate view.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                CalmCard(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "1. The automatic thought", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = thought,
                        onValueChange = { thought = it },
                        placeholder = { Text("e.g., I ruined the whole presentation.") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )
                }
            }

            item {
                CalmCard(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "2. Evidence FOR this thought", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = evidenceFor,
                        onValueChange = { evidenceFor = it },
                        placeholder = { Text("Factual observations only.") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )
                }
            }

            item {
                CalmCard(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "3. Evidence AGAINST this thought", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = evidenceAgainst,
                        onValueChange = { evidenceAgainst = it },
                        placeholder = { Text("What went okay? What contradicts the worst-case?") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )
                }
            }

            item {
                CalmCard(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "4. Balanced alternative thought", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = balancedAlternative,
                        onValueChange = { balancedAlternative = it },
                        placeholder = { Text("e.g., I stumbled on one slide, but the overall message was delivered.") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )
                }
            }

            item {
                CalmCard(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "5. One next action", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = nextAction,
                        onValueChange = { nextAction = it },
                        placeholder = { Text("e.g., Drink water and close the laptop.") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )
                }
            }

            item {
                Button(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("finish_challenge_thought_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Save & Complete")
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
