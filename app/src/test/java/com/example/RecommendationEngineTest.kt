package com.example

import com.example.data.local.entity.ExerciseCompletion
import com.example.data.local.entity.PauseSession
import com.example.data.model.ExerciseType
import com.example.data.repository.RecommendationEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RecommendationEngineTest {

    private lateinit var engine: RecommendationEngine

    @Before
    fun setup() {
        engine = RecommendationEngine()
    }

    @Test
    fun getHomeRecommendations_emptyHistory_returnsDefaultHelpfulExercises() {
        val recommendations = engine.getHomeRecommendations(emptyList(), emptyList())
        assertEquals(3, recommendations.size)
        assertEquals(ExerciseType.SLOW_BREATHING, recommendations[0].exerciseType)
    }

    @Test
    fun getHomeRecommendations_withRatings_prioritizesHighestRatedExercise() {
        val completions = listOf(
            ExerciseCompletion(
                id = 1,
                sessionId = 1,
                exerciseType = ExerciseType.GROUNDING_333.id,
                durationSeconds = 120,
                isSkipped = false,
                helpfulnessRating = 5,
                emotionAtTime = "Anxious",
                triggerAtTime = "",
                timestamp = System.currentTimeMillis()
            ),
            ExerciseCompletion(
                id = 2,
                sessionId = 1,
                exerciseType = ExerciseType.SLOW_BREATHING.id,
                durationSeconds = 300,
                isSkipped = false,
                helpfulnessRating = 3,
                emotionAtTime = "Anxious",
                triggerAtTime = "",
                timestamp = System.currentTimeMillis()
            )
        )

        val recommendations = engine.getHomeRecommendations(completions, emptyList())
        assertEquals(ExerciseType.GROUNDING_333, recommendations[0].exerciseType)
        assertTrue(recommendations[0].reason.contains("helpful"))
    }

    @Test
    fun getGroundingRecommendation_recommends54321WhenRatedHigher() {
        val completions = listOf(
            ExerciseCompletion(
                id = 1,
                sessionId = 1,
                exerciseType = ExerciseType.GROUNDING_54321.id,
                durationSeconds = 240,
                isSkipped = false,
                helpfulnessRating = 5,
                emotionAtTime = "Overwhelmed",
                triggerAtTime = "",
                timestamp = System.currentTimeMillis()
            ),
            ExerciseCompletion(
                id = 2,
                sessionId = 2,
                exerciseType = ExerciseType.GROUNDING_333.id,
                durationSeconds = 120,
                isSkipped = false,
                helpfulnessRating = 3,
                emotionAtTime = "Overwhelmed",
                triggerAtTime = "",
                timestamp = System.currentTimeMillis()
            )
        )

        val rec = engine.getGroundingRecommendation(completions)
        assertEquals(ExerciseType.GROUNDING_54321, rec.recommendedType)
        assertTrue(rec.reason.contains("5-4-3-2-1"))
    }

    @Test
    fun generateHomeInsight_insufficientData_returnsNull() {
        val sessions = listOf(
            PauseSession(
                id = 1,
                startTime = System.currentTimeMillis() - 10000,
                endTime = System.currentTimeMillis(),
                startEmotion = "Anxious",
                startIntensity = 3,
                trigger = "Work",
                endFeeling = "A little lighter",
                mostHelpfulPart = "Breathing",
                reflection = "",
                isCompleted = true
            )
        )
        val insight = engine.generateHomeInsight(sessions, emptyList())
        assertNull(insight)
    }

    @Test
    fun generateHomeInsight_sufficientData_returnsInsight() {
        val sessions = (1..4).map { i ->
            PauseSession(
                id = i.toLong(),
                startTime = System.currentTimeMillis() - (i * 60000),
                endTime = System.currentTimeMillis() - (i * 60000) + 300000,
                startEmotion = "Anxious",
                startIntensity = 4,
                trigger = "Social interaction",
                endFeeling = "A little lighter",
                mostHelpfulPart = "Breathing",
                reflection = "Felt better",
                isCompleted = true
            )
        }
        val insight = engine.generateHomeInsight(sessions, emptyList())
        assertNotNull(insight)
        assertTrue(insight!!.lowercase().contains("social interaction") || insight.lowercase().contains("breathing"))
    }
}
