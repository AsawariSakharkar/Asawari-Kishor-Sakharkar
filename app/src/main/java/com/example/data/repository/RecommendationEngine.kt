package com.example.data.repository

import com.example.data.local.entity.ExerciseCompletion
import com.example.data.local.entity.PauseSession
import com.example.data.model.ExerciseType

data class ExerciseRecommendation(
    val exerciseType: ExerciseType,
    val reason: String,
    val isPersonalized: Boolean
)

data class GroundingRecommendation(
    val recommendedType: ExerciseType, // GROUNDING_333 or GROUNDING_54321
    val reason: String
)

class RecommendationEngine {

    /**
     * Recommends exercises based purely on user-confirmed helpfulness ratings.
     * If no helpful ratings exist or insufficient data, returns default gentle starter tools.
     */
    fun getHomeRecommendations(
        completions: List<ExerciseCompletion>,
        recentSessions: List<PauseSession>
    ): List<ExerciseRecommendation> {
        val helpfulCompletions = completions.filter { it.helpfulnessRating >= 3 && !it.isSkipped }
        
        if (helpfulCompletions.isEmpty()) {
            return listOf(
                ExerciseRecommendation(
                    exerciseType = ExerciseType.SLOW_BREATHING,
                    reason = "A gentle starting point to reset your breathing rhythm.",
                    isPersonalized = false
                ),
                ExerciseRecommendation(
                    exerciseType = ExerciseType.GROUNDING_333,
                    reason = "Quick sensory check to anchor you in the present.",
                    isPersonalized = false
                ),
                ExerciseRecommendation(
                    exerciseType = ExerciseType.LET_IT_PASS,
                    reason = "Create distance from repetitive thoughts.",
                    isPersonalized = false
                )
            )
        }

        // Count helpful occurrences per exercise
        val helpfulCountByType = helpfulCompletions.groupBy { it.exerciseType }
            .mapValues { (_, list) -> list.size }

        // Determine recently used exercise to offer variety
        val lastUsedType = completions.firstOrNull()?.exerciseType

        val sortedTypes = helpfulCountByType.entries
            .sortedByDescending { it.value }
            .map { it.key }

        val recommendations = mutableListOf<ExerciseRecommendation>()

        for (typeId in sortedTypes) {
            val exerciseType = ExerciseType.entries.find { it.id == typeId } ?: continue
            val count = helpfulCountByType[typeId] ?: 0
            
            // If it's the very last used and we have other helpful options, we can prioritize variety
            val reason = if (count > 1) {
                "You found this helpful $count times."
            } else {
                "You previously marked this as helpful."
            }

            recommendations.add(
                ExerciseRecommendation(
                    exerciseType = exerciseType,
                    reason = reason,
                    isPersonalized = true
                )
            )
            if (recommendations.size >= 3) break
        }

        // Fill remaining slots if fewer than 3 rated exercises
        val defaultPool = listOf(
            ExerciseType.GROUNDING_333,
            ExerciseType.SLOW_BREATHING,
            ExerciseType.LET_IT_PASS,
            ExerciseType.BELLY_BREATHING,
            ExerciseType.MEDITATION
        )
        for (candidate in defaultPool) {
            if (recommendations.none { it.exerciseType == candidate }) {
                recommendations.add(
                    ExerciseRecommendation(
                        exerciseType = candidate,
                        reason = "Recommended for finding calm in the present moment.",
                        isPersonalized = false
                    )
                )
            }
            if (recommendations.size >= 3) break
        }

        return recommendations
    }

    /**
     * Recommends either 3-3-3 or 5-4-3-2-1 based on user's past self-rated helpfulness.
     */
    fun getGroundingRecommendation(completions: List<ExerciseCompletion>): GroundingRecommendation {
        val ratings333 = completions
            .filter { it.exerciseType == ExerciseType.GROUNDING_333.id && it.helpfulnessRating > 0 }
            .map { it.helpfulnessRating }

        val ratings54321 = completions
            .filter { it.exerciseType == ExerciseType.GROUNDING_54321.id && it.helpfulnessRating > 0 }
            .map { it.helpfulnessRating }

        val avg333 = if (ratings333.isNotEmpty()) ratings333.average() else 0.0
        val avg54321 = if (ratings54321.isNotEmpty()) ratings54321.average() else 0.0

        return when {
            ratings54321.isNotEmpty() && avg54321 > avg333 -> {
                GroundingRecommendation(
                    recommendedType = ExerciseType.GROUNDING_54321,
                    reason = "You previously marked 5-4-3-2-1 as helpful."
                )
            }
            ratings333.isNotEmpty() && avg333 >= avg54321 -> {
                GroundingRecommendation(
                    recommendedType = ExerciseType.GROUNDING_333,
                    reason = "You previously marked 3-3-3 as helpful."
                )
            }
            else -> {
                GroundingRecommendation(
                    recommendedType = ExerciseType.GROUNDING_333,
                    reason = "A quick 3-step sensory anchor to bring attention to the present."
                )
            }
        }
    }

    /**
     * Generates a privacy-friendly, factual insight statement only if enough confirmed data exists (at least 3 instances).
     */
    fun generateHomeInsight(
        sessions: List<PauseSession>,
        completions: List<ExerciseCompletion>
    ): String? {
        val completedSessions = sessions.filter { it.isCompleted && it.trigger.isNotBlank() && it.startEmotion.isNotBlank() }
        if (completedSessions.size < 3) return null

        // Check if a specific trigger is common
        val triggerCounts = completedSessions.groupBy { it.trigger }
            .mapValues { it.value.size }
            .filter { it.value >= 2 }

        val topTrigger = triggerCounts.maxByOrNull { it.value }

        if (topTrigger != null) {
            // Find most helpful part or exercise for this trigger
            val sessionsWithTrigger = completedSessions.filter { it.trigger == topTrigger.key }
            val mostHelpfulPart = sessionsWithTrigger.mapNotNull { it.mostHelpfulPart.takeIf { p -> p.isNotBlank() && p != "Not sure" } }
                .groupBy { it }
                .maxByOrNull { it.value.size }?.key

            if (mostHelpfulPart != null) {
                return "Based on your check-ins, you often find $mostHelpfulPart helpful after ${topTrigger.key.lowercase()}."
            }
            return "Based on your check-ins, ${topTrigger.key.lowercase()} is a frequent trigger you pause for."
        }

        val lighterCount = completedSessions.count { it.endFeeling == "A little lighter" || it.endFeeling == "Much lighter" }
        if (lighterCount >= 3) {
            return "Based on your check-ins, taking a pause often leaves your mind feeling lighter."
        }

        return null
    }
}
