package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.ExerciseCompletionDao
import com.example.data.local.dao.JournalEntryDao
import com.example.data.local.dao.PauseSessionDao
import com.example.data.local.dao.ThoughtCaptureDao
import com.example.data.local.entity.ExerciseCompletion
import com.example.data.local.entity.JournalEntry
import com.example.data.local.entity.PauseSession
import com.example.data.local.entity.ThoughtCapture

@Database(
    entities = [
        PauseSession::class,
        ExerciseCompletion::class,
        JournalEntry::class,
        ThoughtCapture::class
    ],
    version = 1,
    exportSchema = false
)
abstract class OverthinkDatabase : RoomDatabase() {
    abstract fun pauseSessionDao(): PauseSessionDao
    abstract fun exerciseCompletionDao(): ExerciseCompletionDao
    abstract fun journalEntryDao(): JournalEntryDao
    abstract fun thoughtCaptureDao(): ThoughtCaptureDao

    companion object {
        @Volatile
        private var INSTANCE: OverthinkDatabase? = null

        fun getDatabase(context: Context): OverthinkDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    OverthinkDatabase::class.java,
                    "overthink_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
