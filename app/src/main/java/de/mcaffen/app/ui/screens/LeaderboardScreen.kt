@file:OptIn(ExperimentalMaterial3Api::class)

package de.mcaffen.app.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.mcaffen.app.model.PlayerStat
import de.mcaffen.app.ui.McAffenBrand
import de.mcaffen.app.ui.components.AnimatedBackdrop
import de.mcaffen.app.ui.components.GlowHalo
import de.mcaffen.app.ui.components.GradientProgressBar
import de.mcaffen.app.ui.components.LiveDot
import de.mcaffen.app.ui.components.PillChip
import de.mcaffen.app.ui.components.PlayerAvatar
import de.mcaffen.app.ui.components.PressableCard
import de.mcaffen.app.ui.components.ProgressRing
import de.mcaffen.app.ui.components.Reveal
import de.mcaffen.app.ui.components.SectionTitle
import de.mcaffen.app.ui.components.ShimmerRow
import de.mcaffen.app.ui.components.SoftCard
import kotlinx.coroutines.launch

internal const val PODIUM_SIZE = 3

/** Gold, Silber, Bronze – aus den Markenfarben des Themes. */
@Composable
internal fun medalColor(position: Int): Color? = when (position) {
    1 -> McAffenBrand.colors.gold
    2 -> McAffenBrand.colors.silver
    3 -> McAffenBrand.colors.bronze
    else -> null
}

/**
 * Rangliste mit Hero-Kopf, Podium, „Dein Profil“-Karte und animierten Zeilen.
 */
@Composable
internal fun LeaderboardScreen(
    players: List<PlayerStat>,
    myName: String,
    loading: Boolean,
    animationsEnabled: Boolean,
    bestRank: Int,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    boardSort: String = "waves",
    boardPlayers: List<PlayerStat> = emptyList(),
    boardLoading: Boolean = false,
    onBoardSortChange: (String) -> Unit = {},
) {
    // Die Standardliste kommt aus dem Cache (auch offline); die anderen Werte
    // werden live geladen. Angezeigt wird immer die aktive Ansicht.
    val activePlayers = if (boardSort == "waves") players else boardPlayers
    val activeLoading = loading || boardLoading
    val players = activePlayers
    var query by remember { mutableStateOf("") }
    val filtered = players.filter { it.name.contains(query, ignoreCase = true) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val myIndex = players.indexOfFirst { it.name.equals(myName, ignoreCase = true) }
    val myStat = if (myIndex >= 0) players[myIndex] else null
    val leaderScore = players.firstOrNull()?.score ?: 0

    fun jumpToMe() {
        if (query.isNotBlank() || myIndex < PODIUM_SIZE) return
        val lazyIndex = myIndex - PODIUM_SIZE
        if (lazyIndex >= 0) {
            scope.launch { listState.animateScrollToItem(lazyIndex) }
        }
    }

    Box(modifier.fillMaxSize()) {
        AnimatedBackdrop(enabled = animationsEnabled)
        PullToRefreshBox(isRefreshing = loading, onRefresh = onRefresh, modifier = Modifier.fillMaxSize()) {
            when {
                players.isEmpty() && !activeLoading ->
                    EmptyState(
                        emoji = "🏆",
                        title = "Rangliste wird geladen",
                        text = "Sobald mcaffen.de Daten liefert, erscheint hier das Podium " +
                            "(aktuell: erreichte Wellen in „Nacht der Untoten“). Ziehe zum " +
                            "Aktualisieren nach unten.",
                        modifier = Modifier.fillMaxSize(),
                    )
                activeLoading && players.isEmpty() ->
                    Column(
                        Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        ShimmerRow()
                        ShimmerRow()
                        ShimmerRow()
                        ShimmerRow()
                    }
                else ->
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        item("sorts") {
                            BoardSortRow(selected = boardSort, onSelect = onBoardSortChange)
                        }
                        item("search") {
                            SearchField(query = query, onQueryChange = { query = it })
                        }
                        if (query.isBlank()) {
                            item("hero") {
                                Reveal(visible = animationsEnabled) {
                                    LeaderboardHero(
                                        playerCount = players.size,
                                        onlineTop = players.firstOrNull()?.name,
                                        topScore = leaderScore,
                                        animationsEnabled = animationsEnabled,
                                    )
                                }
                            }
                            if (myName.isNotBlank()) {
                                item("me") {
                                    Reveal(visible = animationsEnabled) {
                                        MyProfileCard(
                                            myName = myName,
                                            myStat = myStat,
                                            myIndex = myIndex,
                                            leaderScore = leaderScore,
                                            bestRank = bestRank,
                                            canJump = players.size > PODIUM_SIZE && myIndex >= PODIUM_SIZE,
                                            onJump = ::jumpToMe,
                                        )
                                    }
                                }
                            }
                            item("podium") {
                                Reveal(visible = animationsEnabled) { Podium(players.take(PODIUM_SIZE)) }
                            }
                            item("listHeader") {
                                SectionTitle("Platzierungen · Wellen (Nacht der Untoten)") {
                                    PillChip(
                                        text = "${formatScore(players.size)} Spieler",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        containerAlpha = 0.10f,
                                    )
                                }
                            }
                        }
                        if (filtered.isEmpty()) {
                            item("empty") {
                                EmptyState(
                                    emoji = "🔍",
                                    title = "Niemanden gefunden",
                                    text = "Kein Spieler passt zu „$query“. Prüfe die Schreibweise.",
                                    modifier = Modifier.padding(top = 24.dp),
                                )
                            }
                        } else {
                            val listed = when {
                                query.isNotBlank() -> filtered
                                players.size > PODIUM_SIZE -> players.drop(PODIUM_SIZE)
                                else -> emptyList()
                            }
                            items(listed, key = { it.name }) { player ->
                                val position = players.indexOfFirst { it.name == player.name } + 1
                                PlayerRow(
                                    player = player,
                                    position = position,
                                    leaderScore = leaderScore,
                                    isMe = player.name.equals(myName, ignoreCase = true),
                                    animationsEnabled = animationsEnabled,
                                )
                            }
                        }
                    }
            }
        }
    }
}

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Spieler suchen") },
        leadingIcon = {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Suche leeren",
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { onQueryChange("") },
                )
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    )
}

