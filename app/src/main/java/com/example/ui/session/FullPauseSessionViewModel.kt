package com.example.ui.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.ExerciseCompletion
import com.example.data.model.Emotion
import com.example.data.model.EndFeeling
import com.example.data.model.ExerciseType
import com.example.data.model.HelpfulPart
import com.example.data.model.Trigger
import com.example.data.repository.GroundingRecommendation
import com.example.data.repository.OverthinkRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class SessionStep {
    data object CheckIn : SessionStep()
    data object Breathing : SessionStep() // Exercise 1
    data object ThoughtCaptureStep : SessionStep() // Exercise 2
    data object LetItPassStep : SessionStep() // Exercise 3
    data object GroundingStep : SessionStep() // Exercise 4
    data object WalkStep : SessionStep() // Exercise 5
    data object EndCheckIn : SessionStep()
    data object GentleSupport : SessionStep()
    data object Completed : SessionStep()
}

data class SessionUiState(
    val currentStep: SessionStep = SessionStep.CheckIn,
    val sessionId: Long = 0L,
    val selectedEmotion: Emotion = Emotion.ANXIOUS,
    val selectedIntensity: Int = 3,
    val selectedTrigger: Trigger? = null,
    
    // Exercise 1: Breathing
    val breathingSecondsRemaining: Int = 300,
    val isBreathingPaused: Boolean = false,
    val showOneMinuteCheck: Boolean = false,
    val isAmbientSoundPlaying: Boolean = false,

    // Exercise 2: Thought Capture
    val thoughtCaptureSecondsRemaining: Int = 50,
    val isThoughtCaptureRunning: Boolean = false,
    val enteredWords: List<String> = emptyList(),
    val currentWordInput: String = "",
    val showIncludeInsightsPrompt: Boolean = false,

    // Exercise 3: Let It Pass
    val letItPassThought: String = "",
    val isCloudDrifting: Boolean = false,
    val showLetItPassMessage: Boolean = false,

    // Exercise 4: Grounding
    val chosenGroundingType: ExerciseType = ExerciseType.GROUNDING_333,
    val groundingRecommendation: GroundingRecommendation? = null,
    val currentGroundingStepIndex: Int = 0,

    // Exercise 5: Walk
    val walkSecondsRemaining: Int = 300,
    val isWalkPaused: Boolean = false,
    val stepCount: Int = 0,
    val showWalkAlternatives: Boolean = false,
    val chosenAlternative: String? = null,

    // End Check-In
    val endFeeling: EndFeeling = EndFeeling.A_LITTLE_LIGHTER,
    val mostHelpfulPart: HelpfulPart = HelpfulPart.BREATHING,
    val reflectionText: String = "",
    val helpfulnessRating: Int = 4
) {
    val stepNumber: Int
        get() = when (currentStep) {
            SessionStep.CheckIn -> 0
            SessionStep.Breathing -> 1
            SessionStep.ThoughtCaptureStep -> 2
            SessionStep.LetItPassStep -> 3
            SessionStep.GroundingStep -> 4
            SessionStep.WalkStep -> 5
            SessionStep.EndCheckIn, SessionStep.GentleSupport, SessionStep.Completed -> 5
        }

    val estimatedMinutesRemaining: Int
        get() = when (currentStep) {
            SessionStep.CheckIn -> 15
            SessionStep.Breathing -> 14
            SessionStep.ThoughtCaptureStep -> 10
            SessionStep.LetItPassStep -> 8
            SessionStep.GroundingStep -> 6
            SessionStep.WalkStep -> 5
            else -> 0
        }
}

