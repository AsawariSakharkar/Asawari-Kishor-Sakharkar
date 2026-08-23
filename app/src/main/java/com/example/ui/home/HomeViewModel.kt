package com.example.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.ExerciseCompletion
import com.example.data.local.entity.PauseSession
import com.example.data.repository.ExerciseRecommendation
import com.example.data.repository.OverthinkRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

data class HomeUiState(
    val recommendations: List<ExerciseRecommendation> = emptyList(),
    val recentSessions: List<PauseSession> = emptyList(),
    val recentCompletions: List<ExerciseCompletion> = emptyList(),
    val privacyInsight: String? = null,
    val totalPausesCount: Int = 0,
    val emotionsLoggedCount: Int = 0,
    val daysCheckedInCount: Int = 0
)

class HomeViewModel(
    private val repository: OverthinkRepository
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        repository.allSessions,
        repository.allCompletions,
        repository.allJournalEntries
    ) { sessions, completions, journalEntries ->
        val recommendations = repository.recommendationEngine.getHomeRecommendations(completions, sessions)
        val insight = repository.recommendationEngine.generateHomeInsight(sessions, completions)
        val completedSessions = sessions.filter { it.isCompleted }

        val calendar = Calendar.getInstance()
        val sessionDays = sessions.map {
            calendar.timeInMillis = it.startTime
            "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.DAY_OF_YEAR)}"
        }
        val journalDays = journalEntries.map {
            calendar.timeInMillis = it.timestamp
            "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.DAY_OF_YEAR)}"
        }
        val uniqueDays = (sessionDays + journalDays).distinct().filter { it.isNotBlank() }.size

        val emotionsCount = sessions.count { it.startEmotion.isNotBlank() } +
                journalEntries.count { it.emotion.isNotBlank() }

        HomeUiState(
            recommendations = recommendations,
            recentSessions = completedSessions.take(5),
            recentCompletions = completions.take(5),
            privacyInsight = insight,
            totalPausesCount = completedSessions.size,
            emotionsLoggedCount = emotionsCount,
            daysCheckedInCount = uniqueDays
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    class Factory(private val repository: OverthinkRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(repository) as T
        }
    }
}

