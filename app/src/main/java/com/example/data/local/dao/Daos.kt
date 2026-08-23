package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.ExerciseCompletion
import com.example.data.local.entity.JournalEntry
import com.example.data.local.entity.PauseSession
import com.example.data.local.entity.ThoughtCapture
import kotlinx.coroutines.flow.Flow

@Dao
interface PauseSessionDao {
    @Query("SELECT * FROM pause_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<PauseSession>>

    @Query("SELECT * FROM pause_sessions WHERE id = :id")
    suspend fun getSessionById(id: Long): PauseSession?

    @Query("SELECT * FROM pause_sessions ORDER BY startTime DESC LIMIT 10")
    fun getRecentSessions(): Flow<List<PauseSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: PauseSession): Long

    @Update
    suspend fun updateSession(session: PauseSession)

    @Query("DELETE FROM pause_sessions")
    suspend fun deleteAll()
}

@Dao
interface ExerciseCompletionDao {
    @Query("SELECT * FROM exercise_completions ORDER BY timestamp DESC")
    fun getAllCompletions(): Flow<List<ExerciseCompletion>>

    @Query("SELECT * FROM exercise_completions WHERE exerciseType = :type")
    fun getCompletionsByType(type: String): Flow<List<ExerciseCompletion>>

    @Query("SELECT * FROM exercise_completions WHERE helpfulnessRating >= 3")
    fun getHelpfulCompletions(): Flow<List<ExerciseCompletion>>

    @Query("SELECT * FROM exercise_completions WHERE sessionId = :sessionId")
    suspend fun getCompletionsForSession(sessionId: Long): List<ExerciseCompletion>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletion(completion: ExerciseCompletion): Long

    @Query("DELETE FROM exercise_completions")
    suspend fun deleteAll()
}

@Dao
interface JournalEntryDao {
    @Query("SELECT * FROM journal_entries ORDER BY timestamp DESC")
    fun getAllEntries(): Flow<List<JournalEntry>>

    @Query("SELECT * FROM journal_entries WHERE id = :id")
    suspend fun getEntryById(id: Long): JournalEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: JournalEntry): Long

    @Update
    suspend fun updateEntry(entry: JournalEntry)

    @Delete
    suspend fun deleteEntry(entry: JournalEntry)

    @Query("DELETE FROM journal_entries WHERE id = :id")
    suspend fun deleteEntryById(id: Long)

    @Query("DELETE FROM journal_entries")
    suspend fun deleteAll()
}

@Dao
interface ThoughtCaptureDao {
    @Query("SELECT * FROM thought_captures WHERE includeInInsights = 1 ORDER BY timestamp DESC")
    fun getInsightsThoughts(): Flow<List<ThoughtCapture>>

    @Query("SELECT * FROM thought_captures ORDER BY timestamp DESC")
    fun getAllThoughts(): Flow<List<ThoughtCapture>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertThought(thought: ThoughtCapture): Long

    @Query("DELETE FROM thought_captures")
    suspend fun deleteAll()
}
