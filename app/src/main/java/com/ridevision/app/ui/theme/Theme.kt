package com.ridevision.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CyanAccent,
    onPrimary = Color.Black,
    primaryContainer = CockpitSurfaceVariant,
    onPrimaryContainer = CyanAccent,
    secondary = BlueDrive,
    onSecondary = Color.Black,
    secondaryContainer = CockpitSurfaceVariant,
    onSecondaryContainer = Color.White,
    tertiary = EmeraldSafe,
    background = CockpitBackground,
    onBackground = TextPrimary,
    surface = CockpitSurface,
    onSurface = TextPrimary,
    surfaceVariant = CockpitSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    error = SevereRed,
    onError = Color.White
)

@Composable
fun RideVisionTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
