package de.mcaffen.app.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object ThemeOptions {
    const val DARK_SYSTEM = "SYSTEM"
    const val DARK_LIGHT = "LIGHT"
    const val DARK_DARK = "DARK"

    const val COLOR_GREEN = "GREEN"
    const val COLOR_BLUE = "BLUE"
    const val COLOR_PURPLE = "PURPLE"
    const val COLOR_ORANGE = "ORANGE"
    const val COLOR_RED = "RED"
    const val COLOR_TEAL = "TEAL"
    const val COLOR_PINK = "PINK"
    const val COLOR_CYAN = "CYAN"
    const val COLOR_AMBER = "AMBER"
    const val COLOR_INDIGO = "INDIGO"

    val darkModes = listOf(
        DARK_SYSTEM to "System",
        DARK_LIGHT to "Hell",
        DARK_DARK to "Dunkel",
    )

    val colors = listOf(
        COLOR_GREEN to "McAffen Grün",
        COLOR_BLUE to "Blau",
        COLOR_PURPLE to "Violett",
        COLOR_ORANGE to "Orange",
        COLOR_RED to "Rot",
        COLOR_TEAL to "Türkis",
        COLOR_PINK to "Pink",
        COLOR_CYAN to "Cyan",
        COLOR_AMBER to "Bernstein",
        COLOR_INDIGO to "Indigo",
    )

    /** Hell/Dunkel-Auswahl mit übersetzten Beschriftungen (Einstellungen). */
    @Composable
    fun darkModes(): List<Pair<String, String>> {
        val english = LocalStrings.current.language == AppLanguage.ENGLISH
        return listOf(
            DARK_SYSTEM to "System",
            DARK_LIGHT to if (english) "Light" else "Hell",
            DARK_DARK to if (english) "Dark" else "Dunkel",
        )
    }

    /** Farbwelten mit übersetzten Namen; „McAffen“ bleibt Markenname. */
    @Composable
    fun colors(): List<Pair<String, String>> {
        val english = LocalStrings.current.language == AppLanguage.ENGLISH
        return listOf(
            COLOR_GREEN to if (english) "McAffen Green" else "McAffen Grün",
            COLOR_BLUE to if (english) "Blue" else "Blau",
            COLOR_PURPLE to if (english) "Purple" else "Violett",
            COLOR_ORANGE to "Orange",
            COLOR_RED to if (english) "Red" else "Rot",
            COLOR_TEAL to if (english) "Teal" else "Türkis",
            COLOR_PINK to "Pink",
            COLOR_CYAN to "Cyan",
            COLOR_AMBER to if (english) "Amber" else "Bernstein",
            COLOR_INDIGO to "Indigo",
        )
    }

    /** Akzentfarbe einer Farbwelt – für die Vorschau-Punkte in den Einstellungen. */
    fun previewColor(color: String): Color = when (color) {
        COLOR_BLUE -> Color(0xFF2F6FB0)
        COLOR_PURPLE -> Color(0xFF6750A4)
        COLOR_ORANGE -> Color(0xFFC96A12)
        COLOR_RED -> Color(0xFFB3312A)
        COLOR_TEAL -> Color(0xFF00796B)
        COLOR_PINK -> Color(0xFFB3306E)
        COLOR_CYAN -> Color(0xFF00707F)
        COLOR_AMBER -> Color(0xFF9A6B00)
        COLOR_INDIGO -> Color(0xFF3A4BA8)
        else -> Color(0xFF3F7F32)
    }
}

/**
 * Markenfarben, die das Material-Farbschema nicht abdeckt:
 * Verläufe, Podium-Medaillen (Gold/Silber/Bronze) und Trendfarben.
 */
data class McAffenColors(
    val brandGradient: List<Color>,
    val heroGradient: List<Color>,
    val gold: Color = Color(0xFFFFC14D),
    val silver: Color = Color(0xFFCBD5E1),
    val bronze: Color = Color(0xFFE39A5C),
    val positive: Color,
    val negative: Color,
    val glow: Color,
)

private val LocalMcAffenColors = staticCompositionLocalOf {
    McAffenColors(
        brandGradient = listOf(Color(0xFF3F7F32), Color(0xFF2E7D32)),
        heroGradient = listOf(Color(0xFFD7F0CE), Color(0xFFFBFDF8)),
        positive = Color(0xFF1F8A44),
        negative = Color(0xFFC62828),
        glow = Color(0x2E3F7F32),
    )
}

