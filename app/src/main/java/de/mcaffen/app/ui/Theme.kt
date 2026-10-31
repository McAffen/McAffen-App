package de.mcaffen.app.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object ThemeOptions {
    const val DARK_SYSTEM = "SYSTEM"
    const val DARK_LIGHT = "LIGHT"
    const val DARK_DARK = "DARK"

    const val COLOR_GREEN = "GREEN"
    const val COLOR_BLUE = "BLUE"
    const val COLOR_PURPLE = "PURPLE"

    val darkModes = listOf(
        DARK_SYSTEM to "System",
        DARK_LIGHT to "Hell",
        DARK_DARK to "Dunkel",
    )

    val colors = listOf(
        COLOR_GREEN to "McAffen Grün",
        COLOR_BLUE to "Blau",
        COLOR_PURPLE to "Violett",
    )
}

private val McAffenLightColorScheme = lightColorScheme(
    primary = Color(0xFF4C8C3F),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD7F0CE),
    onPrimaryContainer = Color(0xFF0C2B08),
    secondary = Color(0xFF3F8C8C),
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
    primary = Color(0xFF86C774),
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

private val BlueLightColorScheme = McAffenLightColorScheme.copy(
    primary = Color(0xFF3A6EA5),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD3E4FF),
    onPrimaryContainer = Color(0xFF001A33),
    secondary = Color(0xFF3A8CA5),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFC7E8F0),
    onSecondaryContainer = Color(0xFF002028),
    background = Color(0xFFF8FBFF),
    onBackground = Color(0xFF181C20),
    surface = Color(0xFFF8FBFF),
    onSurface = Color(0xFF181C20),
    surfaceVariant = Color(0xFFDFE3EB),
    onSurfaceVariant = Color(0xFF43474E),
)

private val BlueDarkColorScheme = McAffenDarkColorScheme.copy(
    primary = Color(0xFFA2C9FF),
    onPrimary = Color(0xFF003059),
    primaryContainer = Color(0xFF27496F),
    onPrimaryContainer = Color(0xFFD3E4FF),
    secondary = Color(0xFF93D0E2),
    onSecondary = Color(0xFF003038),
    secondaryContainer = Color(0xFF1F4E58),
    onSecondaryContainer = Color(0xFFC7E8F0),
    background = Color(0xFF101418),
    onBackground = Color(0xFFE2E5EA),
    surface = Color(0xFF101418),
    onSurface = Color(0xFFE2E5EA),
    surfaceVariant = Color(0xFF43474E),
    onSurfaceVariant = Color(0xFFC3C7CF),
)

private val PurpleLightColorScheme = McAffenLightColorScheme.copy(
    primary = Color(0xFF6750A4),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEADDFF),
    onPrimaryContainer = Color(0xFF21005D),
    secondary = Color(0xFF9A50A4),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEFC7F0),
    onSecondaryContainer = Color(0xFF2B0020),
    background = Color(0xFFFCF9FE),
    onBackground = Color(0xFF1C1B1E),
    surface = Color(0xFFFCF9FE),
    onSurface = Color(0xFF1C1B1E),
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454E),
)

private val PurpleDarkColorScheme = McAffenDarkColorScheme.copy(
    primary = Color(0xFFD0BCFF),
    onPrimary = Color(0xFF381E72),
    primaryContainer = Color(0xFF4F378B),
    onPrimaryContainer = Color(0xFFEADDFF),
    secondary = Color(0xFFD8A8DC),
    onSecondary = Color(0xFF33163B),
    secondaryContainer = Color(0xFF493253),
    onSecondaryContainer = Color(0xFFEFC7F0),
    background = Color(0xFF141218),
    onBackground = Color(0xFFE6E0E9),
    surface = Color(0xFF141218),
    onSurface = Color(0xFFE6E0E9),
    surfaceVariant = Color(0xFF49454E),
    onSurfaceVariant = Color(0xFFCBC4CF),
)

@Composable
fun McAffenTheme(
    color: String = ThemeOptions.COLOR_GREEN,
    darkMode: String = ThemeOptions.DARK_SYSTEM,
    content: @Composable () -> Unit,
) {
    val dark = when (darkMode) {
        ThemeOptions.DARK_LIGHT -> false
        ThemeOptions.DARK_DARK -> true
        else -> isSystemInDarkTheme()
    }
    val scheme: ColorScheme = when (color) {
        ThemeOptions.COLOR_BLUE -> if (dark) BlueDarkColorScheme else BlueLightColorScheme
        ThemeOptions.COLOR_PURPLE -> if (dark) PurpleDarkColorScheme else PurpleLightColorScheme
        else -> if (dark) McAffenDarkColorScheme else McAffenLightColorScheme
    }
    MaterialTheme(colorScheme = scheme, content = content)
}
