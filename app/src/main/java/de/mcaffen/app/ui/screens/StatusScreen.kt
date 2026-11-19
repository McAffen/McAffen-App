@file:OptIn(ExperimentalMaterial3Api::class)

package de.mcaffen.app.ui

import android.content.Intent
import android.net.Uri
import de.mcaffen.app.ui.LocalStrings
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Lan
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.mcaffen.app.data.SettingsStore
import de.mcaffen.app.model.ServerInfo
import de.mcaffen.app.model.ServerStatus
import de.mcaffen.app.ui.McAffenBrand
import de.mcaffen.app.ui.cleanColorCodes
import de.mcaffen.app.data.LanBridge
import de.mcaffen.app.ui.components.AnimatedBackdrop
import de.mcaffen.app.ui.components.SoftDivider
import de.mcaffen.app.ui.components.GlowHalo
import de.mcaffen.app.ui.components.LiveDot
import de.mcaffen.app.ui.components.PillChip
import de.mcaffen.app.ui.components.Reveal
import de.mcaffen.app.ui.components.SectionTitle
import de.mcaffen.app.ui.components.SoftCard
import de.mcaffen.app.ui.components.StatusDot
import de.mcaffen.app.ui.formatScore
import kotlinx.coroutines.delay

/**
 * Live-Status: Hero mit Spielerzahl, Tagesrekord, Serverliste, IP-Karte,
 * Community-Links und Meldungen.
 */
