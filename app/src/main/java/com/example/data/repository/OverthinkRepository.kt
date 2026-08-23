package com.example.data.repository

import com.example.data.local.OverthinkDatabase
import com.example.data.local.entity.ExerciseCompletion
import com.example.data.local.entity.JournalEntry
import com.example.data.local.entity.PauseSession
import com.example.data.local.entity.ThoughtCapture
import kotlinx.coroutines.flow.Flow

class OverthinkRepository(
    private val database: OverthinkDatabase,
    val recommendationEngine: RecommendationEngine = RecommendationEngine()
) {
    private val sessionDao = database.pauseSessionDao()
    private val completionDao = database.exerciseCompletionDao()
    private val journalDao = database.journalEntryDao()
    private val thoughtDao = database.thoughtCaptureDao()

    // Sessions
    val allSessions: Flow<List<PauseSession>> = sessionDao.getAllSessions()
    val recentSessions: Flow<List<PauseSession>> = sessionDao.getRecentSessions()

    suspend fun startSession(emotion: String, intensity: Int, trigger: String): Long {
        val session = PauseSession(
            startTime = System.currentTimeMillis(),
            startEmotion = emotion,
            startIntensity = intensity,
            trigger = trigger,
            isCompleted = false
        )
        return sessionDao.insertSession(session)
    }

    suspend fun finishSession(
        sessionId: Long,
        endFeeling: String,
        mostHelpfulPart: String,
        reflection: String
    ) {
        val existing = sessionDao.getSessionById(sessionId) ?: return
        val updated = existing.copy(
            endTime = System.currentTimeMillis(),
            endFeeling = endFeeling,
            mostHelpfulPart = mostHelpfulPart,
            reflection = reflection,
            isCompleted = true
        )
        sessionDao.updateSession(updated)
    }

    // Exercise Completions
    val allCompletions: Flow<List<ExerciseCompletion>> = completionDao.getAllCompletions()
    val helpfulCompletions: Flow<List<ExerciseCompletion>> = completionDao.getHelpfulCompletions()

    suspend fun recordExerciseCompletion(
        sessionId: Long?,
        exerciseType: String,
        durationSeconds: Int,
        isSkipped: Boolean,
        helpfulnessRating: Int = -1,
        emotion: String = "",
        trigger: String = ""
    ): Long {
        val completion = ExerciseCompletion(
            sessionId = sessionId,
            exerciseType = exerciseType,
            durationSeconds = durationSeconds,
            isSkipped = isSkipped,
            helpfulnessRating = helpfulnessRating,
            timestamp = System.currentTimeMillis(),
            emotionAtTime = emotion,
            triggerAtTime = trigger
        )
        return completionDao.insertCompletion(completion)
    }

    // Journal
    val allJournalEntries: Flow<List<JournalEntry>> = journalDao.getAllEntries()

    suspend fun saveJournalEntry(
        id: Long = 0L,
        emotion: String,
        content: String,
        reflectionPrompt: String,
        isVoiceEntry: Boolean
    ): Long {
        val entry = JournalEntry(
            id = id,
            timestamp = System.currentTimeMillis(),
            emotion = emotion,
            content = content,
            reflectionPrompt = reflectionPrompt,
            isVoiceEntry = isVoiceEntry
        )
        return if (id > 0L) {
            journalDao.updateEntry(entry)
            id
        } else {
            journalDao.insertEntry(entry)
        }
    }

    suspend fun deleteJournalEntry(entry: JournalEntry) {
        journalDao.deleteEntry(entry)
    }

    suspend fun deleteJournalEntryById(id: Long) {
        journalDao.deleteEntryById(id)
    }

    // Thought Captures
    val insightsThoughts: Flow<List<ThoughtCapture>> = thoughtDao.getInsightsThoughts()
    val insightsThoughtCaptures: Flow<List<ThoughtCapture>> get() = insightsThoughts
    val allThoughts: Flow<List<ThoughtCapture>> = thoughtDao.getAllThoughts()

    suspend fun saveThoughtCapture(
        sessionId: Long?,
        words: String,
        includeInInsights: Boolean
    ): Long {
        val capture = ThoughtCapture(
            sessionId = sessionId,
            words = words,
            includeInInsights = includeInInsights,
            timestamp = System.currentTimeMillis()
        )
        return thoughtDao.insertThought(capture)
    }

    // Privacy & Reset
    suspend fun clearAllData() {
        sessionDao.deleteAll()
        completionDao.deleteAll()
        journalDao.deleteAll()
        thoughtDao.deleteAll()
    }

    suspend fun resetPersonalizationData() {
        completionDao.deleteAll()
    }
}