/** Hero-Kopf der Rangliste: Markenfarbe, Kennzahlen und atmender Schein. */
@Composable
private fun LeaderboardHero(
    playerCount: Int,
    onlineTop: String?,
    topScore: Int,
    animationsEnabled: Boolean,
) {
    val animatedCount by animateIntAsState(
        targetValue = playerCount,
        animationSpec = tween(900),
        label = "heroCount",
    )
    Box(Modifier.fillMaxWidth()) {
        SoftCard(
            modifier = Modifier.fillMaxWidth(),
            colors = McAffenBrand.colors.brandGradient.map { it.copy(alpha = 0.92f) },
            shape = RoundedCornerShape(26.dp),
            contentPadding = 20.dp,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PillChip(
                    text = "LIVE",
                    color = Color.White,
                    containerAlpha = 0.22f,
                    icon = { LiveDot(color = Color.White, enabled = animationsEnabled) },
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Rangliste",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.9f),
                )
            }
            Text(
                "Platz ${formatScore(1)} wartet",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
            )
            Text(
                if (onlineTop != null) {
                    "$onlineTop führt mit ${formatScore(topScore)} Punkten – ${formatScore(animatedCount)} Spieler im Rennen."
                } else {
                    "Noch keine Platzierungen verfügbar."
                },
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.85f),
            )
        }
        if (animationsEnabled) {
            Box(Modifier.align(Alignment.TopEnd)) {
                GlowHalo(size = 140.dp, color = Color.White.copy(alpha = 0.22f))
            }
        }
    }
}