class FullPauseSessionViewModel(
    private val repository: OverthinkRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SessionUiState())
    val uiState: StateFlow<SessionUiState> = _uiState.asStateFlow()

    private var breathingTimerJob: Job? = null
    private var thoughtTimerJob: Job? = null
    private var walkTimerJob: Job? = null

    init {
        loadGroundingRecommendation()
    }

    private fun loadGroundingRecommendation() {
        viewModelScope.launch {
            val completions = repository.allCompletions.first()
            val rec = repository.recommendationEngine.getGroundingRecommendation(completions)
            _uiState.update {
                it.copy(
                    groundingRecommendation = rec,
                    chosenGroundingType = rec.recommendedType
                )
            }
        }
    }

    // Check-In Actions
    fun selectEmotion(emotion: Emotion) {
        _uiState.update { it.copy(selectedEmotion = emotion) }
    }

    fun selectIntensity(intensity: Int) {
        _uiState.update { it.copy(selectedIntensity = intensity) }
    }

    fun selectTrigger(trigger: Trigger?) {
        _uiState.update {
            it.copy(selectedTrigger = if (it.selectedTrigger == trigger) null else trigger)
        }
    }

    fun beginPause() {
        viewModelScope.launch {
            val sId = repository.startSession(
                emotion = _uiState.value.selectedEmotion.displayName,
                intensity = _uiState.value.selectedIntensity,
                trigger = _uiState.value.selectedTrigger?.displayName ?: ""
            )
            _uiState.update {
                it.copy(
                    sessionId = sId,
                    currentStep = SessionStep.Breathing
                )
            }
            startBreathingTimer()
        }
    }

    // Exercise 1: Breathing
    fun startBreathingTimer() {
        breathingTimerJob?.cancel()
        _uiState.update { it.copy(isBreathingPaused = false) }
        breathingTimerJob = viewModelScope.launch {
            while (_uiState.value.breathingSecondsRemaining > 0 && !_uiState.value.isBreathingPaused) {
                delay(1000L)
                val newSeconds = _uiState.value.breathingSecondsRemaining - 1
                val showPrompt = newSeconds == 240 // 1 minute passed (300 -> 240)
                _uiState.update {
                    it.copy(
                        breathingSecondsRemaining = newSeconds,
                        showOneMinuteCheck = it.showOneMinuteCheck || showPrompt
                    )
                }
            }
            if (_uiState.value.breathingSecondsRemaining <= 0) {
                moveToNextExercise(ExerciseType.SLOW_BREATHING, isSkipped = false)
            }
        }
    }

    fun toggleBreathingPause() {
        val paused = !_uiState.value.isBreathingPaused
        _uiState.update { it.copy(isBreathingPaused = paused) }
        if (paused) {
            breathingTimerJob?.cancel()
        } else {
            startBreathingTimer()
        }
    }

    fun toggleAmbientSound() {
        _uiState.update { it.copy(isAmbientSoundPlaying = !it.isAmbientSoundPlaying) }
    }

    fun dismissOneMinutePrompt() {
        _uiState.update { it.copy(showOneMinuteCheck = false) }
    }

    fun skipExercise(type: ExerciseType) {
        moveToNextExercise(type, isSkipped = true)
    }

    fun continueExercise(type: ExerciseType) {
        moveToNextExercise(type, isSkipped = false)
    }

    // Exercise 2: Thought Capture
    fun startThoughtCaptureTimer() {
        thoughtTimerJob?.cancel()
        _uiState.update { it.copy(isThoughtCaptureRunning = true) }
        thoughtTimerJob = viewModelScope.launch {
            while (_uiState.value.thoughtCaptureSecondsRemaining > 0) {
                delay(1000L)
                _uiState.update { it.copy(thoughtCaptureSecondsRemaining = it.thoughtCaptureSecondsRemaining - 1) }
            }
            _uiState.update { it.copy(showIncludeInsightsPrompt = true) }
        }
    }

    fun updateWordInput(text: String) {
        _uiState.update { it.copy(currentWordInput = text) }
    }

    fun addWord() {
        val word = _uiState.value.currentWordInput.trim()
        if (word.isNotBlank() && _uiState.value.enteredWords.size < 5) {
            _uiState.update {
                it.copy(
                    enteredWords = it.enteredWords + word,
                    currentWordInput = ""
                )
            }
        }
    }

    fun removeWord(word: String) {
        _uiState.update { it.copy(enteredWords = it.enteredWords - word) }
    }

    fun finishThoughtCapture(includeInInsights: Boolean) {
        viewModelScope.launch {
            if (_uiState.value.enteredWords.isNotEmpty()) {
                repository.saveThoughtCapture(
                    sessionId = _uiState.value.sessionId,
                    words = _uiState.value.enteredWords.joinToString(", "),
                    includeInInsights = includeInInsights
                )
            }
            moveToNextExercise(ExerciseType.THOUGHT_CAPTURE, isSkipped = false)
        }
    }

    // Exercise 3: Let It Pass
    fun updateLetItPassThought(thought: String) {
        _uiState.update { it.copy(letItPassThought = thought) }
    }

    fun triggerLetItPass() {
        _uiState.update { it.copy(isCloudDrifting = true, showLetItPassMessage = true) }
    }

    fun finishLetItPass(savePrivately: Boolean) {
        viewModelScope.launch {
            if (savePrivately && _uiState.value.letItPassThought.isNotBlank()) {
                repository.saveThoughtCapture(
                    sessionId = _uiState.value.sessionId,
                    words = _uiState.value.letItPassThought,
                    includeInInsights = false // Keep strictly private
                )
            }
            moveToNextExercise(ExerciseType.LET_IT_PASS, isSkipped = false)
        }
    }

    // Exercise 4: Grounding
    fun switchGroundingType(type: ExerciseType) {
        _uiState.update {
            it.copy(
                chosenGroundingType = type,
                currentGroundingStepIndex = 0
            )
        }
    }

    fun advanceGroundingStep(totalSteps: Int) {
        val nextIndex = _uiState.value.currentGroundingStepIndex + 1
        if (nextIndex < totalSteps) {
            _uiState.update { it.copy(currentGroundingStepIndex = nextIndex) }
        } else {
            moveToNextExercise(_uiState.value.chosenGroundingType, isSkipped = false)
        }
    }

    // Exercise 5: Walk
    fun startWalkTimer() {
        walkTimerJob?.cancel()
        _uiState.update { it.copy(isWalkPaused = false) }
        walkTimerJob = viewModelScope.launch {
            while (_uiState.value.walkSecondsRemaining > 0 && !_uiState.value.isWalkPaused) {
                delay(1000L)
                _uiState.update { it.copy(walkSecondsRemaining = it.walkSecondsRemaining - 1) }
            }
            if (_uiState.value.walkSecondsRemaining <= 0) {
                moveToNextExercise(ExerciseType.WALK, isSkipped = false)
            }
        }
    }

    fun toggleWalkPause() {
        val paused = !_uiState.value.isWalkPaused
        _uiState.update { it.copy(isWalkPaused = paused) }
        if (paused) {
            walkTimerJob?.cancel()
        } else {
            startWalkTimer()
        }
    }

    fun incrementStepCount() {
        _uiState.update { it.copy(stepCount = it.stepCount + 1) }
    }

    fun showWalkAlternatives(show: Boolean) {
        _uiState.update { it.copy(showWalkAlternatives = show) }
    }

    fun chooseWalkAlternative(alt: String) {
        _uiState.update { it.copy(chosenAlternative = alt, showWalkAlternatives = false) }
    }

    // End Check-In
    fun setEndFeeling(feeling: EndFeeling) {
        _uiState.update { it.copy(endFeeling = feeling) }
    }

    fun setMostHelpfulPart(part: HelpfulPart) {
        _uiState.update { it.copy(mostHelpfulPart = part) }
    }

    fun setReflectionText(text: String) {
        _uiState.update { it.copy(reflectionText = text) }
    }

    fun setHelpfulnessRating(rating: Int) {
        _uiState.update { it.copy(helpfulnessRating = rating) }
    }

    fun completeSession(onFinish: () -> Unit) {
        val state = _uiState.value
        if (state.endFeeling == EndFeeling.WORSE && state.currentStep != SessionStep.GentleSupport) {
            _uiState.update { it.copy(currentStep = SessionStep.GentleSupport) }
            return
        }

        viewModelScope.launch {
            repository.finishSession(
                sessionId = state.sessionId,
                endFeeling = state.endFeeling.displayName,
                mostHelpfulPart = state.mostHelpfulPart.displayName,
                reflection = state.reflectionText
            )
            // Also record an overall completion
            repository.recordExerciseCompletion(
                sessionId = state.sessionId,
                exerciseType = "full_pause_session",
                durationSeconds = (15 * 60) - (state.breathingSecondsRemaining + state.walkSecondsRemaining),
                isSkipped = false,
                helpfulnessRating = state.helpfulnessRating,
                emotion = state.selectedEmotion.displayName,
                trigger = state.selectedTrigger?.displayName ?: ""
            )
            onFinish()
        }
    }

    fun endSessionEarly(onFinish: () -> Unit) {
        // Record as skipped / ended early calmly without punishment
        viewModelScope.launch {
            val state = _uiState.value
            if (state.sessionId > 0) {
                repository.finishSession(
                    sessionId = state.sessionId,
                    endFeeling = "Ended early",
                    mostHelpfulPart = "None",
                    reflection = ""
                )
            }
            breathingTimerJob?.cancel()
            thoughtTimerJob?.cancel()
            walkTimerJob?.cancel()
            onFinish()
        }
    }

    private fun moveToNextExercise(completedType: ExerciseType, isSkipped: Boolean) {
        val state = _uiState.value
        viewModelScope.launch {
            repository.recordExerciseCompletion(
                sessionId = state.sessionId,
                exerciseType = completedType.id,
                durationSeconds = when (completedType) {
                    ExerciseType.SLOW_BREATHING -> 300 - state.breathingSecondsRemaining
                    ExerciseType.THOUGHT_CAPTURE -> 50 - state.thoughtCaptureSecondsRemaining
                    ExerciseType.WALK -> 300 - state.walkSecondsRemaining
                    else -> 120
                },
                isSkipped = isSkipped,
                helpfulnessRating = if (isSkipped) 1 else 4,
                emotion = state.selectedEmotion.displayName,
                trigger = state.selectedTrigger?.displayName ?: ""
            )
        }

        when (state.currentStep) {
            SessionStep.Breathing -> {
                breathingTimerJob?.cancel()
                _uiState.update { it.copy(currentStep = SessionStep.ThoughtCaptureStep) }
                startThoughtCaptureTimer()
            }
            SessionStep.ThoughtCaptureStep -> {
                thoughtTimerJob?.cancel()
                _uiState.update { it.copy(currentStep = SessionStep.LetItPassStep) }
            }
            SessionStep.LetItPassStep -> {
                _uiState.update { it.copy(currentStep = SessionStep.GroundingStep) }
            }
            SessionStep.GroundingStep -> {
                _uiState.update { it.copy(currentStep = SessionStep.WalkStep) }
                startWalkTimer()
            }
            SessionStep.WalkStep -> {
                walkTimerJob?.cancel()
                _uiState.update { it.copy(currentStep = SessionStep.EndCheckIn) }
            }
            else -> {}
        }
    }

    override fun onCleared() {
        super.onCleared()
        breathingTimerJob?.cancel()
        thoughtTimerJob?.cancel()
        walkTimerJob?.cancel()
    }

    class Factory(private val repository: OverthinkRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FullPauseSessionViewModel(repository) as T
        }
    }
}
