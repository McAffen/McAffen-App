package de.mcaffen.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.mcaffen.app.ui.McAffenBrand

/**
 * Zusätzliche, ruhige Effekte für mehr Tiefe – alle rein mit Canvas/Modifiern,
 * keine Bitmaps, und jeder lässt sich über [enabled] abschalten.
 */

/** Langsam wandernder Lichtstreifen über einer Fläche („Shimmer"). */
@Composable
fun ShimmerLine(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    color: Color = MaterialTheme.colorScheme.primary,
    height: Dp = 1.dp,
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val shift by transition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3_200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmerShift",
    )
    val t = if (enabled) shift else 0.5f
    Box(
        modifier
            .fillMaxWidth()
            .height(height)
            .background(
                Brush.horizontalGradient(
                    0f to Color.Transparent,
                    0.35f to color.copy(alpha = 0.05f),
                    0.5f to color.copy(alpha = 0.55f),
                    0.65f to color.copy(alpha = 0.05f),
                    1f to Color.Transparent,
                    startX = (t * 1_200f),
                    endX = (t * 1_200f) + 900f,
                ),
            ),
    )
}

/** Zwei gegenläufig drehende Ringe hinter einem Element – gibt Tiefe ohne Unruhe. */
@Composable
fun OrbitRings(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    color: Color = MaterialTheme.colorScheme.primary,
    size: Dp = 220.dp,
) {
    val transition = rememberInfiniteTransition(label = "orbit")
    val spin by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 24_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "orbitSpin",
    )
    val angle = if (enabled) spin else 0f
    Box(
        modifier.size(size),
        contentAlignment = androidx.compose.ui.Alignment.Center,
    ) {
        Box(
            Modifier
                .size(size * 0.86f)
                .graphicsLayer { rotationZ = angle }
                .clip(CircleShape)
                .background(
                    Brush.sweepGradient(
                        listOf(
                            Color.Transparent,
                            color.copy(alpha = 0.16f),
                            Color.Transparent,
                        ),
                    ),
                ),
        )
        Box(
            Modifier
                .size(size * 0.62f)
                .graphicsLayer { rotationZ = -angle * 1.4f }
                .clip(CircleShape)
                .background(
                    Brush.sweepGradient(
                        listOf(
                            Color.Transparent,
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.20f),
                            Color.Transparent,
                        ),
                    ),
                ),
        )
    }
}

/** Weicher Farbschein, der hinter Karten atmet – verstärkt den Verlauf der Fläche. */
@Composable
fun BreathingGlow(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    color: Color = McAffenBrand.colors.brandGradient.first(),
    size: Dp = 180.dp,
) {
    val transition = rememberInfiniteTransition(label = "glow")
    val pulse by transition.animateFloat(
        initialValue = 0.80f,
        targetValue = 1.20f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4_600),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "glowPulse",
    )
    val scale = if (enabled) pulse else 1f
    Box(
        modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha = 0.35f
            }
            .blur(70.dp)
            .clip(CircleShape)
            .background(color),
    )
}

/** Feine, schräg laufende Linien als Textur über einer Fläche. */
@Composable
fun DiagonalStreaks(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    color: Color = MaterialTheme.colorScheme.onSurface,
) {
    val transition = rememberInfiniteTransition(label = "streaks")
    val move by transition.animateFloat(
        initialValue = 0f,
        targetValue = 40f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "streakMove",
    )
    val offset = if (enabled) move else 0f
    androidx.compose.foundation.Canvas(modifier.fillMaxSize()) {
        val step = 26.dp.toPx()
        val alpha = if (enabled) 0.05f else 0.03f
        var x = -size.height + offset
        while (x < size.width + size.height) {
            drawLine(
                color = color.copy(alpha = alpha),
                start = Offset(x, size.height),
                end = Offset(x + size.height, 0f),
                strokeWidth = 1.dp.toPx(),
            )
            x += step
        }
    }
}
