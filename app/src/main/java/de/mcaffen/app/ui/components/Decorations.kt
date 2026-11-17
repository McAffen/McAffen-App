package de.mcaffen.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin

/** Zeichnet eine Wellenlinie (Verlauf) als Canvas – für Hero-Bereiche. */
@Composable
fun WaveDecoration(
    modifier: Modifier = Modifier,
    color: Color,
    amplitude: Dp = 10.dp,
    wavelength: Dp = 120.dp,
    animate: Boolean = true,
) {
    val transition = rememberInfiniteTransition(label = "wave")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "phase",
    )
    val p = if (animate) phase else 0f
    Canvas(modifier) {
        drawWave(p, color, amplitude.toPx(), wavelength.toPx())
    }
}

private fun DrawScope.drawWave(phase: Float, color: Color, amplitude: Float, wavelength: Float) {
    val path = Path()
    val mid = size.height / 2f
    val steps = 80
    for (i in 0..steps) {
        val x = size.width * i / steps
        val y = mid + sin((x / wavelength) * 2f * PI.toFloat() + phase) * amplitude
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    drawPath(
        path,
        brush = Brush.horizontalGradient(
            listOf(color.copy(alpha = 0f), color, color.copy(alpha = 0f)),
        ),
        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round),
    )
}

/**
 * Schlanker Fortschrittsbalken mit Verlauf und animiertem Füllstand.
 * [fraction] 0..1, [color] Akzentfarbe, [track] Hintergrund.
 */
@Composable
fun GradientProgressBar(
    fraction: Float,
    modifier: Modifier = Modifier,
    color: Color,
    track: Color,
    height: Dp = 6.dp,
    animate: Boolean = true,
) {
    val progress = remember { Animatable(if (animate) 0f else fraction) }
    LaunchedEffect(fraction) {
        progress.animateTo(
            targetValue = fraction.coerceIn(0f, 1f),
            animationSpec = tween(durationMillis = 900, easing = LinearEasing),
        )
    }
    val value = if (animate) progress.value else fraction.coerceIn(0f, 1f)
    Canvas(
        modifier
            .fillMaxWidth()
            .height(height),
    ) {
        val radius = size.height / 2f
        drawRoundRect(
            color = track.copy(alpha = 0.45f),
            size = size,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius, radius),
        )
        val w = size.width * value
        if (w > 0f) {
            drawRoundRect(
                brush = Brush.horizontalGradient(listOf(color, color.copy(alpha = 0.75f))),
                size = Size(w.coerceAtLeast(size.height), size.height),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius, radius),
            )
        }
    }
}

/** Konfetti-Regen für Erfolgsmomente (z. B. „Vote gespeichert“). */
@Composable
fun ConfettiBurst(
    modifier: Modifier = Modifier,
    play: Boolean,
    colors: List<Color>,
    pieces: Int = 42,
) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(play) {
        if (play) {
            progress.snapTo(0f)
            progress.animateTo(1f, animationSpec = tween(durationMillis = 1600))
        }
    }
    if (!play) return
    val seeds = remember {
        val random = java.util.Random(7)
        List(pieces) {
            FloatArray(6).also { a ->
                a[0] = random.nextFloat() // startX
                a[1] = random.nextFloat() // speed
                a[2] = random.nextFloat() // spread
                a[3] = random.nextFloat() // size
                a[4] = random.nextFloat() // rotation
                a[5] = random.nextFloat() // color index
            }
        }
    }
    Canvas(modifier) {
        val t = progress.value
        seeds.forEach { a ->
            val x = size.width * a[0] + sin((t * 6f + a[2] * 10f)) * 40.dp.toPx()
            val y = -20.dp.toPx() + t * (size.height + 60.dp.toPx()) * (0.6f + a[1] * 0.8f)
            val c = colors[(a[5] * colors.size).toInt().coerceIn(0, colors.size - 1)]
            drawCircle(
                color = c.copy(alpha = (1f - t).coerceIn(0f, 1f)),
                radius = (2.dp + 3.dp * a[3]).toPx(),
                center = Offset(x, y),
            )
        }
    }
}
