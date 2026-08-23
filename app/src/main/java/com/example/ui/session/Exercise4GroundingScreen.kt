package com.example.ui.session

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DirectionsWalk
import androidx.compose.material.icons.outlined.Hearing
import androidx.compose.material.icons.outlined.PanTool
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.model.ExerciseType
import com.example.data.repository.GroundingRecommendation
import com.example.ui.components.CalmCard

data class GroundingStepData(
    val stepTitle: String,
    val instruction: String,
    val count: Int,
    val icon: ImageVector,
    val helperTip: String
)

@Composable
fun Exercise4GroundingScreen(
    chosenGroundingType: ExerciseType,
    currentStepIndex: Int,
    groundingRecommendation: GroundingRecommendation?,
    onSwitchType: (ExerciseType) -> Unit,
    onAdvanceStep: (totalSteps: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val steps = if (chosenGroundingType == ExerciseType.GROUNDING_333) {
        listOf(
            GroundingStepData(
                stepTitle = "Look around you",
                instruction = "Notice 3 things you can see",
                count = 3,
                icon = Icons.Outlined.Visibility,
                helperTip = "Scan your space slowly. Look for textures, colors, or shadows."
            ),
            GroundingStepData(
                stepTitle = "Listen carefully",
                instruction = "Notice 3 sounds you can hear",
                count = 3,
                icon = Icons.Outlined.Hearing,
                helperTip = "Near or distant: room hum, wind, traffic, or your own breath."
            ),
            GroundingStepData(
                stepTitle = "Connect with your body",
                instruction = "Move 3 parts of your body",
                count = 3,
                icon = Icons.Outlined.DirectionsWalk,
                helperTip = "Gently roll your shoulders, wiggle your toes, or unclench your jaw."
            )
        )
    } else {
        listOf(
            GroundingStepData(
                stepTitle = "Sight",
                instruction = "Notice 5 things you can see",
                count = 5,
                icon = Icons.Outlined.Visibility,
                helperTip = "Notice subtle details, patterns, light reflections."
            ),
            GroundingStepData(
                stepTitle = "Touch",
                instruction = "Notice 4 things you can touch",
                count = 4,
                icon = Icons.Outlined.PanTool,
                helperTip = "Feel the fabric of your clothes, the surface of your chair, the cool air."
            ),
            GroundingStepData(
                stepTitle = "Sound",
                instruction = "Notice 3 sounds you can hear",
                count = 3,
                icon = Icons.Outlined.Hearing,
                helperTip = "Focus on the background layers of sound around you."
            ),
            GroundingStepData(
                stepTitle = "Smell",
                instruction = "Notice 2 things you can smell",
                count = 2,
                icon = Icons.Outlined.WaterDrop,
                helperTip = "Breathe in softly. Can you smell fresh air, soap, or coffee?"
            ),
            GroundingStepData(
                stepTitle = "Taste",
                instruction = "Notice 1 thing you can taste",
                count = 1,
                icon = Icons.Outlined.Restaurant,
                helperTip = "Take a sip of water or notice the current taste in your mouth."
            )
        )
    }

    val currentStep = steps.getOrElse(currentStepIndex) { steps.last() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.padding(top = 12.dp)) {
            // Header & Switcher
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (chosenGroundingType == ExerciseType.GROUNDING_333) "3-3-3 Grounding" else "5-4-3-2-1 Grounding",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Recommendation explanation if present
            groundingRecommendation?.let { rec ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = rec.reason,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Exercise Type Toggle Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = chosenGroundingType == ExerciseType.GROUNDING_333,
                    onClick = { onSwitchType(ExerciseType.GROUNDING_333) },
                    label = { Text("3-3-3 Rule") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("switch_333_chip")
                )
                FilterChip(
                    selected = chosenGroundingType == ExerciseType.GROUNDING_54321,
                    onClick = { onSwitchType(ExerciseType.GROUNDING_54321) },
                    label = { Text("5-4-3-2-1 Rule") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("switch_54321_chip")
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Step Progress inside grounding
            Text(
                text = "Step ${currentStepIndex + 1} of ${steps.size}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Current Step Card with AnimatedContent
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "grounding_step_transition"
            ) { stepData ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = stepData.icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = stepData.instruction,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = stepData.helperTip,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "Take your time. No need to type.",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Advance Step Button
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
        ) {
            Button(
                onClick = { onAdvanceStep(steps.size) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("grounding_next_step_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = if (currentStepIndex + 1 < steps.size) "Noticed — Next step" else "Complete grounding",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = if (currentStepIndex + 1 < steps.size) Icons.AutoMirrored.Filled.ArrowForward else Icons.Outlined.Check,
                    contentDescription = null
                )
            }
        }
    }
}