/** Karte „Dein Profil“: Rang, Punkte, Fortschritt zu Platz 1, Rekord. */
@Composable
private fun MyProfileCard(
    myName: String,
    myStat: PlayerStat?,
    myIndex: Int,
    leaderScore: Int,
    bestRank: Int,
    canJump: Boolean,
    onJump: () -> Unit,
) {
    val rank = myIndex + 1
    val progress = if (myStat != null && leaderScore > 0) {
        myStat.score.toFloat() / leaderScore
    } else {
        0f
    }
    val isRecord = myStat != null && bestRank > 0 && rank == bestRank && bestRank > 1
    PressableCard(onClick = { if (canJump) onJump() }) {
        SoftCard(
            modifier = Modifier.fillMaxWidth(),
            colors = listOf(
                MaterialTheme.colorScheme.primaryContainer,
                MaterialTheme.colorScheme.surfaceContainerHigh,
            ),
            shape = RoundedCornerShape(26.dp),
            contentPadding = 18.dp,
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                ProgressRing(
                    progress = progress,
                    modifier = Modifier.size(76.dp),
                    strokeWidth = 6.dp,
                    color = MaterialTheme.colorScheme.primary,
                ) {
                    PlayerAvatar(
                        name = myName,
                        size = 58.dp,
                        ringColor = MaterialTheme.colorScheme.primary,
                    )
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        "Dein Profil",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        myName,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (myStat != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                "Rang #$rank",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                "· ${formatScore(myStat.score)} Punkte",
                                style = MaterialTheme.typography.bodySmall,
                            )
                            TrendChip(myStat)
                        }
                    } else {
                        Text(
                            "Noch nicht in der Rangliste – ein Vote pro Tag bringt dich rein.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            if (myStat != null) {
                GradientProgressBar(
                    fraction = progress,
                    color = MaterialTheme.colorScheme.primary,
                    track = MaterialTheme.colorScheme.onSurface,
                    animate = true,
                )
                Text(
                    when {
                        rank == 1 -> "Du führst die Rangliste an 👑"
                        leaderScore > myStat.score ->
                            "Noch ${formatScore(leaderScore - myStat.score)} Punkte bis Platz 1"
                        else -> "Gleichauf mit der Spitze"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (isRecord || canJump) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (isRecord) PillChip(text = "Bester Rang", color = McAffenBrand.colors.gold)
                    if (canJump) {
                        PillChip(
                            text = "In der Liste zeigen →",
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }
}

/** Top-3-Podium: Platz 2 – Platz 1 – Platz 3, mit Krone für die Spitze. */
@Composable
private fun Podium(players: List<PlayerStat>) {
    if (players.isEmpty()) return
    val visualOrder = buildList {
        players.getOrNull(1)?.let { add(1) }
        players.getOrNull(0)?.let { add(0) }
        players.getOrNull(2)?.let { add(2) }
    }
    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        visualOrder.forEach { index ->
            PodiumSlot(
                player = players[index],
                position = index + 1,
                isChampion = index == 0,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun PodiumSlot(
    player: PlayerStat,
    position: Int,
    isChampion: Boolean,
    modifier: Modifier = Modifier,
) {
    val medal = medalColor(position)
    val crown by animateFloatAsState(
        targetValue = if (isChampion) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "crown",
    )
    Column(
        modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (isChampion) {
            Text(
                "👑",
                fontSize = 22.sp,
                modifier = Modifier
                    .padding(bottom = 2.dp)
                    .graphicsLayer {
                        scaleX = crown
                        scaleY = crown
                        alpha = crown
                    },
            )
        }
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = Color.Transparent,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                (medal ?: MaterialTheme.colorScheme.surfaceVariant).copy(alpha = if (isChampion) 0.35f else 0.20f),
                                MaterialTheme.colorScheme.surfaceContainer,
                            ),
                        ),
                    )
                    .border(
                        width = if (isChampion) 1.5.dp else 1.dp,
                        color = (medal ?: MaterialTheme.colorScheme.outlineVariant).copy(alpha = 0.55f),
                        shape = RoundedCornerShape(22.dp),
                    )
                    .padding(vertical = 14.dp, horizontal = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                PlayerAvatar(
                    name = player.name,
                    size = if (isChampion) 64.dp else 52.dp,
                    ringColor = medal,
                    ringWidth = if (isChampion) 2.5.dp else 2.dp,
                )
                Text(
                    player.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = if (isChampion) FontWeight.Black else FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    formatScore(player.score),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = medal ?: MaterialTheme.colorScheme.onSurface,
                )
                PillChip(
                    text = "#$position",
                    color = medal ?: MaterialTheme.colorScheme.onSurfaceVariant,
                    containerAlpha = 0.16f,
                )
            }
        }
    }
}

/** Eine Zeile der Rangliste: Rang, Kopf, Punkte, Trend und Fortschritt zur Spitze. */
@Composable
private fun PlayerRow(
    player: PlayerStat,
    position: Int,
    leaderScore: Int,
    isMe: Boolean,
    animationsEnabled: Boolean,
) {
    val medal = medalColor(position)
    Reveal(
        visible = animationsEnabled,
        fromScale = 0.97f,
        fromOffsetY = 10.dp,
    ) {
        SoftCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = if (isMe) {
                listOf(
                    MaterialTheme.colorScheme.primaryContainer,
                    MaterialTheme.colorScheme.surfaceContainerHigh,
                )
            } else {
                listOf(
                    MaterialTheme.colorScheme.surfaceContainerLow,
                    MaterialTheme.colorScheme.surfaceContainer,
                )
            },
            contentPadding = 12.dp,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            (medal ?: MaterialTheme.colorScheme.surfaceVariant).copy(alpha = if (medal != null) 0.25f else 1f),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (medal != null) {
                        Icon(
                            Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = medal,
                            modifier = Modifier.size(17.dp),
                        )
                    } else {
                        Text(
                            "$position",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                PlayerAvatar(
                    name = player.name,
                    size = 42.dp,
                    ringColor = if (isMe) MaterialTheme.colorScheme.primary else null,
                )
                Column(Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            player.name,
                            fontWeight = if (isMe) FontWeight.Bold else FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (isMe) {
                            PillChip(text = "DU", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    TrendChip(player)
                }
                Text(
                    formatScore(player.score),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                )
            }
            if (leaderScore > 0) {
                val fraction = (player.score / leaderScore.coerceAtLeast(1).toFloat()).coerceIn(0.03f, 1f)
                GradientProgressBar(
                    fraction = fraction,
                    color = medal ?: MaterialTheme.colorScheme.primary,
                    track = MaterialTheme.colorScheme.onSurface,
                    height = 4.dp,
                    animate = animationsEnabled,
                )
            }
        }
    }
}

/** Trend-Chip: ↑ Zuwachs, ↓ Verlust, NEU für neue Einträge. */
@Composable
internal fun TrendChip(player: PlayerStat) {
    val previous = player.previousScore
    if (previous == null && player.score <= 0) return
    if (previous == player.score) return

    val trend = when {
        previous == null -> Triple(McAffenBrand.colors.positive, "NEU", Icons.AutoMirrored.Filled.TrendingUp)
        player.score > previous ->
            Triple(McAffenBrand.colors.positive, "+${formatScore(player.score - previous)}", Icons.AutoMirrored.Filled.TrendingUp)
        else ->
            Triple(McAffenBrand.colors.negative, "−${formatScore(previous - player.score)}", Icons.AutoMirrored.Filled.TrendingDown)
    }
    PillChip(
        text = trend.second,
        color = trend.first,
        icon = {
            Icon(
                trend.third,
                contentDescription = null,
                tint = trend.first,
                modifier = Modifier.size(12.dp),
            )
        },
    )
}

/** Einheitlicher Leerzustand mit Emoji, Titel und Erklärung. */
@Composable
internal fun EmptyState(
    emoji: String,
    title: String,
    text: String,
    modifier: Modifier = Modifier,
) {
    // Sanft schwebendes Emoji: macht leere Zustände lebendig statt tot.
    val float = rememberInfiniteTransition(label = "emptyFloat")
    val bob by float.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = androidx.compose.animation.core.FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "emptyBob",
    )
    Column(
        modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            emoji,
            fontSize = 40.sp,
            modifier = Modifier.offset(y = bob.dp),
        )
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
        Text(
            text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

/** Werte der Rangliste – entspricht den Sortierungen von `/ndu/top`. */
internal data class BoardSort(val key: String, val label: String, val unit: String, val emoji: String)

internal val BOARD_SORTS = listOf(
    BoardSort("waves", "Wellen", "Wellen", "🌊"),
    BoardSort("kills", "Kills", "Kills", "⚔️"),
    BoardSort("games", "Spiele", "Spiele", "🎮"),
    BoardSort("coins", "Coins", "Coins", "🪙"),
)

/** Segment-Umschalter für die Ranglisten-Werte mit gleitendem Hintergrund. */
@Composable
private fun BoardSortRow(selected: String, onSelect: (String) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        BOARD_SORTS.forEach { sort ->
            val active = sort.key == selected
            val background by animateColorAsState(
                targetValue = if (active) MaterialTheme.colorScheme.primary else Color.Transparent,
                animationSpec = tween(260),
                label = "sortBackground",
            )
            val textColor by animateColorAsState(
                targetValue = if (active) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                animationSpec = tween(260),
                label = "sortText",
            )
            Box(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(background)
                    .clickable { onSelect(sort.key) }
                    .padding(vertical = 9.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(sort.emoji, fontSize = 13.sp)
                    Text(
                        sort.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                        color = textColor,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}
