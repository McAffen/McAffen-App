package de.mcaffen.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import de.mcaffen.app.ui.McAffenBrand
import java.util.Random

/**
 * Ruhiger, lebendiger Hintergrund für den Startbildschirm: weiche Farbwolken,
 * die langsam treiben, ein feines Punktraster und schwebende Partikel.
 */
@Composable
fun AnimatedBackdrop(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    blobA: Color = MaterialTheme.colorScheme.primary,
    blobB: Color = MaterialTheme.colorScheme.secondary,
) {
    val transition = rememberInfiniteTransition(label = "backdrop")
    val drift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14_000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "drift",
    )
    val breath by transition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6_000),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "breath",
    )
    val t = if (enabled) drift else 0.5f
    val b = if (enabled) breath else 1f

    Box(
        modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.background,
                        McAffenBrand.colors.heroGradient.first(),
                        MaterialTheme.colorScheme.background,
                    ),
                ),
            ),
    ) {
        Box(
            Modifier
                .size(340.dp)
                .offset(x = (-120).dp + (t * 70).dp, y = (-90).dp + (t * 40).dp)
                .graphicsLayer {
                    scaleX = b
                    scaleY = b
                    alpha = 0.5f
                }
                .blur(90.dp)
                .clip(CircleShape)
                .background(blobA),
        )
        Box(
            Modifier
                .size(280.dp)
                .offset(x = 170.dp - (t * 50).dp, y = 420.dp - (t * 60).dp)
                .graphicsLayer {
                    scaleX = 1.1f * b
                    scaleY = 1.1f * b
                    alpha = 0.32f
                }
                .blur(100.dp)
                .clip(CircleShape)
                .background(blobB),
        )
        DotGrid(
            Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = if (enabled) 0.10f + 0.05f * t else 0.10f },
        )
        // Zusätzliche Tiefe: feine Schräglinien + ein dritter, langsam atmender
        // Farbschein in der Akzentfarbe des Themas.
        DiagonalStreaks(Modifier.fillMaxSize(), enabled = enabled)
        Box(
            Modifier
                .size(240.dp)
                .offset(x = 60.dp + (t * 40).dp, y = (-40).dp + (t * 90).dp)
                .graphicsLayer {
                    scaleX = 1.2f * b
                    scaleY = 1.2f * b
                    alpha = 0.22f
                }
                .blur(110.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.tertiary),
        )
        FloatingParticles(Modifier.fillMaxSize(), enabled = enabled)
    }
}

/** Feines Punktraster (Canvas, keine Bitmap). */
@Composable
fun DotGrid(modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.onSurface) {
    Canvas(modifier) {
        val spacing = 22.dp.toPx()
        val radius = 1.1.dp.toPx()
        var y = 0f
        while (y < size.height) {
            var x = 0f
            while (x < size.width) {
                drawCircle(color = color, radius = radius, center = Offset(x, y))
                x += spacing
            }
            y += spacing
        }
    }
}

/** Schwebende Partikel – subtile Tiefe für Hero-Bereiche. */
@Composable
fun FloatingParticles(
    modifier: Modifier = Modifier,
    count: Int = 14,
    enabled: Boolean = true,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    val transition = rememberInfiniteTransition(label = "particles")
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "particleT",
    )
    val seeds = remember {
        val random = Random(42)
        List(count) {
            Triple(random.nextFloat(), random.nextFloat(), 0.4f + random.nextFloat() * 0.8f)
        }
    }
    Canvas(modifier) {
        seeds.forEach { (sx, sy, speed) ->
            val progress = if (enabled) (t * speed + sy) % 1f else sy
            val x = sx * size.width
            val y = (1f - progress) * size.height
            val r = (1.2f + 1.8f * sx).dp.toPx()
            drawCircle(
                color = color.copy(alpha = 0.28f * (1f - progress) + 0.05f),
                radius = r,
                center = Offset(x, y),
            )
        }
    }
}

/** Abschnitts-Header mit Akzentbalken in Markenfarbe. */
@Composable
fun SectionTitle(
    text: String,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                Modifier
                    .size(width = 4.dp, height = 18.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Brush.verticalGradient(McAffenBrand.colors.brandGradient)),
            )
            Text(
                text,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
        trailing?.invoke()
    }
}

/** Dekorative Trennlinie, die zur Mitte hin ausläuft. */
@Composable
fun SoftDivider(modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color.Transparent,
                        MaterialTheme.colorScheme.outlineVariant,
                        Color.Transparent,
                    ),
                ),
            ),
    )
}
