@file:OptIn(ExperimentalMaterial3Api::class)

package de.mcaffen.app.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.mcaffen.app.model.PlayerStat
import de.mcaffen.app.model.VoteSite
import de.mcaffen.app.ui.McAffenBrand
import de.mcaffen.app.ui.VOTE_COOLDOWN_MS
import de.mcaffen.app.ui.components.AnimatedBackdrop
import de.mcaffen.app.ui.components.CelebrationOverlay
import de.mcaffen.app.ui.components.GradientProgressBar
import de.mcaffen.app.ui.components.PillChip
import de.mcaffen.app.ui.components.PlayerAvatar
import de.mcaffen.app.ui.components.Reveal
import de.mcaffen.app.ui.components.SectionTitle
import de.mcaffen.app.ui.components.SoftCard
import de.mcaffen.app.ui.components.StatusDot
import de.mcaffen.app.ui.formatRemaining
import de.mcaffen.app.ui.formatRemainingShort
import de.mcaffen.app.ui.formatScore
import de.mcaffen.app.ui.toVoteUri
import kotlinx.coroutines.delay

/**
 * Vote-Bereich: Namensprüfung, Fortschritt über alle Seiten und je Seite eine
 * Karte mit Countdown, Status und direktem Vote-Button.
 */
@Composable
internal fun VoteScreen(
    players: List<PlayerStat>,
    voteSites: List<VoteSite>,
    voteTimes: Map<String, Long>,
    onMarkVoted: (String) -> Unit,
    storedName: String,
    onStoreName: (String) -> Unit,
    loading: Boolean,
    animationsEnabled: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = System.currentTimeMillis()
            delay(1000)
        }
    }
    var name by remember { mutableStateOf(storedName) }
    var searchedName by remember { mutableStateOf<String?>(null) }
    var celebrate by remember { mutableStateOf(false) }

    val player = players.firstOrNull { it.name.equals(searchedName, ignoreCase = true) }
    val canVote = player != null && name.trim().equals(searchedName, ignoreCase = true)
    val selectedPlayerName = player?.name
    val canSearch = name.isNotBlank() && players.isNotEmpty()

    val readyCount = voteSites.count { site ->
        val last = voteTimes[site.site] ?: 0L
        last <= 0L || last + VOTE_COOLDOWN_MS - now <= 0
    }
    val total = voteSites.size.coerceAtLeast(1)
    val readyFraction by animateFloatAsState(
        targetValue = readyCount.toFloat() / total,
        animationSpec = tween(600),
        label = "readyFraction",
    )

    Box(modifier.fillMaxSize()) {
        AnimatedBackdrop(enabled = animationsEnabled)
        PullToRefreshBox(isRefreshing = loading, onRefresh = onRefresh, modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item("progress") {
                    Reveal(visible = animationsEnabled) {
                        VoteProgressCard(
                            readyCount = readyCount,
                            total = voteSites.size,
                            fraction = readyFraction,
                            playerName = selectedPlayerName,
                        )
                    }
                }
                item("name") {
                    SoftCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        contentPadding = 16.dp,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        SectionTitle("Dein Minecraft-Name")
                        OutlinedTextField(
                            value = name,
                            onValueChange = {
                                name = it
                                searchedName = null
                                onStoreName(it.trim())
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            placeholder = { Text("Spielername") },
                            shape = RoundedCornerShape(16.dp),
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            ),
                        )
                        Button(
                            onClick = { searchedName = name.trim().takeIf(String::isNotEmpty) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            enabled = canSearch,
                            shape = RoundedCornerShape(14.dp),
                        ) {
                            Text("Prüfen & freischalten", fontWeight = FontWeight.Bold)
                        }
                        AnimatedContent(
                            targetState = when {
                                searchedName != null && player != null -> "ok"
                                searchedName != null -> "missing"
                                else -> "hint"
                            },
                            transitionSpec = {
                                (fadeIn(tween(220)) + scaleIn(tween(220), initialScale = 0.96f)) togetherWith
                                    (fadeOut(tween(150)) + scaleOut(tween(150), targetScale = 0.98f))
                            },
                            label = "voteStatus",
                        ) { state ->
                            when (state) {
                                "ok" -> Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    PlayerAvatar(player?.name.orEmpty(), 40.dp, ringColor = McAffenBrand.colors.positive)
                                    Column {
                                        Text(
                                            "Spieler gefunden",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = McAffenBrand.colors.positive,
                                        )
                                        Text(
                                            player?.name.orEmpty(),
                                            style = MaterialTheme.typography.bodySmall,
                                        )
                                    }
                                }
                                "missing" -> Text(
                                    "Spieler nicht gefunden – prüfe die Schreibweise.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = McAffenBrand.colors.negative,
                                )
                                else -> Text(
                                    "Gib deinen Namen ein, um voten zu können. Er wird gespeichert.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
                item("header") {
                    SectionTitle("Vote-Seiten") {
                        PillChip(
                            text = "$readyCount / ${voteSites.size} bereit",
                            color = if (readyCount > 0) McAffenBrand.colors.positive else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                if (voteSites.isEmpty()) {
                    item("empty") {
                        EmptyState(
                            emoji = "🗳️",
                            title = "Keine Vote-Seiten",
                            text = "Ziehe zum Aktualisieren nach unten – die Seiten kommen von mcaffen.de.",
                        )
                    }
                }
                items(voteSites, key = { it.site }) { site ->
                    val lastVoted = voteTimes[site.site] ?: 0L
                    val remaining = lastVoted + VOTE_COOLDOWN_MS - now
                    val readyAgain = lastVoted <= 0L || remaining <= 0
                    val voteUri = selectedPlayerName?.let { playerName -> site.url.toVoteUri(playerName) }
                    Reveal(visible = animationsEnabled, fromScale = 0.97f) {
                        VoteSiteCard(
                            site = site,
                            lastVoted = lastVoted,
                            remaining = remaining,
                            readyAgain = readyAgain,
                            canVote = canVote && voteUri != null,
                            onVote = {
                                onMarkVoted(site.site)
                                voteUri?.let { context.startActivity(Intent(Intent.ACTION_VIEW, it)) }
                                celebrate = true
                            },
                        )
                    }
                }
                item("footer") {
                    Text(
                        "Für jede Abstimmung erhältst du Belohnungen wie Vote-Schlüssel und Coins. " +
                            "Die Vote-Links öffnen sich im Browser.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }
        CelebrationOverlay(show = celebrate, modifier = Modifier.fillMaxSize())
        LaunchedEffect(celebrate) {
            if (celebrate) {
                delay(1700)
                celebrate = false
            }
        }
    }
}

/** Fortschrittskarte: wie viele Vote-Seiten heute schon bereit sind. */
@Composable
private fun VoteProgressCard(
    readyCount: Int,
    total: Int,
    fraction: Float,
    playerName: String?,
) {
    SoftCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = McAffenBrand.colors.brandGradient.map { it.copy(alpha = 0.92f) },
        contentPadding = 18.dp,
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    "Vote-Fortschritt",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.85f),
                )
                Text(
                    "$readyCount von $total bereit",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                )
            }
            Text("🗳️", fontSize = 34.sp)
        }
        GradientProgressBar(
            fraction = fraction,
            color = Color.White,
            track = Color.Black,
            height = 8.dp,
        )
        Text(
            if (playerName != null) {
                "Voten für $playerName – jede Seite bringt Punkte."
            } else {
                "Spieler prüfen, um alle Seiten freizuschalten."
            },
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.85f),
        )
    }
}

/** Karte einer Vote-Seite mit Countdown, Status und Vote-Button. */
@Composable
private fun VoteSiteCard(
    site: VoteSite,
    lastVoted: Long,
    remaining: Long,
    readyAgain: Boolean,
    canVote: Boolean,
    onVote: () -> Unit,
) {
    val accent = when {
        lastVoted <= 0L -> MaterialTheme.colorScheme.primary
        readyAgain -> McAffenBrand.colors.positive
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    SoftCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        contentPadding = 14.dp,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(accent.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center,
            ) {
                StatusDot(online = lastVoted <= 0L || readyAgain, size = 8.dp)
            }
            Column(Modifier.weight(1f)) {
                Text(site.site, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                Text(
                    when {
                        lastVoted <= 0L -> "Vote-Schlüssel & Coins"
                        readyAgain -> "Bereit – du kannst wieder voten"
                        else -> "Wieder möglich in ${formatRemainingShort(remaining)}"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = accent,
                )
            }
            Button(
                onClick = onVote,
                enabled = canVote,
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (readyAgain) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = if (readyAgain) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer,
                ),
            ) {
                Text(if (readyAgain) "Voten" else "Nochmal", fontWeight = FontWeight.Bold)
                Icon(
                    Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .size(15.dp),
                )
            }
        }
        if (lastVoted > 0L && !readyAgain) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    Icons.Default.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    formatRemaining(remaining),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.width(4.dp))
                Box(
                    Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                ) {
                    val done = 1f - (remaining.toFloat() / VOTE_COOLDOWN_MS).coerceIn(0f, 1f)
                    Box(
                        Modifier
                            .fillMaxWidth(done)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(Brush.horizontalGradient(McAffenBrand.colors.brandGradient)),
                    )
                }
            }
        }
        if (lastVoted > 0L && readyAgain) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = McAffenBrand.colors.positive,
                    modifier = Modifier.size(15.dp),
                )
                Text(
                    "Zuletzt gevotet – jetzt wieder möglich",
                    style = MaterialTheme.typography.labelSmall,
                    color = McAffenBrand.colors.positive,
                )
            }
        }
        if (!canVote) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            ) {
                Text(
                    "Spieler prüfen, um diese Seite zu öffnen.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
