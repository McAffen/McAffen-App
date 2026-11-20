package de.mcaffen.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.mcaffen.app.ui.LocalStrings

/**
 * Blendet Inhalt beim ersten Anzeigen mit Feder und Fade ein.
 * [delayMillis] staffelt Karten in Listen (Reveal-Effekt).
 */
@Composable
fun Reveal(
    modifier: Modifier = Modifier,
    visible: Boolean = true,
    fromScale: Float = 0.94f,
    fromOffsetY: Dp = 18.dp,
    content: @Composable () -> Unit,
) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(visible) {
        if (visible) {
            progress.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow,
                ),
            )
        }
    }
    Box(
        modifier.graphicsLayer {
            alpha = progress.value.coerceIn(0f, 1f)
            val s = fromScale + (1f - fromScale) * progress.value
            scaleX = s
            scaleY = s
            translationY = (1f - progress.value) * fromOffsetY.toPx()
        },
    ) {
        content()
    }
}

/**
 * Vollflächiger Startbildschirm: Logo federt ein, Farbwolke atmet,
 * Ladezeile und Punkte darunter – statt eines nackten Spinners.
 */
@Composable
fun BootSplash(
    modifier: Modifier = Modifier,
    show: Boolean,
    animationsEnabled: Boolean = true,
    text: String = LocalStrings.current["boot.loading"],
    logo: @Composable (Modifier) -> Unit,
) {
    AnimatedVisibility(
        visible = show,
        enter = fadeIn(tween(200)) + scaleIn(tween(320), initialScale = 1f),
        exit = fadeOut(tween(400)) + scaleOut(tween(400), targetScale = 1.08f),
        modifier = modifier.fillMaxSize(),
    ) {
        val scale = remember { Animatable(0.55f) }
        LaunchedEffect(Unit) {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow,
                ),
            )
        }
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                            MaterialTheme.colorScheme.background,
                        ),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            GlowHalo(size = 260.dp, enabled = animationsEnabled)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(22.dp),
            ) {
                logo(
                    Modifier
                        .size(132.dp)
                        .scale(if (animationsEnabled) scale.value else 1f),
                )
                Text(
                    text,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                PulsingDots()
            }
        }
    }
}

/** Drei Punkte, die nacheinander aufleuchten (Ladeindikator ohne Spinner-Optik). */
@Composable
fun PulsingDots(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    val transition = rememberInfiniteTransition(label = "dots")
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(3) { index ->
            val alpha by transition.animateFloat(
                initialValue = 0.2f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 520, delayMillis = index * 140),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "dot$index",
            )
            Box(
                Modifier
                    .size(7.dp)
                    .graphicsLayer { this.alpha = alpha }
                    .clip(CircleShape)
                    .background(color),
            )
        }
    }
}
