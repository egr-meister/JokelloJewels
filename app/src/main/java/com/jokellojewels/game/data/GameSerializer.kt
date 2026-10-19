package com.jokellojewels.game.data

import com.jokellojewels.game.logic.GameLogic
import kotlinx.serialization.json.Json

/**
 * Context-free JSON encode/decode for [GameData]. Never throws on read:
 * null, empty, blank, corrupted or wrongly-typed JSON all decode to defaults.
 */
object GameSerializer {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
    }

    fun decode(raw: String?): GameData {
        if (raw.isNullOrBlank()) return GameData()
        return try {
            sanitize(json.decodeFromString(GameData.serializer(), raw))
        } catch (e: Exception) {
            GameData()
        }
    }

    fun encode(data: GameData): String = json.encodeToString(GameData.serializer(), sanitize(data))

    /** Drops blank keys and clamps values to legal ranges so bad data can never reach the UI. */
    fun sanitize(data: GameData): GameData = data.copy(
        quizBest = data.quizBest
            .filterKeys { it.isNotBlank() }
            .mapValues { (_, v) -> v.coerceIn(0, GameLogic.QUIZ_QUESTIONS_PER_CATEGORY) },
        pairsBest = data.pairsBest
            .filterKeys { it.isNotBlank() }
            .mapValues { (_, v) -> v.coerceIn(0, GameLogic.ITEM_COUNT) },
    )
}
