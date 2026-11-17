package de.mcaffen.app.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.mcaffen.app.ui.McAffenBrand

/** Vollflächiger Farbverlauf-Hintergrund für ganze Screens. */
@Composable
fun GradientBackdrop(
    modifier: Modifier = Modifier,
    colors: List<Color> = listOf(
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
        MaterialTheme.colorScheme.background,
        MaterialTheme.colorScheme.background,
    ),
) {
    Box(
        modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors)),
    )
}

/** Karte mit feinem Farbverlauf – Basis für alle Flächen der App. */
@Composable
fun SoftCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(22.dp),
    colors: List<Color> = listOf(
        MaterialTheme.colorScheme.surfaceContainerLow,
        MaterialTheme.colorScheme.surfaceContainer,
    ),
    contentPadding: Dp = 16.dp,
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(8.dp),
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier,
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(colors))
                .padding(contentPadding),
            verticalArrangement = verticalArrangement,
            horizontalAlignment = horizontalAlignment,
        ) {
            content()
        }
    }
}

/** Sanft atmender Lichtschein hinter Hero-Elementen (Logo, Live-Zahlen). */
@Composable
fun GlowHalo(
    modifier: Modifier = Modifier,
    size: Dp = 220.dp,
    color: Color = McAffenBrand.colors.glow,
    enabled: Boolean = true,
) {
    val transition = rememberInfiniteTransition(label = "halo")
    val pulse by transition.animateFloat(
        initialValue = 0.82f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "haloPulse",
    )
    val scale = if (enabled) pulse else 1f
    Box(
        modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha = if (enabled) 0.95f else 0.6f
            }
            .blur(48.dp)
            .background(
                Brush.radialGradient(colors = listOf(color, color.copy(alpha = 0f))),
                CircleShape,
            ),
    )
}

/** Schimmer-Platzhalter für Ladezustände. */
@Composable
fun ShimmerCard(
    modifier: Modifier = Modifier,
    height: Dp = 72.dp,
    cornerRadius: Dp = 20.dp,
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val shift by transition.animateFloat(
        initialValue = -600f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 1500)),
        label = "shimmerShift",
    )
    val base = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.10f)
    val highlight = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.22f)
    Box(
        modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                Brush.linearGradient(
                    colors = listOf(base, highlight, base),
                    start = Offset(shift, 0f),
                    end = Offset(shift + 400f, 220f),
                ),
            ),
    )
}

/** Listenzeile als Schimmer (Kopf + Text). */
@Composable
fun ShimmerRow(modifier: Modifier = Modifier) {
    Card(
        modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.14f)),
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ShimmerCard(height = 12.dp, cornerRadius = 6.dp, modifier = Modifier.fillMaxWidth(0.55f))
                ShimmerCard(height = 8.dp, cornerRadius = 4.dp, modifier = Modifier.fillMaxWidth(0.32f))
            }
        }
    }
}

/** Pulsierender Punkt für „Live“-Zustände. */
@Composable
fun LiveDot(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    enabled: Boolean = true,
) {
    val transition = rememberInfiniteTransition(label = "live")
    val alpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(tween(900), repeatMode = RepeatMode.Reverse),
        label = "liveAlpha",
    )
    Box(
        modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = if (enabled) alpha else 1f)),
    )
}
