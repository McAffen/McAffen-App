package de.mcaffen.app.ui.components

import android.net.Uri
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import de.mcaffen.app.ui.McAffenBrand

/**
 * Minecraft-Spielerkopf von mc-heads.net mit Markenring.
 * [ringColor] färbt den Rand (Gold für Platz 1, Akzent für „Du“).
 */
@Composable
fun PlayerAvatar(
    name: String,
    size: Dp,
    modifier: Modifier = Modifier,
    ringColor: Color? = null,
    ringWidth: Dp = 2.dp,
) {
    val border = if (ringColor != null) {
        Modifier.border(ringWidth, ringColor, CircleShape)
    } else {
        Modifier
    }
    SubcomposeAsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data("https://mc-heads.net/avatar/${Uri.encode(name)}/128")
            .crossfade(true)
            .build(),
        contentDescription = "Minecraft-Kopf von $name",
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .then(border),
        loading = { AvatarFallback(size) },
        error = { AvatarFallback(size) },
    )
}

@Composable
private fun AvatarFallback(size: Dp) {
    Box(
        Modifier
            .size(size)
            .background(
                Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.surfaceContainerHigh,
                        MaterialTheme.colorScheme.surfaceContainerHighest,
                    ),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            Icons.Default.Person,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(size / 2),
        )
    }
}

/** Ringförmiger Fortschritt (z. B. „Abstand zu Platz 1“) mit animiertem Bogen. */
@Composable
fun ProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 6.dp,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    accent: Color = McAffenBrand.colors.brandGradient.last(),
    content: @Composable (() -> Unit)? = null,
) {
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = spring(stiffness = Spring.StiffnessVeryLow, dampingRatio = Spring.DampingRatioNoBouncy),
        label = "ring",
    )
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = strokeWidth.toPx()
            val inset = stroke / 2
            val arcSize = Size(size.width - stroke, size.height - stroke)
            drawArc(
                color = trackColor.copy(alpha = 0.35f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(color, accent, color),
                ),
                startAngle = -90f,
                sweepAngle = 360f * animated,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
        }
        content?.invoke()
    }
}

/** Chip mit Markenfarbe, optionalem Icon und Tönung. */
@Composable
fun PillChip(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    containerAlpha: Float = 0.14f,
    icon: (@Composable () -> Unit)? = null,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = color.copy(alpha = containerAlpha),
    ) {
        Row(
            Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            icon?.invoke()
            Text(
                text,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = color,
            )
        }
    }
}

/**
 * Karte, die beim Antippen sanft einsinkt – gibt jedem Tap ein Gefühl von Gewicht.
 * [enabled] = false deaktiviert nur die Animation, nicht den Klick.
 */
@Composable
fun PressableCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) 0.975f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "press",
    )
    val indication = ripple(bounded = true)
    Box(
        modifier
            .scale(scale)
            .clickable(
                interactionSource = interaction,
                indication = indication,
                onClick = onClick,
            ),
    ) {
        content()
    }
}

/** Kleiner Statuspunkt mit Außenring (online / offline). */
@Composable
fun StatusDot(
    online: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 10.dp,
) {
    val brand = McAffenBrand.colors
    val color = if (online) brand.positive else brand.negative
    Box(modifier.size(size.times(1.8f)), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .size(size.times(1.8f))
                .clip(CircleShape)
                .background(brush = SolidColor(color.copy(alpha = 0.18f))),
        )
        Box(
            Modifier
                .size(size)
                .clip(CircleShape)
                .background(brush = SolidColor(color)),
        )
    }
}
