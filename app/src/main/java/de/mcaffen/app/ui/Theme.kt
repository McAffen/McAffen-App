package de.mcaffen.app.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Minecraft-inspired green palette for McAffen.
private val Green = Color(0xFF4C8C3F)
private val GreenDark = Color(0xFF86C774)
private val Accent = Color(0xFF3F8C8C)

private val McAffenLightColorScheme = lightColorScheme(
    primary = Green,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD7F0CE),
    onPrimaryContainer = Color(0xFF0C2B08),
    secondary = Accent,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFC7EBEB),
    onSecondaryContainer = Color(0xFF002020),
    background = Color(0xFFFAFDF6),
    onBackground = Color(0xFF1A1C18),
    surface = Color(0xFFFAFDF6),
    onSurface = Color(0xFF1A1C18),
    surfaceVariant = Color(0xFFE0E4DA),
    onSurfaceVariant = Color(0xFF43483F),
    error = Color(0xFFB00020),
    onError = Color.White,
)

private val McAffenDarkColorScheme = darkColorScheme(
    primary = GreenDark,
    onPrimary = Color(0xFF14461A),
    primaryContainer = Color(0xFF2E6329),
    onPrimaryContainer = Color(0xFFD7F0CE),
    secondary = Color(0xFF87D0D0),
    onSecondary = Color(0xFF003737),
    secondaryContainer = Color(0xFF205050),
    onSecondaryContainer = Color(0xFFC7EBEB),
    background = Color(0xFF121410),
    onBackground = Color(0xFFE2E3DC),
    surface = Color(0xFF121410),
    onSurface = Color(0xFFE2E3DC),
    surfaceVariant = Color(0xFF43483F),
    onSurfaceVariant = Color(0xFFC3C8BD),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
)

@Composable
fun McAffenTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) McAffenDarkColorScheme else McAffenLightColorScheme,
        content = content,
    )
}
