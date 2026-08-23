package com.example.ui.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.datastore.UserPreferencesRepository
import com.example.data.local.entity.ExerciseCompletion
import com.example.data.local.entity.PauseSession
import com.example.data.local.entity.ThoughtCapture
import com.example.data.repository.OverthinkRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

enum class TimeRange(val displayName: String, val days: Int) {
    WEEK("Past 7 Days", 7),
    MONTH("Past 30 Days", 30)
}

data class EmotionCount(val emotion: String, val count: Int, val percentage: Float)
data class TriggerCount(val trigger: String, val count: Int, val percentage: Float)
data class TimeOfDayCount(val period: String, val timeRangeLabel: String, val count: Int, val percentage: Float)
data class EndFeelingCount(val feeling: String, val count: Int, val percentage: Float)
data class ExerciseHelpfulness(val exerciseName: String, val averageRating: Float, val count: Int)
data class RecurringWord(val word: String, val count: Int)
data class DayActivity(val dateLabel: String, val count: Int)

data class PrimaryInsight(
    val headline: String,
    val description: String,
    val topExerciseName: String? = null
)

data class InsightsUiState(
    val isTrackingEnabled: Boolean = true,
    val selectedTimeRange: TimeRange = TimeRange.WEEK,
    val totalSessionsCount: Int = 0,
    val totalMindfulMinutes: Int = 0,
    val primaryInsight: PrimaryInsight = PrimaryInsight(
        headline = "Your patterns will appear here",
        description = "Complete a few pause sessions to discover which exercises give you the most clarity and relief."
    ),
    val dailyActivity: List<DayActivity> = emptyList(),
    val topEmotions: List<EmotionCount> = emptyList(),
    val topTriggers: List<TriggerCount> = emptyList(),
    val timeOfDayBreakdown: List<TimeOfDayCount> = emptyList(),
    val endFeelings: List<EndFeelingCount> = emptyList(),
    val helpfulExercises: List<ExerciseHelpfulness> = emptyList(),
    val recurringThemes: List<RecurringWord> = emptyList()
)

