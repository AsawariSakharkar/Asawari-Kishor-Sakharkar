package com.example.ui.tools

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.OverthinkTopAppBar

data class ToolItemData(
    val id: String,
    val title: String,
    val benefit: String,
    val durationText: String,
    val bestWhen: String
)

@Composable
fun ToolsScreen(
    onOpenTool: (String) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        OverthinkTopAppBar(
            title = "Tool Library",
            onSettingsClick = onOpenSettings
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Text(
                    text = "Individual practices to pause and reset.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Section 1: Calm the body
            item {
                ToolSectionHeader(title = "Calm the body")
            }
            item {
                ToolCard(
                    data = ToolItemData(
                        id = "belly_breathing",
                        title = "Belly Breathing",
                        benefit = "Activates the body's natural rest response",
                        durationText = "3 min",
                        bestWhen = "Your chest feels tight or your pulse is racing"
                    ),
                    onStart = { onOpenTool("belly_breathing") }
                )
            }
            item {
                ToolCard(
                    data = ToolItemData(
                        id = "meditation",
                        title = "Mindful Meditation",
                        benefit = "Gently anchors awareness on breath, body, or sounds",
                        durationText = "3–10 min",
                        bestWhen = "You need quiet time to step back from mental chatter"
                    ),
                    onStart = { onOpenTool("meditation") }
                )
            }

            // Section 2: Return to the present
            item {
                ToolSectionHeader(title = "Return to the present")
            }
            item {
                ToolCard(
                    data = ToolItemData(
                        id = "grounding_333",
                        title = "3-3-3 Rule",
                        benefit = "Quick 3-step physical and sensory anchor",
                        durationText = "2 min",
                        bestWhen = "Sudden restlessness or acute spiral begins"
                    ),
                    onStart = { onOpenTool("grounding_333") }
                )
            }
            item {
                ToolCard(
                    data = ToolItemData(
                        id = "grounding_54321",
                        title = "5-4-3-2-1 Rule",
                        benefit = "Thorough sensory immersion through 5 senses",
                        durationText = "4 min",
                        bestWhen = "You feel deeply detached or stuck in your head"
                    ),
                    onStart = { onOpenTool("grounding_54321") }
                )
            }

            // Section 3: Create space from thoughts
            item {
                ToolSectionHeader(title = "Create space from thoughts")
            }
            item {
                ToolCard(
                    data = ToolItemData(
                        id = "let_it_pass",
                        title = "Let It Pass",
                        benefit = "Visual thought defusion without struggling to fix it",
                        durationText = "2 min",
                        bestWhen = "A repetitive, nagging thought keeps replaying"
                    ),
                    onStart = { onOpenTool("let_it_pass") }
                )
            }

            // Section 4: Understand the loop
            item {
                ToolSectionHeader(title = "Understand the loop")
            }
            item {
                ToolCard(
                    data = ToolItemData(
                        id = "identify_loop",
                        title = "Identify Your Overthinking",
                        benefit = "Map the pattern (replay, worry, assumption) and choose a tool",
                        durationText = "3 min",
                        bestWhen = "You know you are overthinking but don't know why"
                    ),
                    onStart = { onOpenTool("identify_loop") }
                )
            }

            // Section 5: Examine a thought
            item {
                ToolSectionHeader(title = "Examine a thought")
            }
            item {
                ToolCard(
                    data = ToolItemData(
                        id = "question_thought",
                        title = "Question the Thought",
                        benefit = "Distinguish facts from predictions and assumptions",
                        durationText = "3 min",
                        bestWhen = "You are treating worst-case worries as absolute facts"
                    ),
                    onStart = { onOpenTool("question_thought") }
                )
            }
            item {
                ToolCard(
                    data = ToolItemData(
                        id = "challenge_thought",
                        title = "Challenge a Thought",
                        benefit = "Explore evidence for and against, then draft a balanced view",
                        durationText = "4 min",
                        bestWhen = "A harsh self-judgment or fear feels overwhelming"
                    ),
                    onStart = { onOpenTool("challenge_thought") }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun ToolSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
fun ToolCard(
    data: ToolItemData,
    onStart: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onStart,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("tool_card_${data.id}")
    ) {
        Column(modifier = Modifier.padding(18.dp).fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = data.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = data.durationText,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = data.benefit,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Best when: ${data.bestWhen}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                FilledTonalButton(
                    onClick = onStart,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("start_tool_${data.id}")
                ) {
                    Text(
                        text = "Start",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