object McAffenBrand {
    val colors: McAffenColors
        @Composable get() = LocalMcAffenColors.current
}

private val McAffenLightColorScheme = lightColorScheme(
    primary = Color(0xFF3F7F32),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD7F0CE),
    onPrimaryContainer = Color(0xFF0C2B08),
    secondary = Color(0xFF3F8C8C),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFC7EBEB),
    onSecondaryContainer = Color(0xFF002020),
    tertiary = Color(0xFFC98A16),
    onTertiary = Color.White,
    background = Color(0xFFF7FAF3),
    onBackground = Color(0xFF171B15),
    surface = Color(0xFFFBFDF8),
    onSurface = Color(0xFF171B15),
    surfaceVariant = Color(0xFFE4E9DE),
    onSurfaceVariant = Color(0xFF43483F),
    surfaceContainer = Color(0xFFF0F4EC),
    surfaceContainerHigh = Color(0xFFEAF0E4),
    surfaceContainerHighest = Color(0xFFE4E9DE),
    surfaceContainerLow = Color(0xFFF5F9F1),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    outline = Color(0xFF8A9184),
    outlineVariant = Color(0xFFC9D1C3),
    error = Color(0xFFB3261E),
    onError = Color.White,
)

private val McAffenDarkColorScheme = darkColorScheme(
    primary = Color(0xFF8FD47C),
    onPrimary = Color(0xFF0C2B08),
    primaryContainer = Color(0xFF2E6329),
    onPrimaryContainer = Color(0xFFD7F0CE),
    secondary = Color(0xFF87D0D0),
    onSecondary = Color(0xFF003737),
    secondaryContainer = Color(0xFF205050),
    onSecondaryContainer = Color(0xFFC7EBEB),
    tertiary = Color(0xFFFFCB6B),
    onTertiary = Color(0xFF3D2A00),
    background = Color(0xFF0E120D),
    onBackground = Color(0xFFE3E6DE),
    surface = Color(0xFF131712),
    onSurface = Color(0xFFE3E6DE),
    surfaceVariant = Color(0xFF3F453C),
    onSurfaceVariant = Color(0xFFC3C8BD),
    surfaceContainer = Color(0xFF1A1F19),
    surfaceContainerHigh = Color(0xFF212620),
    surfaceContainerHighest = Color(0xFF2A3028),
    surfaceContainerLow = Color(0xFF161B15),
    surfaceContainerLowest = Color(0xFF0B0E0A),
    outline = Color(0xFF8D9487),
    outlineVariant = Color(0xFF3F453C),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
)

private val BlueLight = McAffenLightColorScheme.copy(
    primary = Color(0xFF2F6FB0),
    primaryContainer = Color(0xFFD3E4FF),
    onPrimaryContainer = Color(0xFF001A33),
    secondary = Color(0xFF3A8CA5),
    secondaryContainer = Color(0xFFC7E8F0),
    onSecondaryContainer = Color(0xFF002028),
    background = Color(0xFFF6FAFF),
    surface = Color(0xFFFBFDFF),
    surfaceContainer = Color(0xFFEDF3FB),
    surfaceContainerHigh = Color(0xFFE7EEF8),
    surfaceContainerHighest = Color(0xFFDFE7F2),
    surfaceContainerLow = Color(0xFFF2F7FD),
    surfaceVariant = Color(0xFFDFE3EB),
    onSurfaceVariant = Color(0xFF43474E),
)

private val BlueDark = McAffenDarkColorScheme.copy(
    primary = Color(0xFF9CC7FF),
    primaryContainer = Color(0xFF27496F),
    onPrimary = Color(0xFF00315C),
    secondary = Color(0xFF93D0E2),
    secondaryContainer = Color(0xFF1F4E58),
    background = Color(0xFF0C1116),
    surface = Color(0xFF11161C),
    surfaceContainer = Color(0xFF171D24),
    surfaceContainerHigh = Color(0xFF1E242C),
    surfaceContainerHighest = Color(0xFF262D36),
    surfaceContainerLow = Color(0xFF141A20),
    surfaceVariant = Color(0xFF3A4149),
    onSurfaceVariant = Color(0xFFC3C7CF),
)

