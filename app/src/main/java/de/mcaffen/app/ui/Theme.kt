package de.mcaffen.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Typography as ComposeTypography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val McAffenLightColorScheme = lightColorScheme(
    primary = Color(0xFF6750A4),
    secondary = Color(0xFF03DAC5),
    background = Color(0xFFFFFFFF),
    surface = Color(0xFFFFFFFF),
    error = Color(0xFFB00020),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    onError = Color.White,
)

@Composable
fun McAffenTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = McAffenLightColorScheme,
        typography = ComposeTypography,
        content = content
    )
}
