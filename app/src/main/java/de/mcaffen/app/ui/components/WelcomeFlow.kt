package de.mcaffen.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.mcaffen.app.R
import de.mcaffen.app.ui.AppLanguage
import de.mcaffen.app.ui.LocalStrings
import de.mcaffen.app.ui.McAffenBrand
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Eine Folie – die Texte stehen unter den Schlüsseln in [Strings]. */
private data class WelcomeSlide(
    val keyPrefix: String,
    val emoji: String,
)

private val slides = listOf(
    WelcomeSlide("welcome", "🐵"),
    WelcomeSlide("welcome.2", "🏆"),
    WelcomeSlide("welcome.3", "🗳️"),
    WelcomeSlide("welcome.4", "⚡"),
)

/**
 * Erststart-Erlebnis: animierter Willkommens-Screen mit Farbwolken, schwebenden
 * Partikeln, Sprachwahl, Slider-Screens und Namenseingabe. Ersetzt den alten statischen Dialog.
 */
@Composable
fun WelcomeFlow(
    initialName: String,
    animationsEnabled: Boolean,
    language: String,
    onLanguageChange: (String) -> Unit,
    onConfirm: (String) -> Unit,
    onSkip: () -> Unit,
) {
    // Seite 0 ist die Sprachwahl, danach folgen die Folien.
    val pager = rememberPagerState(pageCount = { slides.size + 1 })
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf(initialName) }
    val isLast = pager.currentPage == slides.size
    // Kurze Verzögerung, damit Hintergrund-Effekte nicht mit dem Activity-Start konkurrieren.
    var entered by remember { mutableStateOf(!animationsEnabled) }
    LaunchedEffect(Unit) {
        delay(120)
        entered = true
    }

    Box(Modifier.fillMaxSize()) {
        AnimatedBackdrop(enabled = animationsEnabled && entered)
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Kopfzeile: Logo + Wortmarke
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BrandLogo(Modifier.size(42.dp))
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            "McAffen",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                        )
                        Text(
                            "mcaffen.de",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                TextButton(onClick = onSkip) {
                    Text(LocalStrings.current["welcome.skip"], style = MaterialTheme.typography.labelLarge)
                }
            }

            HorizontalPager(
                state = pager,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalAlignment = Alignment.CenterVertically,
            ) { page ->
                if (page == 0) {
                    LanguageContent(
                        selected = AppLanguage.resolve(language),
                        onSelect = { onLanguageChange(it.code) },
                        animationsEnabled = animationsEnabled,
                    )
                } else {
                    SlideContent(
                        slide = slides[page - 1],
                        animationsEnabled = animationsEnabled,
                    )
                }
            }

            // Seitenanzeige
            Row(
                Modifier.padding(vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                (0 until pager.pageCount).forEach { index ->
                    val active = pager.currentPage == index
                    Box(
                        Modifier
                            .height(8.dp)
                            .width(if (active) 26.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (active) {
                                    Brush.horizontalGradient(McAffenBrand.colors.brandGradient)
                                } else {
                                    Brush.horizontalGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.outlineVariant,
                                            MaterialTheme.colorScheme.outlineVariant,
                                        ),
                                    )
                                },
                            ),
                    )
                }
            }

            // Namenskarte / Abschluss
            AnimatedVisibility(
                visible = isLast,
                enter = fadeIn(tween(300)) + scaleIn(tween(300), initialScale = 0.92f),
                exit = fadeOut(tween(180)),
            ) {
                Surface(
                    shape = RoundedCornerShape(26.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            LocalStrings.current["welcome.nameTitle"],
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            LocalStrings.current["welcome.nameHint"],
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            placeholder = { Text(LocalStrings.current["welcome.namePlaceholder"]) },
                            leadingIcon = { Text("🐵", fontSize = 18.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            ),
                        )
                        Button(
                            onClick = { onConfirm(name.trim()) },
                            enabled = name.isNotBlank(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                            ),
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(LocalStrings.current["welcome.start"], fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (!isLast) {
                Button(
                    onClick = { scope.launch { pager.animateScrollToPage(pager.currentPage + 1) } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text(LocalStrings.current["welcome.next"], fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                }
                TextButton(onClick = onSkip, modifier = Modifier.padding(top = 4.dp)) {
                    Text(LocalStrings.current["welcome.later"])
                }
            }
            Spacer(Modifier.height(6.dp))
        }
    }
}

/** Erste Seite: Sprache wählen. Die Texte wechseln sofort mit der Auswahl. */
@Composable
private fun LanguageContent(
    selected: AppLanguage,
    onSelect: (AppLanguage) -> Unit,
    animationsEnabled: Boolean,
) {
    val strings = LocalStrings.current
    Column(
        Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center,
        ) {
            GlowHalo(size = 220.dp, enabled = animationsEnabled)
            FloatingEmoji("🌍", animationsEnabled)
        }
        PillChip(
            text = strings["welcome.lang.eyebrow"].uppercase(),
            color = MaterialTheme.colorScheme.primary,
            containerAlpha = 0.16f,
        )
        Text(
            strings["welcome.lang.title"],
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            strings["welcome.lang.text"],
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
        )
        listOf(AppLanguage.GERMAN to "🇩🇪", AppLanguage.ENGLISH to "🇬🇧").forEach { (option, flag) ->
            val active = option == selected
            Surface(
                onClick = { onSelect(option) },
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                border = BorderStroke(
                    width = if (active) 2.dp else 1.dp,
                    color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(flag, fontSize = 24.sp)
                    Spacer(Modifier.width(14.dp))
                    Text(
                        option.label,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                        modifier = Modifier.weight(1f),
                    )
                    if (active) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
private fun SlideContent(
    slide: WelcomeSlide,
    animationsEnabled: Boolean,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(230.dp),
            contentAlignment = Alignment.Center,
        ) {
            GlowHalo(size = 240.dp, enabled = animationsEnabled)
            FloatingEmoji(slide.emoji, animationsEnabled)
        }
        val strings = LocalStrings.current
        PillChip(
            text = strings["${slide.keyPrefix}.eyebrow"].uppercase(),
            color = MaterialTheme.colorScheme.primary,
            containerAlpha = 0.16f,
        )
        Text(
            strings["${slide.keyPrefix}.title"],
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            strings["${slide.keyPrefix}.text"],
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
        )
    }
}

/** Emoji als „schwebendes“ Hero-Element mit sanfter Drehung und Atmung. */
@Composable
private fun FloatingEmoji(emoji: String, animationsEnabled: Boolean) {
    val transition = rememberInfiniteTransition(label = "emoji")
    val float by transition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(tween(2200), RepeatMode.Reverse),
        label = "float",
    )
    val tilt by transition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(tween(3200), RepeatMode.Reverse),
        label = "tilt",
    )
    Box(
        Modifier
            .size(168.dp)
            .graphicsLayer {
                translationY = if (animationsEnabled) float.dp.toPx() else 0f
                rotationZ = if (animationsEnabled) tilt else 0f
            }
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    listOf(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                        MaterialTheme.colorScheme.surfaceContainerHigh,
                    ),
                ),
            )
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(emoji, fontSize = 74.sp)
    }
}

/** Marken-Logo mit Ring – überall dort, wo die App „ihr Gesicht“ zeigt. */
@Composable
fun BrandLogo(
    modifier: Modifier = Modifier,
    ring: Boolean = true,
) {
    val ringModifier = if (ring) {
        Modifier.border(1.5.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
    } else {
        Modifier
    }
    Image(
        painter = painterResource(R.drawable.mcaffen_logo),
        contentDescription = LocalStrings.current["cd.logo"],
        contentScale = ContentScale.Crop,
        modifier = modifier
            .clip(CircleShape)
            .then(ringModifier),
    )
}

/** Mini-Konfetti, das beim Abschluss kurz über den Screen rieselt. */
@Composable
fun CelebrationOverlay(show: Boolean, modifier: Modifier = Modifier) {
    AnimatedVisibility(
        visible = show,
        enter = fadeIn(tween(200)),
        exit = fadeOut(tween(600)),
        modifier = modifier.fillMaxSize(),
    ) {
        val colors = listOf(
            McAffenBrand.colors.gold,
            McAffenBrand.colors.positive,
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.secondary,
        )
        ConfettiBurst(
            modifier = Modifier.fillMaxSize(),
            play = show,
            colors = colors,
        )
    }
}