@Composable
internal fun StatusScreen(
    status: ServerStatus?,
    settings: SettingsStore,
    loading: Boolean,
    animationsEnabled: Boolean,
    onRefresh: () -> Unit,
    /** Spielmodi (aus /api/spielmodi) – werden hier mit angezeigt. */
    modes: List<de.mcaffen.app.network.GameModeDto> = emptyList(),
    modesLoading: Boolean = false,
    onLoadModes: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val strings = LocalStrings.current
    var todayPeak by remember { mutableIntStateOf(settings.peakToday()) }
    LaunchedEffect(status) {
        status?.let { todayPeak = settings.touchPeakToday(it.onlinePlayers) }
    }
    val animatedPlayers by animateIntAsState(
        targetValue = status?.onlinePlayers ?: 0,
        animationSpec = tween(durationMillis = 900),
        label = "onlinePlayers",
    )
    val onlineServers = status?.servers?.count { it.online } ?: 0
    val haptic = LocalHapticFeedback.current
    // Modi einmalig nachladen, wenn der Status geöffnet wird.
    LaunchedEffect(Unit) { if (modes.isEmpty()) onLoadModes() }

    Box(modifier.fillMaxSize()) {
        AnimatedBackdrop(enabled = animationsEnabled)
        PullToRefreshBox(isRefreshing = loading, onRefresh = onRefresh, modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (status == null) {
                    item("empty") {
                        EmptyState(
                            emoji = "📡",
                            title = LocalStrings.current["status.empty.title"],
                            text = LocalStrings.current["status.empty.text"],
                        )
                    }
                } else {
                    item("hero") {
                        Reveal(visible = animationsEnabled) {
                            StatusHero(
                                players = animatedPlayers,
                                servers = status.networkServers,
                                onlineServers = onlineServers,
                                peak = todayPeak,
                                animationsEnabled = animationsEnabled,
                            )
                        }
                    }
                    if (status.servers.isNotEmpty()) {
                        item("servers") {
                            Reveal(visible = animationsEnabled) {
                                SoftCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(24.dp),
                                    contentPadding = 16.dp,
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    SectionTitle(LocalStrings.current["status.servers"]) {
                                        PillChip(
                                            text = "$onlineServers online",
                                            color = McAffenBrand.colors.positive,
                                        )
                                    }
                                    status.servers.forEach { server ->
                                        ServerRow(server)
                                    }
                                }
                            }
                        }
                    }
                    item("ip") {
                        Reveal(visible = animationsEnabled) { ServerAddressCard() }
                    }
                    item("lan") {
                        Reveal(visible = animationsEnabled, fromScale = 0.97f) {
                            LanCard(
                                animationsEnabled = animationsEnabled,
                                onStarted = { haptic.performHapticFeedback(HapticFeedbackType.LongPress) },
                            )
                        }
                    }
                    // Spielmodi gehören hierhin: sie zeigen, wo man startet.
                    if (modes.isNotEmpty() || modesLoading) {
                        item("modesHeader") {
                            SectionTitle(LocalStrings.current["status.modes"]) {
                                PillChip(
                                    text = if (modesLoading) "…" else "${modes.size}",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    containerAlpha = 0.10f,
                                )
                            }
                        }
                        if (modesLoading && modes.isEmpty()) {
                            item("modesLoading") {
                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                                }
                            }
                        }
                        items(modes, key = { it.name }) { mode ->
                            Reveal(visible = animationsEnabled, fromScale = 0.97f) {
                                ModeRow(mode)
                            }
                        }
                    }
                    item("community") {
                        Reveal(visible = animationsEnabled) {
                            CommunityLinksCard(
                                onOpen = { url ->
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                },
                                onShare = {
                                    val share = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(
                                            Intent.EXTRA_TEXT,
                                            strings["status.shareText"],
                                        )
                                    }
                                    context.startActivity(
                                        Intent.createChooser(share, strings["status.shareChooser"]),
                                    )
                                },
                            )
                        }
                    }
                    val messages = status.messages.filter(String::isNotBlank)
                    item("newsHeader") {
                        SectionTitle(LocalStrings.current["status.messages"]) {
                            PillChip(
                                text = "${messages.size}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                containerAlpha = 0.10f,
                            )
                        }
                    }
                    if (messages.isEmpty()) {
                        item("newsEmpty") {
                            EmptyState(
                                emoji = "📣",
                                title = LocalStrings.current["status.messages.empty.title"],
                                text = LocalStrings.current["status.messages.empty.text"],
                            )
                        }
                    } else {
                        items(messages) { message ->
                            Reveal(visible = animationsEnabled, fromScale = 0.97f) {
                                MessageCard(message)
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Hero mit Live-Kennzahl und Tagesrekord. */
@Composable
private fun StatusHero(
    players: Int,
    servers: Int,
    onlineServers: Int,
    peak: Int,
    animationsEnabled: Boolean,
) {
    Box(Modifier.fillMaxWidth()) {
        SoftCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            colors = McAffenBrand.colors.brandGradient.map { it.copy(alpha = 0.92f) },
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
                    LocalStrings.current["status.network"],
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.9f),
                )
            }
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    formatScore(players),
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                )
                Text(
                    LocalStrings.current["status.playersOnline"],
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.padding(bottom = 10.dp),
                )
            }
            Text(
                formatString(
                    "status.reachable",
                    onlineServers,
                    servers,
                    formatScore(peak),
                ),
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

@Composable
private fun ServerRow(server: ServerInfo) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StatusDot(online = server.online)
        Column(Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(server.name, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                if (server.type.equals("proxy", ignoreCase = true)) {
                    PillChip(
                        text = "PROXY",
                        color = MaterialTheme.colorScheme.secondary,
                        containerAlpha = 0.16f,
                    )
                }
            }
            server.version?.let { version ->
                val cleaned = cleanColorCodes(version)
                if (cleaned.isNotBlank()) {
                    val maintenance = version.contains("Wartung", ignoreCase = true)
                    Text(
                        cleaned,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (maintenance) McAffenBrand.colors.negative else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                formatScore(server.players),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Black,
            )
            Text(
                LocalStrings.current["common.player"],
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ServerAddressCard() {
    val clipboard = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }
    LaunchedEffect(copied) {
        if (copied) {
            delay(2000)
            copied = false
        }
    }
    SoftCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        contentPadding = 16.dp,
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("🎮", fontSize = 20.sp)
                }
                Column {
                    Text(
                        LocalStrings.current["status.javaIp"],
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        "mcaffen.de",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                    )
                }
            }
            FilledTonalButton(
                onClick = {
                    clipboard.setText(AnnotatedString("mcaffen.de"))
                    copied = true
                },
                shape = RoundedCornerShape(14.dp),
            ) {
                Icon(
                    if (copied) Icons.Default.Refresh else Icons.Default.ContentCopy,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    if (copied) LocalStrings.current["common.copied"] else LocalStrings.current["common.copy"],
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
        SoftDivider()
        // Bedrock: gleicher Server, andere Edition – Adresse direkt daneben.
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    LocalStrings.current["status.bedrockIp"],
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    "${LanBridge.HOST}:${LanBridge.PORT}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Black,
                )
            }
            FilledTonalButton(
                onClick = {
                    clipboard.setText(AnnotatedString("${LanBridge.HOST}:${LanBridge.PORT}"))
                    copied = true
                },
                shape = RoundedCornerShape(14.dp),
            ) {
                Icon(
                    if (copied) Icons.Default.Refresh else Icons.Default.ContentCopy,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    if (copied) LocalStrings.current["common.copied"] else LocalStrings.current["common.copy"],
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun CommunityLinksCard(onOpen: (String) -> Unit, onShare: () -> Unit) {
    SoftCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        contentPadding = 16.dp,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SectionTitle(LocalStrings.current["status.community"])
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilledTonalButton(
                onClick = { onOpen("https://mcaffen.de") },
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            ) { Text("Website") }
            FilledTonalButton(
                onClick = { onOpen("https://discord.com/invite/tpyHQqBzVD") },
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            ) { Text("Discord") }
            FilledTonalButton(
                onClick = { onOpen("https://www.youtube.com/@McAffenofficial") },
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            ) { Text("YouTube") }
        }
        Button(
            onClick = onShare,
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp),
            shape = RoundedCornerShape(14.dp),
        ) {
            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(17.dp))
            Text(LocalStrings.current["status.share"], fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun MessageCard(message: String) {
    SoftCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        contentPadding = 14.dp,
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center,
            ) {
                Text("📣", fontSize = 16.sp)
            }
            Text(
                message,
                Modifier.padding(start = 12.dp),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

/**
 * LAN-Welt für Konsolen, direkt im Status: zeigt die Adressen dieses Geräts und
 * erklärt in vier Schritten, wie bedrock.mcaffen.de auf PS5, Switch & Co.
 * als LAN-Welt erscheint.
 */
@Composable
private fun LanCard(animationsEnabled: Boolean, onStarted: () -> Unit = {}) {
    val context = LocalContext.current
    val strings = LocalStrings.current
    // Bei jedem Zurückkehren in die App neu prüfen: WLAN kann sich ändern,
    // remember allein würde den alten Wert (z. B. "nicht im WLAN") festhalten.
    var netTick by remember { mutableStateOf(0) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) netTick++
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    val localIp = remember(netTick) { LanBridge.localAddress(context) }
    val onWifi = remember(netTick) { LanBridge.isOnWifi(context) }
    var expanded by remember { mutableStateOf(false) }
    var running by remember { mutableStateOf(LanBridge.isRunning()) }
    var error by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        onDispose { LanBridge.stop() }
    }

    SoftCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        contentPadding = 16.dp,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        SectionTitle(LocalStrings.current["status.lan.title"]) {
            PillChip(
                text = if (onWifi) LocalStrings.current["status.lan.onWifi"] else LocalStrings.current["status.lan.noWifi"],
                color = if (onWifi) McAffenBrand.colors.positive else McAffenBrand.colors.negative,
            )
        }
        Text(
            LocalStrings.current["status.lan.intro"],
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            AddressPill(
                label = LocalStrings.current["status.lan.target"],
                value = "${LanBridge.HOST}:${LanBridge.PORT}",
                modifier = Modifier.weight(1f),
            )
            AddressPill(
                label = LocalStrings.current["status.lan.thisDevice"],
                value = localIp ?: LocalStrings.current["status.lan.notInWifi"],
                modifier = Modifier.weight(1f),
            )
        }
        // Startet oder beendet die lokale UDP-Brücke.
        Button(
            onClick = {
                if (running) {
                    LanBridge.stop()
                    running = false
                } else if (!onWifi) {
                    error = strings["status.lan.errorWifi"]
                } else {
                    LanBridge.start(context).onSuccess {
                        running = true
                        expanded = true
                        error = null
                        onStarted()
                    }.onFailure {
                        running = false
                        error = strings.format(
                            "status.lan.errorStart",
                            it.message ?: strings.format("status.lan.portBusy", LanBridge.PORT),
                        )
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(14.dp),
        ) {
            Icon(
                if (running) Icons.Default.Stop else Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Text(
                if (running) LocalStrings.current["status.lan.stop"] else LocalStrings.current["status.lan.start"],
                fontWeight = FontWeight.Bold,
            )
        }
        error?.let {
            Text(
                it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
        AnimatedVisibility(visible = expanded) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LanBridge.steps(localIp, LocalStrings.current.language).forEachIndexed { index, step ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "${index + 1}.",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Column {
                            Text(
                                step.title,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                step.detail,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                Text(
                    LocalStrings.current["status.lan.footnote"],
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** Kompakte Adressanzeige mit Beschriftung. */
@Composable
private fun AddressPill(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
        )
    }
}

/** Ein Spielmodus als kompakte Zeile im Status. */
@Composable
private fun ModeRow(mode: de.mcaffen.app.network.GameModeDto) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.55f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        mode.image?.let { image ->
            AsyncImage(
                model = image,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp)),
            )
        }
        Column(Modifier.weight(1f)) {
            Text(
                mode.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            (mode.subtitle ?: mode.description)?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                )
            }
        }
        mode.statusLabel?.let { label ->
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            )
        }
    }
}
