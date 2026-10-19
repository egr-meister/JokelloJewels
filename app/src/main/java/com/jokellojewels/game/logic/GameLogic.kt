package com.jokellojewels.game.logic

import kotlin.random.Random

/** One Matching Pairs level: number of pairs, grid columns and time limit. */
data class LevelConfig(
    val level: Int,
    val pairs: Int,
    val columns: Int,
    val seconds: Int,
) {
    val cardCount: Int get() = pairs * 2
    val rows: Int get() = (cardCount + columns - 1) / columns
}

/** A card in a shuffled deck. [itemIndex] is 0..11 and maps to Assets.items. */
data class DeckCard(
    val id: Int,
    val itemIndex: Int,
)

/**
 * Pure, context-free game rules. No Android types — fully unit-testable.
 */
object GameLogic {

    const val ITEM_COUNT = 12
    const val QUIZ_QUESTIONS_PER_CATEGORY = 10

    val levels: List<LevelConfig> = listOf(
        LevelConfig(level = 1, pairs = 2, columns = 2, seconds = 60),
        LevelConfig(level = 2, pairs = 3, columns = 2, seconds = 70),
        LevelConfig(level = 3, pairs = 4, columns = 2, seconds = 70),
        LevelConfig(level = 4, pairs = 6, columns = 3, seconds = 80),
        LevelConfig(level = 5, pairs = 6, columns = 3, seconds = 80),
        LevelConfig(level = 6, pairs = 8, columns = 4, seconds = 100),
        LevelConfig(level = 7, pairs = 8, columns = 4, seconds = 100),
        LevelConfig(level = 8, pairs = 10, columns = 4, seconds = 110),
        LevelConfig(level = 9, pairs = 12, columns = 4, seconds = 120),
    )

    val levelCount: Int get() = levels.size

    fun levelConfig(level: Int): LevelConfig? = levels.firstOrNull { it.level == level }

    /**
     * Builds a shuffled deck of `pairs` distinct items, each present exactly twice.
     * `pairs` is clamped to 1..[ITEM_COUNT].
     */
    fun buildDeck(pairs: Int, random: Random = Random.Default): List<DeckCard> {
        val count = pairs.coerceIn(1, ITEM_COUNT)
        val chosen = (0 until ITEM_COUNT).shuffled(random).take(count)
        return (chosen + chosen)
            .shuffled(random)
            .mapIndexed { index, item -> DeckCard(id = index, itemIndex = item) }
    }

    fun bestMatched(level: Int, pairsBest: Map<String, Int>): Int =
        (pairsBest[level.toString()] ?: 0).coerceAtLeast(0)

    /** A level is cleared once all of its pairs have been matched at least once. */
    fun isLevelCleared(level: Int, pairsBest: Map<String, Int>): Boolean {
        val config = levelConfig(level) ?: return false
        return bestMatched(level, pairsBest) >= config.pairs
    }

    /** Level 1 is always unlocked; level N unlocks when level N-1 is cleared. */
    fun isLevelUnlocked(level: Int, pairsBest: Map<String, Int>): Boolean = when {
        levelConfig(level) == null -> false
        level == 1 -> true
        else -> isLevelCleared(level - 1, pairsBest)
    }

    fun completedLevelCount(pairsBest: Map<String, Int>): Int =
        levels.count { isLevelCleared(it.level, pairsBest) }

    /** Next level number after [level], or null if it is the last one. */
    fun nextLevel(level: Int): Int? = levelConfig(level + 1)?.level

    fun isValidQuizScore(score: Int): Boolean = score in 0..QUIZ_QUESTIONS_PER_CATEGORY

    fun formatTime(seconds: Int): String {
        val s = seconds.coerceAtLeast(0)
        val minutes = s / 60
        val rest = s % 60
        return "$minutes:" + rest.toString().padStart(2, '0')
    }
}
