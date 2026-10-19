package com.jokellojewels.game.data

import kotlinx.serialization.Serializable

/**
 * Everything the game persists, stored as ONE JSON string in DataStore Preferences.
 * Every field has a default so older / partial / future JSON always decodes.
 *
 * quizBest:  categoryId -> best score (0..10)
 * pairsBest: level number as string ("1".."9") -> best matched pairs on that level
 */
@Serializable
data class GameData(
    val quizBest: Map<String, Int> = emptyMap(),
    val pairsBest: Map<String, Int> = emptyMap(),
    val settings: Settings = Settings(),
)

@Serializable
data class Settings(
    val soundEnabled: Boolean = true,
)
