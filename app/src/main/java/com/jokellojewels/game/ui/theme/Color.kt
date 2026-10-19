package com.jokellojewels.game.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Visual concept: "Royal Jester Stage" — velvet curtains, royal purple plates with gold rims,
 * ruby / emerald / amethyst jewels, taken straight from the art pack.
 *
 * HEX       name          usage
 * #1A0620   Night         window/app background, scrims, darkest panel stop
 * #2C0D3A   Plum          surfaces, panel bottom stop, splash & icon background tone
 * #4A1786   Velvet        royal purple: panel top stop, card backs, HUD pills
 * #C8102E   Ruby          accents, low-timer pill, wrong-answer partner color
 * #19A35B   Emerald       matched-card border, progress bar, highlights
 * #F6C453   Gold          titles, borders, rims, stars
 * #FFF3DC   Cream         body text on dark backgrounds
 * #D2BFEA   Lavender      secondary text
 * #1E8E4E   CorrectGreen  correct quiz answer (white text ≥ 4.5:1) + ✓ mark
 * #C62839   WrongRed      wrong quiz answer (white text ≥ 4.5:1) + ✗ mark
 */
object JJColors {
    val Night = Color(0xFF1A0620)
    val Plum = Color(0xFF2C0D3A)
    val Velvet = Color(0xFF4A1786)
    val VelvetDeep = Color(0xFF22073F)
    val Ruby = Color(0xFFC8102E)
    val Emerald = Color(0xFF19A35B)
    val Gold = Color(0xFFF6C453)
    val GoldDeep = Color(0xFFB0761A)
    val Cream = Color(0xFFFFF3DC)
    val Lavender = Color(0xFFD2BFEA)
    val CorrectGreen = Color(0xFF1E8E4E)
    val WrongRed = Color(0xFFC62839)
    val White = Color(0xFFFFFFFF)
}
