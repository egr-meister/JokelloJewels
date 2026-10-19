package com.jokellojewels.game

import com.jokellojewels.game.logic.GameLogic
import com.jokellojewels.game.logic.QuizContent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class GameLogicTest {

    // ---- Level table ----

    @Test
    fun levelTableMatchesSpec() {
        val expected = listOf(
            Triple(2, 2, 60), Triple(3, 2, 70), Triple(4, 2, 70),
            Triple(6, 3, 80), Triple(6, 3, 80), Triple(8, 4, 100),
            Triple(8, 4, 100), Triple(10, 4, 110), Triple(12, 4, 120),
        )
        assertEquals(9, GameLogic.levelCount)
        GameLogic.levels.forEachIndexed { i, cfg ->
            assertEquals(i + 1, cfg.level)
            assertEquals(expected[i], Triple(cfg.pairs, cfg.columns, cfg.seconds))
        }
    }

    @Test
    fun unknownLevelHasNoConfig() {
        assertNull(GameLogic.levelConfig(0))
        assertNull(GameLogic.levelConfig(10))
        assertNull(GameLogic.levelConfig(-3))
    }

    // ---- Deck building ----

    @Test
    fun deckHasCorrectPairCountsForEveryLevel() {
        GameLogic.levels.forEach { cfg ->
            repeat(20) { seed ->
                val deck = GameLogic.buildDeck(cfg.pairs, Random(seed))
                assertEquals("cards on level ${cfg.level}", cfg.pairs * 2, deck.size)
                val counts = deck.groupingBy { it.itemIndex }.eachCount()
                assertEquals("distinct items on level ${cfg.level}", cfg.pairs, counts.size)
                assertTrue("every item exactly twice", counts.values.all { it == 2 })
                assertTrue("items in 0..11", deck.all { it.itemIndex in 0 until GameLogic.ITEM_COUNT })
                assertEquals("unique card ids", deck.size, deck.map { it.id }.toSet().size)
            }
        }
    }

    @Test
    fun deckClampsPairCount() {
        assertEquals(2, GameLogic.buildDeck(0).size)
        assertEquals(2, GameLogic.buildDeck(-5).size)
        assertEquals(24, GameLogic.buildDeck(99).size)
    }

    @Test
    fun deckIsShuffled() {
        val orders = (0 until 10).map { seed -> GameLogic.buildDeck(12, Random(seed)).map { it.itemIndex } }
        assertTrue("different seeds should give different orders", orders.toSet().size > 1)
    }

    // ---- Unlock / completion ----

    @Test
    fun levelOneAlwaysUnlocked() {
        assertTrue(GameLogic.isLevelUnlocked(1, emptyMap()))
        assertFalse(GameLogic.isLevelUnlocked(2, emptyMap()))
    }

    @Test
    fun levelUnlocksOnlyAfterPreviousCleared() {
        // Level 1 has 2 pairs: 1 matched is not enough.
        assertFalse(GameLogic.isLevelUnlocked(2, mapOf("1" to 1)))
        assertTrue(GameLogic.isLevelUnlocked(2, mapOf("1" to 2)))
        assertFalse(GameLogic.isLevelUnlocked(3, mapOf("1" to 2)))
        assertTrue(GameLogic.isLevelUnlocked(3, mapOf("1" to 2, "2" to 3)))
    }

    @Test
    fun invalidLevelsAreNeverUnlockedOrCleared() {
        val all = GameLogic.levels.associate { it.level.toString() to it.pairs }
        assertFalse(GameLogic.isLevelUnlocked(0, all))
        assertFalse(GameLogic.isLevelUnlocked(10, all))
        assertFalse(GameLogic.isLevelCleared(10, mapOf("10" to 99)))
    }

    @Test
    fun clearedRequiresAllPairs() {
        assertFalse(GameLogic.isLevelCleared(9, mapOf("9" to 11)))
        assertTrue(GameLogic.isLevelCleared(9, mapOf("9" to 12)))
    }

    @Test
    fun completedLevelCounting() {
        assertEquals(0, GameLogic.completedLevelCount(emptyMap()))
        assertEquals(1, GameLogic.completedLevelCount(mapOf("1" to 2, "2" to 1)))
        assertEquals(2, GameLogic.completedLevelCount(mapOf("1" to 2, "2" to 3, "junk" to 5, "42" to 9)))
        val all = GameLogic.levels.associate { it.level.toString() to it.pairs }
        assertEquals(9, GameLogic.completedLevelCount(all))
    }

    @Test
    fun nextLevel() {
        assertEquals(2, GameLogic.nextLevel(1))
        assertNull(GameLogic.nextLevel(9))
    }

    @Test
    fun formatTime() {
        assertEquals("2:00", GameLogic.formatTime(120))
        assertEquals("0:05", GameLogic.formatTime(5))
        assertEquals("0:00", GameLogic.formatTime(-1))
    }

    // ---- Quiz content ----

    @Test
    fun quizHasThreeCategoriesOfTenValidQuestions() {
        assertEquals(3, QuizContent.categories.size)
        assertEquals(listOf("easy", "medium", "hard"), QuizContent.categories.map { it.id })
        assertEquals(3, QuizContent.categories.map { it.id }.toSet().size)
        QuizContent.categories.forEach { c ->
            assertEquals(GameLogic.QUIZ_QUESTIONS_PER_CATEGORY, c.questions.size)
            c.questions.forEach { q ->
                assertEquals("4 options: ${q.text}", 4, q.options.size)
                assertTrue(q.correctIndex in q.options.indices)
                assertEquals("options unique: ${q.text}", q.options.size, q.options.toSet().size)
            }
        }
        assertNull(QuizContent.category(""))
        assertNull(QuizContent.category(null))
        assertNull(QuizContent.category("nope"))
    }
}
