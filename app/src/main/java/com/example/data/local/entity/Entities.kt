package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "pause_sessions")
data class PauseSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long = 0L,
    val startEmotion: String = "",
    val startIntensity: Int = 3,
    val trigger: String = "",
    val endFeeling: String = "",
    val mostHelpfulPart: String = "",
    val reflection: String = "",
    val isCompleted: Boolean = false
) {
    val emotion: String get() = startEmotion
    val intensity: Int get() = startIntensity
}

@Entity(tableName = "exercise_completions")
data class ExerciseCompletion(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val sessionId: Long? = null,
    val exerciseType: String,
    val durationSeconds: Int = 0,
    val isSkipped: Boolean = false,
    val helpfulnessRating: Int = -1, // 1-5, or -1 if unrated
    val timestamp: Long = System.currentTimeMillis(),
    val emotionAtTime: String = "",
    val triggerAtTime: String = ""
)

@Entity(tableName = "journal_entries")
data class JournalEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val timestamp: Long = System.currentTimeMillis(),
    val emotion: String = "",
    val content: String = "",
    val reflectionPrompt: String = "",
    val isVoiceEntry: Boolean = false
)

@Entity(tableName = "thought_captures")
data class ThoughtCapture(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val sessionId: Long? = null,
    val words: String = "", // Comma-separated or single thought
    val includeInInsights: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
