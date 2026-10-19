package com.jokellojewels.game.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jokellojewels.game.data.GameRepository
import com.jokellojewels.game.logic.GameLogic
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Immutable snapshot of saved progress for menus, levels, results and settings. */
data class ProgressUiState(
    val loaded: Boolean = false,
    val quizBest: Map<String, Int> = emptyMap(),
    val pairsBest: Map<String, Int> = emptyMap(),
    val soundEnabled: Boolean = true,
) {
    val completedLevels: Int get() = GameLogic.completedLevelCount(pairsBest)
    fun quizBestFor(categoryId: String): Int? = quizBest[categoryId]
    fun isLevelUnlocked(level: Int): Boolean = GameLogic.isLevelUnlocked(level, pairsBest)
    fun isLevelCleared(level: Int): Boolean = GameLogic.isLevelCleared(level, pairsBest)
    fun bestMatched(level: Int): Int = GameLogic.bestMatched(level, pairsBest)
}

class ProgressViewModel(private val repository: GameRepository) : ViewModel() {

    val state: StateFlow<ProgressUiState> = repository.data
        .map { data ->
            ProgressUiState(
                loaded = true,
                quizBest = data.quizBest,
                pairsBest = data.pairsBest,
                soundEnabled = data.settings.soundEnabled,
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProgressUiState())

    fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setSoundEnabled(enabled) }
    }

    fun resetAll() {
        viewModelScope.launch { repository.resetAll() }
    }
}