class InsightsViewModel(
    private val repository: OverthinkRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _selectedTimeRange = MutableStateFlow(TimeRange.WEEK)

    val uiState: StateFlow<InsightsUiState> = combine(
        repository.allSessions,
        repository.allCompletions,
        repository.insightsThoughtCaptures,
        preferencesRepository.isPatternTrackingEnabled,
        _selectedTimeRange
    ) { sessions: List<PauseSession>, completions: List<ExerciseCompletion>, thoughts: List<ThoughtCapture>, trackingEnabled: Boolean, timeRange: TimeRange ->
        if (!trackingEnabled) {
            InsightsUiState(
                isTrackingEnabled = false,
                selectedTimeRange = timeRange
            )
        } else {
            val now = System.currentTimeMillis()
            val cutoff = now - (timeRange.days * 24L * 60L * 60L * 1000L)

            // Filter completed sessions within time range
            val periodSessions = sessions.filter { it.isCompleted && it.startTime >= cutoff }
            val periodCompletions = completions.filter { !it.isSkipped && it.timestamp >= cutoff }
            val totalSessions = periodSessions.size

            // 1. Primary "What helped lately" Insight
            val lighterSessionsCount = periodSessions.count {
                it.endFeeling.equals("A little lighter", ignoreCase = true) ||
                        it.endFeeling.equals("Much lighter", ignoreCase = true)
            }
            val lighterPercent = if (totalSessions > 0) (lighterSessionsCount * 100) / totalSessions else 0

            val topHelpfulCompletions = periodCompletions
                .groupBy { it.exerciseType }
                .map { (type, list) ->
                    val avg = list.map { it.helpfulnessRating.coerceAtLeast(1) }.average().toFloat()
                    val formattedName = formatExerciseName(type)
                    ExerciseHelpfulness(formattedName, avg, list.size)
                }
                .sortedByDescending { it.averageRating }

            val primaryInsight = if (totalSessions == 0) {
                PrimaryInsight(
                    headline = "Your patterns will appear here",
                    description = "Complete a pause session to discover which exercises give you the most clarity and relief."
                )
            } else if (topHelpfulCompletions.isNotEmpty()) {
                val best = topHelpfulCompletions.first()
                val headline = "${best.exerciseName} helped you most recently"
                val desc = if (lighterPercent > 0) {
                    "$lighterPercent% of your pauses led to a lighter headspace in the ${timeRange.displayName.lowercase()}, with ${best.exerciseName} receiving the highest ratings."
                } else {
                    "${best.exerciseName} was your highest rated exercise across ${best.count} completed pause sessions."
                }
                PrimaryInsight(headline = headline, description = desc, topExerciseName = best.exerciseName)
            } else {
                val headline = if (lighterPercent >= 50) "Pauses have provided noticeable relief" else "You took time to slow down"
                val desc = if (lighterPercent > 0) {
                    "$lighterPercent% of check-ins resulted in feeling lighter afterwards."
                } else {
                    "You completed $totalSessions mindful pauses in the ${timeRange.displayName.lowercase()}."
                }
                PrimaryInsight(headline = headline, description = desc)
            }

            // 2. Activity chart (Day by day)
            val cal = Calendar.getInstance()
            val numDays = timeRange.days
            val activityList = (0 until numDays).reversed().map { dayOffset ->
                val targetCal = (cal.clone() as Calendar).apply {
                    add(Calendar.DAY_OF_YEAR, -dayOffset)
                }
                val targetYear = targetCal.get(Calendar.YEAR)
                val targetDay = targetCal.get(Calendar.DAY_OF_YEAR)

                val label = if (numDays <= 7) {
                    when (targetCal.get(Calendar.DAY_OF_WEEK)) {
                        Calendar.SUNDAY -> "Sun"
                        Calendar.MONDAY -> "Mon"
                        Calendar.TUESDAY -> "Tue"
                        Calendar.WEDNESDAY -> "Wed"
                        Calendar.THURSDAY -> "Thu"
                        Calendar.FRIDAY -> "Fri"
                        Calendar.SATURDAY -> "Sat"
                        else -> ""
                    }
                } else {
                    // For 30 days show every few days or day number (e.g. "12")
                    "${targetCal.get(Calendar.DAY_OF_MONTH)}"
                }

                val count = periodSessions.count { s ->
                    val sCal = Calendar.getInstance().apply { timeInMillis = s.startTime }
                    sCal.get(Calendar.YEAR) == targetYear && sCal.get(Calendar.DAY_OF_YEAR) == targetDay
                }
                DayActivity(label, count)
            }

            // 3. Top Starting Emotions
            val maxEmotionCount = maxOf(1, totalSessions)
            val emotionCounts = periodSessions.groupingBy { it.startEmotion.ifBlank { "Unspecified" } }
                .eachCount()
                .entries.sortedByDescending { it.value }
                .take(5)
                .map { EmotionCount(it.key, it.value, it.value.toFloat() / maxEmotionCount.toFloat()) }

            // 4. Common Triggers
            val triggerCounts = periodSessions
                .mapNotNull { it.trigger.takeIf { t -> t.isNotBlank() } }
                .groupingBy { it }
                .eachCount()
                .entries.sortedByDescending { it.value }
                .take(5)
                .map {
                    val triggerTotal = periodSessions.count { s -> s.trigger.isNotBlank() }.coerceAtLeast(1)
                    TriggerCount(it.key, it.value, it.value.toFloat() / triggerTotal.toFloat())
                }

            // 5. Pause Times Breakdown
            var morningCount = 0
            var afternoonCount = 0
            var eveningCount = 0
            var nightCount = 0

            periodSessions.forEach { s ->
                val sCal = Calendar.getInstance().apply { timeInMillis = s.startTime }
                val hour = sCal.get(Calendar.HOUR_OF_DAY)
                when (hour) {
                    in 6..11 -> morningCount++
                    in 12..17 -> afternoonCount++
                    in 18..21 -> eveningCount++
                    else -> nightCount++
                }
            }

            val timeBreakdown = listOf(
                TimeOfDayCount("Morning", "6 AM – 12 PM", morningCount, if (totalSessions > 0) morningCount.toFloat() / totalSessions else 0f),
                TimeOfDayCount("Afternoon", "12 PM – 6 PM", afternoonCount, if (totalSessions > 0) afternoonCount.toFloat() / totalSessions else 0f),
                TimeOfDayCount("Evening", "6 PM – 10 PM", eveningCount, if (totalSessions > 0) eveningCount.toFloat() / totalSessions else 0f),
                TimeOfDayCount("Night", "10 PM – 6 AM", nightCount, if (totalSessions > 0) nightCount.toFloat() / totalSessions else 0f)
            )

            // 6. End Feelings Breakdown
            val endFeelingsList = listOf("Much lighter", "A little lighter", "The same", "Worse").map { feelingName ->
                val count = periodSessions.count { it.endFeeling.equals(feelingName, ignoreCase = true) }
                EndFeelingCount(feelingName, count, if (totalSessions > 0) count.toFloat() / totalSessions else 0f)
            }

            // 7. Recurring thought themes (where user opted in)
            val periodThoughts = thoughts.filter { it.timestamp >= cutoff }
            val allWords = periodThoughts.flatMap { tc ->
                tc.words.split(",", " ", ";", "\n")
                    .map { it.trim().lowercase() }
                    .filter { it.length > 2 && it !in listOf("the", "and", "that", "this", "with", "from", "about", "have") }
            }
            val recurring = allWords.groupingBy { it }
                .eachCount()
                .filter { it.value >= 2 }
                .entries.sortedByDescending { it.value }
                .take(6)
                .map { RecurringWord(it.key, it.value) }

            val totalMinutes = periodSessions.sumOf { (it.endTime - it.startTime) / 60000 }.toInt()

            InsightsUiState(
                isTrackingEnabled = true,
                selectedTimeRange = timeRange,
                totalSessionsCount = totalSessions,
                totalMindfulMinutes = totalMinutes,
                primaryInsight = primaryInsight,
                dailyActivity = activityList,
                topEmotions = emotionCounts,
                topTriggers = triggerCounts,
                timeOfDayBreakdown = timeBreakdown,
                endFeelings = endFeelingsList,
                helpfulExercises = topHelpfulCompletions,
                recurringThemes = recurring
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = InsightsUiState()
    )

    fun setTimeRange(timeRange: TimeRange) {
        _selectedTimeRange.value = timeRange
    }

    fun setPatternTracking(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setPatternTrackingEnabled(enabled)
        }
    }

    fun clearPatternHistory() {
        viewModelScope.launch {
            repository.clearAllData()
        }
    }

    private fun formatExerciseName(type: String): String {
        return when (type) {
            "slow_breathing" -> "Slow Breathing"
            "thought_capture" -> "Thought Capture"
            "let_it_pass" -> "Let It Pass"
            "grounding_333" -> "3-3-3 Grounding"
            "grounding_54321" -> "5-4-3-2-1 Grounding"
            "walk" -> "Five-Minute Walk"
            "belly_breathing" -> "Belly Breathing"
            "meditation" -> "Mindful Meditation"
            "identify_loop" -> "Identify Overthinking"
            "question_thought" -> "Question the Thought"
            "challenge_thought" -> "Challenge a Thought"
            else -> type.replace("_", " ").replaceFirstChar { it.uppercase() }
        }
    }

    class Factory(
        private val repository: OverthinkRepository,
        private val preferencesRepository: UserPreferencesRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return InsightsViewModel(repository, preferencesRepository) as T
        }
    }
}
