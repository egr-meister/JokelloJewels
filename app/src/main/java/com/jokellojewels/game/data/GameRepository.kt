package com.jokellojewels.game.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

/**
 * Single local source of truth. Stores the whole [GameData] as one serialized JSON string.
 * Reads never crash (IO errors and bad JSON fall back to defaults); writes swallow IO errors.
 */
class GameRepository(private val dataStore: DataStore<Preferences>) {

    private val dataKey = stringPreferencesKey(KEY_GAME_DATA)

    val data: Flow<GameData> = dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs -> GameSerializer.decode(prefs[dataKey]) }
        .distinctUntilChanged()

    suspend fun current(): GameData = data.first()

    suspend fun saveQuizBest(categoryId: String, score: Int) {
        if (categoryId.isBlank()) return
        update { d ->
            val previous = d.quizBest[categoryId] ?: -1
            if (score > previous) d.copy(quizBest = d.quizBest + (categoryId to score)) else d
        }
    }

    suspend fun savePairsBest(level: Int, matchedPairs: Int) {
        if (level <= 0) return
        val key = level.toString()
        update { d ->
            val previous = d.pairsBest[key] ?: -1
            if (matchedPairs > previous) d.copy(pairsBest = d.pairsBest + (key to matchedPairs)) else d
        }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        update { d -> d.copy(settings = d.settings.copy(soundEnabled = enabled)) }
    }

    suspend fun toggleSound() {
        update { d -> d.copy(settings = d.settings.copy(soundEnabled = !d.settings.soundEnabled)) }
    }

    suspend fun resetAll() {
        try {
            dataStore.edit { prefs -> prefs[dataKey] = GameSerializer.encode(GameData()) }
        } catch (e: IOException) {
            // Storage unavailable: nothing to reset that could be read back anyway.
        }
    }

    private suspend fun update(transform: (GameData) -> GameData) {
        try {
            dataStore.edit { prefs ->
                val current = GameSerializer.decode(prefs[dataKey])
                val updated = transform(current)
                if (updated != current) prefs[dataKey] = GameSerializer.encode(updated)
            }
        } catch (e: IOException) {
            // Disk full / unavailable: keep playing, progress for this action is not persisted.
        }
    }

    companion object {
        const val KEY_GAME_DATA = "game_data_json"
    }
}
