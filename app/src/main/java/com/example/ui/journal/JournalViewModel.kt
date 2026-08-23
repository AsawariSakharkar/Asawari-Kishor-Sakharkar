package com.example.ui.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.JournalEntry
import com.example.data.model.Emotion
import com.example.data.repository.OverthinkRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class JournalInputMode {
    WRITE, SPEAK
}

data class JournalUiState(
    val entries: List<JournalEntry> = emptyList(),
    val editingEntryId: Long? = null,
    val selectedEmotion: Emotion = Emotion.ANXIOUS,
    val textContent: String = "",
    val selectedPrompt: String? = null,
    val inputMode: JournalInputMode = JournalInputMode.WRITE,
    val isRecordingVoice: Boolean = false
)

class JournalViewModel(
    private val repository: OverthinkRepository
) : ViewModel() {

    private val _editingState = MutableStateFlow(
        Triple<Long?, Emotion, String>(null, Emotion.ANXIOUS, "")
    )
    private val _selectedPrompt = MutableStateFlow<String?>(null)
    private val _inputMode = MutableStateFlow(JournalInputMode.WRITE)
    private val _isRecording = MutableStateFlow(false)

    val uiState: StateFlow<JournalUiState> = combine(
        repository.allJournalEntries,
        _editingState,
        _selectedPrompt,
        _inputMode,
        _isRecording
    ) { entries, editing, prompt, mode, recording ->
        JournalUiState(
            entries = entries,
            editingEntryId = editing.first,
            selectedEmotion = editing.second,
            textContent = editing.third,
            selectedPrompt = prompt,
            inputMode = mode,
            isRecordingVoice = recording
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = JournalUiState()
    )

    fun selectEmotion(emotion: Emotion) {
        _editingState.update { it.copy(second = emotion) }
    }

    fun setInputMode(mode: JournalInputMode) {
        _inputMode.value = mode
    }

    fun updateText(text: String) {
        _editingState.update { it.copy(third = text) }
    }

    fun selectPrompt(prompt: String?) {
        // Toggle prompt: if already selected, deselect
        if (_selectedPrompt.value == prompt) {
            _selectedPrompt.value = null
        } else {
            _selectedPrompt.value = prompt
        }
    }

    fun appendSpokenText(spokenText: String) {
        _editingState.update { current ->
            val newText = if (current.third.isBlank()) spokenText else "${current.third} $spokenText"
            current.copy(third = newText)
        }
    }

    fun setRecording(isRecording: Boolean) {
        _isRecording.value = isRecording
    }

    fun startEditEntry(entry: JournalEntry) {
        val emotion = Emotion.entries.find { it.displayName == entry.emotion } ?: Emotion.ANXIOUS
        _editingState.value = Triple(entry.id, emotion, entry.content)
        _selectedPrompt.value = entry.reflectionPrompt.ifBlank { null }
        _inputMode.value = if (entry.isVoiceEntry) JournalInputMode.SPEAK else JournalInputMode.WRITE
    }

    fun cancelEdit() {
        _editingState.value = Triple(null, Emotion.ANXIOUS, "")
        _selectedPrompt.value = null
    }

    fun saveEntry() {
        val (id, emotion, content) = _editingState.value
        if (content.isBlank()) return

        val isVoice = _inputMode.value == JournalInputMode.SPEAK
        val prompt = _selectedPrompt.value ?: ""

        viewModelScope.launch {
            repository.saveJournalEntry(
                id = id ?: 0L,
                emotion = emotion.displayName,
                content = content.trim(),
                reflectionPrompt = prompt,
                isVoiceEntry = isVoice
            )
            _editingState.value = Triple(null, Emotion.ANXIOUS, "")
            _selectedPrompt.value = null
            _inputMode.value = JournalInputMode.WRITE
        }
    }

    fun deleteEntry(entry: JournalEntry) {
        viewModelScope.launch {
            repository.deleteJournalEntry(entry)
            if (_editingState.value.first == entry.id) {
                cancelEdit()
            }
        }
    }

    class Factory(private val repository: OverthinkRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return JournalViewModel(repository) as T
        }
    }
}