private val PurpleLight = McAffenLightColorScheme.copy(
    primary = Color(0xFF6750A4),
    primaryContainer = Color(0xFFEADDFF),
    onPrimaryContainer = Color(0xFF21005D),
    secondary = Color(0xFF9A50A4),
    secondaryContainer = Color(0xFFEFC7F0),
    onSecondaryContainer = Color(0xFF2B0020),
    background = Color(0xFFFAF8FF),
    surface = Color(0xFFFDFAFF),
    surfaceContainer = Color(0xFFF4EFFB),
    surfaceContainerHigh = Color(0xFFEFE8F8),
    surfaceContainerHighest = Color(0xFFE7E0F1),
    surfaceContainerLow = Color(0xFFF8F3FD),
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454E),
)

private val PurpleDark = McAffenDarkColorScheme.copy(
    primary = Color(0xFFD0BCFF),
    primaryContainer = Color(0xFF4F378B),
    onPrimary = Color(0xFF381E72),
    secondary = Color(0xFFD8A8DC),
    secondaryContainer = Color(0xFF493253),
    background = Color(0xFF0F0D13),
    surface = Color(0xFF141218),
    surfaceContainer = Color(0xFF1B181F),
    surfaceContainerHigh = Color(0xFF221E27),
    surfaceContainerHighest = Color(0xFF2B2731),
    surfaceContainerLow = Color(0xFF18151B),
    surfaceVariant = Color(0xFF443F4D),
    onSurfaceVariant = Color(0xFFCBC4CF),
)

private val OrangeLight = McAffenLightColorScheme.copy(
    primary = Color(0xFFC96A12),
    primaryContainer = Color(0xFFFFDDB8),
    onPrimaryContainer = Color(0xFF2B1700),
    secondary = Color(0xFFB76E1F),
    secondaryContainer = Color(0xFFFFE0C2),
    onSecondaryContainer = Color(0xFF2A1600),
    background = Color(0xFFFFF9F2),
    surface = Color(0xFFFFFBF6),
    surfaceContainer = Color(0xFFFCF1E3),
    surfaceContainerHigh = Color(0xFFF8EADC),
    surfaceContainerHighest = Color(0xFFF1E2D2),
    surfaceContainerLow = Color(0xFFFEF6ED),
    surfaceVariant = Color(0xFFEDE1D5),
    onSurfaceVariant = Color(0xFF50453B),
)

private val OrangeDark = McAffenDarkColorScheme.copy(
    primary = Color(0xFFFFB77C),
    primaryContainer = Color(0xFF6A3C00),
    onPrimary = Color(0xFF4A2800),
    secondary = Color(0xFFFFC98A),
    secondaryContainer = Color(0xFF6A3C00),
    background = Color(0xFF130F0B),
    surface = Color(0xFF1A1410),
    surfaceContainer = Color(0xFF211A14),
    surfaceContainerHigh = Color(0xFF29211A),
    surfaceContainerHighest = Color(0xFF322920),
    surfaceContainerLow = Color(0xFF1D1712),
    surfaceVariant = Color(0xFF4C423A),
    onSurfaceVariant = Color(0xFFD4C4B5),
)

private val RedLight = McAffenLightColorScheme.copy(
    primary = Color(0xFFB3312A),
    primaryContainer = Color(0xFFFFDAD6),
    onPrimaryContainer = Color(0xFF410002),
    secondary = Color(0xFF9C27B0),
    secondaryContainer = Color(0xFFF3D9FF),
    onSecondaryContainer = Color(0xFF32004A),
    background = Color(0xFFFFF8F6),
    surface = Color(0xFFFFFBF9),
    surfaceContainer = Color(0xFFFBEFEC),
    surfaceContainerHigh = Color(0xFFF6E8E5),
    surfaceContainerHighest = Color(0xFFEFE0DC),
    surfaceContainerLow = Color(0xFFFDF3F0),
    surfaceVariant = Color(0xFFEDE0DC),
    onSurfaceVariant = Color(0xFF504441),
)

private val RedDark = McAffenDarkColorScheme.copy(
    primary = Color(0xFFFFB4AB),
    primaryContainer = Color(0xFF93000A),
    onPrimary = Color(0xFF690005),
    secondary = Color(0xFFE2B4FF),
    secondaryContainer = Color(0xFF650097),
    background = Color(0xFF120D0C),
    surface = Color(0xFF1A1110),
    surfaceContainer = Color(0xFF211715),
    surfaceContainerHigh = Color(0xFF291E1C),
    surfaceContainerHighest = Color(0xFF332725),
    surfaceContainerLow = Color(0xFF1D1413),
    surfaceVariant = Color(0xFF4B3F3D),
    onSurfaceVariant = Color(0xFFD4C3BF),
)

