package com.jokellojewels.game.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val JJColorScheme = darkColorScheme(
    primary = JJColors.Ruby,
    onPrimary = JJColors.White,
    secondary = JJColors.Emerald,
    onSecondary = JJColors.Night,
    tertiary = JJColors.Gold,
    onTertiary = JJColors.Night,
    background = JJColors.Night,
    onBackground = JJColors.Cream,
    surface = JJColors.Plum,
    onSurface = JJColors.Cream,
    surfaceVariant = JJColors.Velvet,
    onSurfaceVariant = JJColors.Lavender,
    surfaceContainerHigh = JJColors.Plum,
    error = JJColors.WrongRed,
    onError = JJColors.White,
    outline = JJColors.Gold,
)

@Composable
fun JokelloTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = JJColorScheme,
        typography = JJTypography,
        content = content,
    )
}
