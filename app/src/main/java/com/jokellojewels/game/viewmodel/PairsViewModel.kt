package com.jokellojewels.game.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jokellojewels.game.audio.SoundManager
import com.jokellojewels.game.data.GameRepository
import com.jokellojewels.game.logic.GameLogic
import com.jokellojewels.game.logic.LevelConfig
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class PairsCardUi(
    val id: Int,
    val itemIndex: Int,
    val faceUp: Boolean = false,
    val matched: Boolean = false,
)

enum class PairsStatus { LOADING, INVALID, LOCKED, PLAYING, FINISHED }

data class PairsOutcome(
    val won: Boolean,
    val matchedPairs: Int,
)

data class PairsUiState(
    val status: PairsStatus = PairsStatus.LOADING,
    val config: LevelConfig? = null,
    val cards: List<PairsCardUi> = emptyList(),
    val secondsLeft: Int = 0,
    val paused: Boolean = false,
    val outcome: PairsOutcome? = null,
) {
    val matchedPairs: Int get() = cards.count { it.matched } / 2
    val totalPairs: Int get() = config?.pairs ?: 0
}

class PairsViewModel(
    private val level: Int,
    private val repository: GameRepository,
    private val sound: SoundManager,
) : ViewModel() {

    private val _state = MutableStateFlow(PairsUiState())
    val state: StateFlow<PairsUiState> = _state.asStateFlow()

    private var timerJob: Job? = null
    private var flipBackJob: Job? = null

    /** Ids of currently face-up, not-yet-matched cards (0, 1 or 2). */
    private val openIds = mutableListOf<Int>()

    init {
        viewModelScope.launch {
            val config = GameLogic.levelConfig(level)
            if (config == null) {
                _state.value = PairsUiState(status = PairsStatus.INVALID)
                return@launch
            }
            val saved = repository.current()
            if (!GameLogic.isLevelUnlocked(level, saved.pairsBest)) {
                _state.value = PairsUiState(status = PairsStatus.LOCKED, config = config)
                return@launch
            }
            startNewGame(config)
        }
    }

    private fun startNewGame(config: LevelConfig) {
        timerJob?.cancel()
        flipBackJob?.cancel()
        openIds.clear()
        val deck = GameLogic.buildDeck(config.pairs).map { PairsCardUi(id = it.id, itemIndex = it.itemIndex) }
        _state.value = PairsUiState(
            status = PairsStatus.PLAYING,
            config = config,
            cards = deck,
            secondsLeft = config.seconds,
        )
        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1_000)
                val s = _state.value
                if (s.status != PairsStatus.PLAYING || s.paused) break
                val left = (s.secondsLeft - 1).coerceAtLeast(0)
                _state.update { it.copy(secondsLeft = left) }
                if (left == 0) {
                    finish(won = false)
                    break
                }
            }
        }
    }

    fun onCardTapped(cardId: Int) {
        val s = _state.value
        if (s.status != PairsStatus.PLAYING || s.paused) return
        if (openIds.size >= 2) return
        val card = s.cards.firstOrNull { it.id == cardId } ?: return
        if (card.faceUp || card.matched) return

        sound.click()
        openIds.add(cardId)
        _state.update { st -> st.copy(cards = st.cards.map { if (it.id == cardId) it.copy(faceUp = true) else it }) }

        if (openIds.size == 2) {
            val (firstId, secondId) = openIds
            val cards = _state.value.cards
            val first = cards.first { it.id == firstId }
            val second = cards.first { it.id == secondId }
            if (first.itemIndex == second.itemIndex) {
                openIds.clear()
                sound.match()
                _state.update { st ->
                    st.copy(cards = st.cards.map {
                        if (it.id == firstId || it.id == secondId) it.copy(faceUp = true, matched = true) else it
                    })
                }
                if (_state.value.cards.all { it.matched }) finish(won = true)
            } else {
                flipBackJob = viewModelScope.launch {
                    delay(MISMATCH_DELAY_MS)
                    _state.update { st ->
                        st.copy(cards = st.cards.map {
                            if ((it.id == firstId || it.id == secondId) && !it.matched) it.copy(faceUp = false) else it
                        })
                    }
                    openIds.clear()
                }
            }
        }
    }

    private fun finish(won: Boolean) {
        val s = _state.value
        if (s.status != PairsStatus.PLAYING) return
        timerJob?.cancel()
        flipBackJob?.cancel()
        val matched = s.matchedPairs
        // FINISHED stops all input immediately; outcome is published only after saving,
        // so the result screen and the next level's unlock always see the saved best.
        _state.update { it.copy(status = PairsStatus.FINISHED) }
        if (won) sound.win() else sound.lose()
        viewModelScope.launch {
            withContext(NonCancellable) { repository.savePairsBest(level, matched) }
            _state.update { it.copy(outcome = PairsOutcome(won = won, matchedPairs = matched)) }
        }
    }

    fun pause() {
        val s = _state.value
        if (s.status != PairsStatus.PLAYING || s.paused) return
        timerJob?.cancel()
        _state.update { it.copy(paused = true) }
    }

    fun resume() {
        val s = _state.value
        if (s.status != PairsStatus.PLAYING || !s.paused) return
        _state.update { it.copy(paused = false) }
        startTimer()
    }

    fun restart() {
        val config = _state.value.config ?: return
        if (_state.value.status == PairsStatus.LOCKED || _state.value.status == PairsStatus.INVALID) return
        startNewGame(config)
    }

    override fun onCleared() {
        timerJob?.cancel()
        flipBackJob?.cancel()
        super.onCleared()
    }

    companion object {
        const val MISMATCH_DELAY_MS = 700L
    }
}