private val TealLight = McAffenLightColorScheme.copy(
    primary = Color(0xFF00796B),
    primaryContainer = Color(0xFFB2DFDB),
    onPrimaryContainer = Color(0xFF00201D),
    secondary = Color(0xFF00ACC1),
    secondaryContainer = Color(0xFFB2EBF2),
    onSecondaryContainer = Color(0xFF00363A),
    background = Color(0xFFF3FDFB),
    surface = Color(0xFFF8FFFD),
    surfaceContainer = Color(0xFFE9F5F2),
    surfaceContainerHigh = Color(0xFFE2EFEC),
    surfaceContainerHighest = Color(0xFFDAE8E5),
    surfaceContainerLow = Color(0xFFEFFAF7),
    surfaceVariant = Color(0xFFDBE5E2),
    onSurfaceVariant = Color(0xFF3F4946),
)

private val TealDark = McAffenDarkColorScheme.copy(
    primary = Color(0xFF80CBC4),
    primaryContainer = Color(0xFF005048),
    onPrimary = Color(0xFF003733),
    secondary = Color(0xFF80DEEA),
    secondaryContainer = Color(0xFF004F55),
    background = Color(0xFF0B1110),
    surface = Color(0xFF101413),
    surfaceContainer = Color(0xFF161B1A),
    surfaceContainerHigh = Color(0xFF1D2322),
    surfaceContainerHighest = Color(0xFF252C2A),
    surfaceContainerLow = Color(0xFF131817),
    surfaceVariant = Color(0xFF3B4543),
    onSurfaceVariant = Color(0xFFBFC9C5),
)

/** Eigene Typografie: kräftige, kompakte Überschriften für einen App-Look. */
private val McAffenTypography = Typography().let { base ->
    base.copy(
        displaySmall = base.displaySmall.copy(fontWeight = FontWeight.Black, letterSpacing = (-0.5).sp),
        headlineLarge = base.headlineLarge.copy(fontWeight = FontWeight.Black, letterSpacing = (-0.6).sp),
        headlineMedium = base.headlineMedium.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.4).sp),
        headlineSmall = base.headlineSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.3).sp),
        titleLarge = base.titleLarge.copy(fontWeight = FontWeight.Bold),
        titleMedium = base.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        titleSmall = base.titleSmall.copy(fontWeight = FontWeight.SemiBold),
        labelLarge = base.labelLarge.copy(fontWeight = FontWeight.SemiBold),
        labelMedium = base.labelMedium.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 0.3.sp),
        labelSmall = base.labelSmall.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 0.4.sp),
    )
}

/** Ziffern mit fester Breite für Ranglisten, Punkte und Countdowns. */
val NumericStyle: TextStyle = TextStyle(
    fontFeatureSettings = "tnum",
    fontWeight = FontWeight.Bold,
)

private val McAffenShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

/** Mischt zwei Farben linear (t = 0 → a, t = 1 → b). */
fun blend(a: Color, b: Color, t: Float): Color {
    val f = t.coerceIn(0f, 1f)
    return Color(
        red = a.red * (1 - f) + b.red * f,
        green = a.green * (1 - f) + b.green * f,
        blue = a.blue * (1 - f) + b.blue * f,
        alpha = a.alpha * (1 - f) + b.alpha * f,
    )
}

/**
 * Die zusätzlichen Farbwelten entstehen aus einer kleinen Tabelle statt aus
 * handgeschriebenen Farbschemata: Der Akzent bestimmt das Farbschema, der
 * Hintergrund wird leicht in seine Richtung getönt. So bleiben Kontraste und
 * Flächenverhältnisse bei jeder Welt gleich – nur der Charakter ändert sich.
 */
private data class WorldSpec(
    val lightPrimary: Color,
    val lightSecondary: Color,
    val darkPrimary: Color,
    val darkSecondary: Color,
)

