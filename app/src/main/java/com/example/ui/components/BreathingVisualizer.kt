package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class BreathPhase(val instruction: String) {
    INHALE("Breathe in"),
    HOLD("Hold"),
    EXHALE("Breathe out"),
    REST("Rest")
}

@Composable
fun BreathingVisualizer(
    modifier: Modifier = Modifier,
    size: Dp = 180.dp,
    showText: Boolean = true,
    isPaused: Boolean = false,
    customInstruction: String? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "breath_transition")

    val animationProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "breath_progress"
    )

    // 8-second cycle:
    // 0.0 - 0.45: Inhale (scale 0.55 -> 1.0)
    // 0.45 - 0.55: Hold (scale 1.0)
    // 0.55 - 0.95: Exhale (scale 1.0 -> 0.55)
    // 0.95 - 1.0: Rest (scale 0.55)
    val (phase, progress) = when {
        isPaused -> Pair(BreathPhase.REST, 0.75f)
        animationProgress < 0.45f -> {
            val t = animationProgress / 0.45f
            val s = 0.55f + 0.45f * FastOutSlowInEasing.transform(t)
            Pair(BreathPhase.INHALE, s)
        }
        animationProgress < 0.55f -> {
            Pair(BreathPhase.HOLD, 1.0f)
        }
        animationProgress < 0.95f -> {
            val t = (animationProgress - 0.55f) / 0.40f
            val s = 1.0f - 0.45f * FastOutSlowInEasing.transform(t)
            Pair(BreathPhase.EXHALE, s)
        }
        else -> {
            Pair(BreathPhase.REST, 0.55f)
        }
    }

    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val primaryColor = MaterialTheme.colorScheme.primary
    val outlineColor = MaterialTheme.colorScheme.outlineVariant

    Column(
        modifier = modifier.testTag("breathing_visualizer"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(size),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(size)) {
                val center = Offset(this.size.width / 2f, this.size.height / 2f)
                val maxRadius = this.size.minDimension / 2f

                // Outer guide ring (subtle outline)
                drawCircle(
                    color = outlineColor.copy(alpha = 0.5f),
                    radius = maxRadius * 0.92f,
                    center = center,
                    style = Stroke(width = 1.5.dp.toPx())
                )

                // Middle expanding ring (dusty-pink primaryContainer)
                val outerAnimatedRadius = maxRadius * (0.45f + 0.47f * progress)
                drawCircle(
                    color = primaryContainer.copy(alpha = 0.45f),
                    radius = outerAnimatedRadius,
                    center = center
                )

                // Inner core ring
                val coreRadius = maxRadius * (0.28f + 0.32f * progress)
                drawCircle(
                    color = primaryColor.copy(alpha = 0.85f),
                    radius = coreRadius,
                    center = center
                )
            }
        }

        if (showText) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = customInstruction ?: phase.instruction,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

