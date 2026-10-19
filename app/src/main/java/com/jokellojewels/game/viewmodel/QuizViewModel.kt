package com.jokellojewels.game.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jokellojewels.game.audio.SoundManager
import com.jokellojewels.game.data.GameRepository
import com.jokellojewels.game.logic.QuizCategory
import com.jokellojewels.game.logic.QuizContent
import com.jokellojewels.game.logic.QuizQuestion
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class QuizUiState(
    val category: QuizCategory? = null,
    val index: Int = 0,
    val selectedIndex: Int? = null,
    val score: Int = 0,
    val paused: Boolean = false,
    /** Set once the final score is saved; the screen then navigates to the results panel. */
    val finishedScore: Int? = null,
) {
    val total: Int get() = category?.questions?.size ?: 0
    val question: QuizQuestion? get() = category?.questions?.getOrNull(index)
    val answered: Boolean get() = selectedIndex != null
    val progress: Float get() = if (total == 0) 0f else (index + if (answered) 1 else 0).toFloat() / total
}

class QuizViewModel(
    categoryId: String,
    private val repository: GameRepository,
    private val sound: SoundManager,
) : ViewModel() {

    private val category: QuizCategory? = QuizContent.category(categoryId)

    private val _state = MutableStateFlow(QuizUiState(category = category))
    val state: StateFlow<QuizUiState> = _state.asStateFlow()

    private var advanceJob: Job? = null

    fun answer(optionIndex: Int) {
        val s = _state.value
        val question = s.question ?: return
        if (s.answered || s.paused || s.finishedScore != null) return
        if (optionIndex !in question.options.indices) return

        val correct = optionIndex == question.correctIndex
        if (correct) sound.correct() else sound.wrong()
        _state.update { it.copy(selectedIndex = optionIndex, score = it.score + if (correct) 1 else 0) }

        advanceJob?.cancel()
        advanceJob = viewModelScope.launch {
            delay(ADVANCE_DELAY_MS)
            _state.first { !it.paused } // hold while the pause overlay is open
            advance()
        }
    }

    private suspend fun advance() {
        val s = _state.value
        val nextIndex = s.index + 1
        if (nextIndex < s.total) {
            _state.update { it.copy(index = nextIndex, selectedIndex = null) }
        } else {
            val finalScore = s.score
            category?.let { cat -> withContext(NonCancellable) { repository.saveQuizBest(cat.id, finalScore) } }
            _state.update { it.copy(finishedScore = finalScore) }
        }
    }

    fun pause() {
        if (_state.value.finishedScore == null) _state.update { it.copy(paused = true) }
    }

    fun resume() {
        _state.update { it.copy(paused = false) }
    }

    fun restart() {
        advanceJob?.cancel()
        advanceJob = null
        _state.value = QuizUiState(category = category)
    }

    companion object {
        const val ADVANCE_DELAY_MS = 750L
    }
}