private val WORLD_SPECS: Map<String, WorldSpec> = mapOf(
    ThemeOptions.COLOR_PINK to WorldSpec(
        Color(0xFFB3306E), Color(0xFF9A4E7E),
        Color(0xFFFFAFD2), Color(0xFFE5B3D8),
    ),
    ThemeOptions.COLOR_CYAN to WorldSpec(
        Color(0xFF00707F), Color(0xFF2C6E9B),
        Color(0xFF6FD8E8), Color(0xFF9FCBF5),
    ),
    ThemeOptions.COLOR_AMBER to WorldSpec(
        Color(0xFF8A6100), Color(0xFF9A5B2C),
        Color(0xFFFFC85C), Color(0xFFF2B98A),
    ),
    ThemeOptions.COLOR_INDIGO to WorldSpec(
        Color(0xFF3A4BA8), Color(0xFF6A4FA0),
        Color(0xFFB7C2FF), Color(0xFFD3BDFF),
    ),
)

/** Baut aus einer [WorldSpec] ein vollständiges helles/dunkles Farbschema. */
private fun worldScheme(spec: WorldSpec, dark: Boolean): ColorScheme {
    val base = if (dark) McAffenDarkColorScheme else McAffenLightColorScheme
    val primary = if (dark) spec.darkPrimary else spec.lightPrimary
    val secondary = if (dark) spec.darkSecondary else spec.lightSecondary
    val container = if (dark) {
        blend(base.surface, primary, 0.30f)
    } else {
        blend(Color.White, primary, 0.18f)
    }
    val background = if (dark) {
        blend(Color(0xFF0E120D), primary, 0.06f)
    } else {
        blend(Color(0xFFF7FAF3), primary, 0.04f)
    }
    return base.copy(
        primary = primary,
        onPrimary = if (dark) blend(Color.Black, primary, 0.12f) else Color.White,
        primaryContainer = container,
        onPrimaryContainer = if (dark) blend(Color.White, primary, 0.15f) else blend(Color.Black, primary, 0.35f),
        secondary = secondary,
        secondaryContainer = if (dark) blend(base.surfaceContainerHigh, secondary, 0.45f) else blend(Color.White, secondary, 0.24f),
        onSecondaryContainer = if (dark) blend(Color.White, secondary, 0.20f) else blend(Color.Black, secondary, 0.40f),
        background = background,
        surface = if (dark) blend(background, Color.White, 0.02f) else blend(background, Color.White, 0.55f),
        surfaceContainerLowest = if (dark) blend(background, Color.Black, 0.35f) else Color.White,
        surfaceContainerLow = if (dark) blend(background, Color.White, 0.03f) else blend(background, Color.White, 0.35f),
        surfaceContainer = if (dark) blend(background, Color.White, 0.06f) else blend(background, Color.White, 0.20f),
        surfaceContainerHigh = if (dark) blend(background, Color.White, 0.09f) else blend(background, Color.White, 0.10f),
        surfaceContainerHighest = if (dark) blend(background, Color.White, 0.12f) else blend(background, Color.White, 0.02f),
    )
}

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
        ThemeOptions.COLOR_BLUE -> if (dark) BlueDark else BlueLight
        ThemeOptions.COLOR_PURPLE -> if (dark) PurpleDark else PurpleLight
        ThemeOptions.COLOR_ORANGE -> if (dark) OrangeDark else OrangeLight
        ThemeOptions.COLOR_RED -> if (dark) RedDark else RedLight
        ThemeOptions.COLOR_TEAL -> if (dark) TealDark else TealLight
        else -> WORLD_SPECS[color]?.let { worldScheme(it, dark) }
            ?: if (dark) McAffenDarkColorScheme else McAffenLightColorScheme
    }
    val brand = scheme.primary
    val extra = McAffenColors(
        brandGradient = listOf(brand, blend(brand, scheme.secondary, 0.55f)),
        heroGradient = if (dark) {
            listOf(blend(scheme.primaryContainer, Color.Black, 0.30f), scheme.surfaceContainerLow)
        } else {
            listOf(blend(scheme.primaryContainer, Color.White, 0.20f), scheme.surface)
        },
        positive = if (dark) Color(0xFF6FD68C) else Color(0xFF1F8A44),
        negative = if (dark) Color(0xFFFF9C99) else Color(0xFFC62828),
        glow = brand.copy(alpha = if (dark) 0.35f else 0.18f),
    )
    CompositionLocalProvider(LocalMcAffenColors provides extra) {
        MaterialTheme(
            colorScheme = scheme,
            typography = McAffenTypography,
            shapes = McAffenShapes,
            content = content,
        )
    }
}
