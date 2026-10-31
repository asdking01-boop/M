package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val KrasDarkColorScheme = darkColorScheme(
    primary = KrasRed,
    onPrimary = Color.White,
    primaryContainer = KrasDarkRed,
    onPrimaryContainer = Color.White,
    secondary = KrasRedBright,
    onSecondary = Color.White,
    background = KrasBlack,
    onBackground = TextPrimary,
    surface = KrasDarkCharcoal,
    onSurface = TextPrimary,
    surfaceVariant = KrasCardBg,
    onSurfaceVariant = TextSecondary,
    outline = GlassWhiteStroke
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    // KRAS TV is always a luxury cinematic dark theme with Liquid Glass
    MaterialTheme(
        colorScheme = KrasDarkColorScheme,
        typography = Typography,
        content = content
    )
}
