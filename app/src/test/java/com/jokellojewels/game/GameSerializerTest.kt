package com.jokellojewels.game

import com.jokellojewels.game.data.GameData
import com.jokellojewels.game.data.GameSerializer
import com.jokellojewels.game.data.Settings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameSerializerTest {

    private val defaults = GameData()

    @Test
    fun nullEmptyAndBlankDecodeToDefaults() {
        assertEquals(defaults, GameSerializer.decode(null))
        assertEquals(defaults, GameSerializer.decode(""))
        assertEquals(defaults, GameSerializer.decode("   \n"))
    }

    @Test
    fun corruptedJsonDecodesToDefaults() {
        listOf(
            "{",
            "not json at all",
            "{\"quizBest\": {\"gem_lore\": 5}",       // truncated
            "[1,2,3]",                                 // wrong root type
            "null",
            "42",
            "{\"quizBest\": \"oops\"}",                // wrong field type
            "{\"quizBest\": {\"gem_lore\": \"x\"}}",   // wrong value type
            "{\"settings\": {\"soundEnabled\": \"maybe\"}}",
        ).forEach { raw ->
            assertEquals("input: $raw", defaults, GameSerializer.decode(raw))
        }
    }

    @Test
    fun unknownKeysAreIgnored() {
        val raw = """{"quizBest":{"gem_lore":7},"futureFeature":{"a":1},"settings":{"soundEnabled":false,"vibration":true}}"""
        val data = GameSerializer.decode(raw)
        assertEquals(7, data.quizBest["gem_lore"])
        assertFalse(data.settings.soundEnabled)
    }

    @Test
    fun missingFieldsUseDefaults() {
        assertEquals(defaults, GameSerializer.decode("{}"))
        val onlyPairs = GameSerializer.decode("""{"pairsBest":{"1":2}}""")
        assertEquals(mapOf("1" to 2), onlyPairs.pairsBest)
        assertTrue(onlyPairs.quizBest.isEmpty())
        assertTrue(onlyPairs.settings.soundEnabled)
        assertTrue(GameSerializer.decode("""{"settings":{}}""").settings.soundEnabled)
    }

    @Test
    fun explicitNullFieldsFallBackToDefaults() {
        val data = GameSerializer.decode("""{"quizBest":null,"pairsBest":null,"settings":null}""")
        assertEquals(defaults, data)
    }

    @Test
    fun roundTrip() {
        val data = GameData(
            quizBest = mapOf("gem_lore" to 9, "royal_vault" to 4),
            pairsBest = mapOf("1" to 2, "2" to 3),
            settings = Settings(soundEnabled = false),
        )
        assertEquals(data, GameSerializer.decode(GameSerializer.encode(data)))
    }

    @Test
    fun outOfRangeValuesAreClamped() {
        val data = GameSerializer.decode("""{"quizBest":{"gem_lore":99,"x":-4," ":5},"pairsBest":{"1":-2,"9":500}}""")
        assertEquals(10, data.quizBest["gem_lore"])
        assertEquals(0, data.quizBest["x"])
        assertFalse(data.quizBest.containsKey(" "))
        assertEquals(0, data.pairsBest["1"])
        assertEquals(12, data.pairsBest["9"])
    }
}
