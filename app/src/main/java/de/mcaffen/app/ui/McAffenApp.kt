package de.mcaffen.app.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import de.mcaffen.app.BuildConfig
import de.mcaffen.app.R
import de.mcaffen.app.data.McAffenRepository
import de.mcaffen.app.data.SettingsStore
import de.mcaffen.app.model.AppRelease
import de.mcaffen.app.model.PlayerStat
import de.mcaffen.app.model.ServerStatus
import de.mcaffen.app.model.VoteSite
import de.mcaffen.app.ui.ThemeOptions
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private enum class Tab(val title: String) {
    LEADERBOARD("Rangliste"),
    VOTES("Voten"),
    STATUS("Status"),
    SETTINGS("Einstellungen"),
}

class McAffenViewModel(private val repository: McAffenRepository) : ViewModel() {
    val players = repository.players
    val voteSites = repository.voteSites
    val status = repository.status
    val legalCache = repository.legalCache
    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()
    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()
    private val _update = MutableStateFlow<AppRelease?>(null)
    val update = _update.asStateFlow()

    fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            _loading.value = true
            _error.value = null
            runCatching { repository.refresh() }
                .onFailure { _error.value = "Daten konnten nicht geladen werden. Cache wird angezeigt." }
            _loading.value = false
            runCatching { repository.refreshLegal() }
        }
    }

    fun checkForUpdate() {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { repository.latestRelease() }
                .onSuccess { release ->
                    if (isNewerVersion(release.tagName, BuildConfig.VERSION_NAME)) {
                        _update.value = release
                    }
                }
        }
    }

    fun dismissUpdate() {
        _update.value = null
    }

    private fun isNewerVersion(candidate: String, current: String): Boolean {
        fun parts(value: String) = value.trim().removePrefix("v").split('.')
            .map { it.takeWhile(Char::isDigit).toIntOrNull() ?: 0 }
        val newer = parts(candidate)
        val installed = parts(current)
        for (index in 0 until maxOf(newer.size, installed.size)) {
            val candidatePart = newer.getOrElse(index) { 0 }
            val currentPart = installed.getOrElse(index) { 0 }
            if (candidatePart != currentPart) return candidatePart > currentPart
        }
        return false
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun McAffenApp(repository: McAffenRepository, settings: SettingsStore) {
    val context = LocalContext.current
    val vm: McAffenViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                McAffenViewModel(repository) as T
        },
    )
    val players by vm.players.collectAsStateWithLifecycle(emptyList())
    val voteSites by vm.voteSites.collectAsStateWithLifecycle(emptyList())
    val status by vm.status.collectAsStateWithLifecycle(null)
    val loading by vm.loading.collectAsStateWithLifecycle()
    val error by vm.error.collectAsStateWithLifecycle()
    val update by vm.update.collectAsStateWithLifecycle()
    val legalCache by vm.legalCache.collectAsStateWithLifecycle(emptyMap())
    val myName by settings.playerName.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(Tab.LEADERBOARD) }
    var isMenuExpanded by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }
    var legalKey by remember { mutableStateOf<String?>(null) }
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { vm.refresh() }
    LaunchedEffect(Unit) { vm.checkForUpdate() }
    LaunchedEffect(error) { error?.let { snackbar.showSnackbar(it) } }

    update?.let { release ->
        AlertDialog(
            onDismissRequest = vm::dismissUpdate,
            title = { Text("Neues Update verfügbar") },
            text = { Text(release.name?.takeIf(String::isNotBlank) ?: "Version ${release.tagName} ist verfügbar.") },
            confirmButton = {
                Button(onClick = {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(release.assetUrl ?: release.htmlUrl)))
                    vm.dismissUpdate()
                }) { Text("Update ansehen") }
            },
            dismissButton = {
                Button(onClick = vm::dismissUpdate) { Text("Später") }
            },
        )
    }

    if (showAbout) {
        AlertDialog(
            onDismissRequest = { showAbout = false },
            title = { Text("Über McAffen-App") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Inoffizielle App für den Minecraft-Server mcaffen.de")
                    Text("Version ${BuildConfig.VERSION_NAME}")
                }
            },
            confirmButton = {
                Column {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { showAbout = false; legalKey = "impressum" }) { Text("Impressum") }
                        TextButton(onClick = { showAbout = false; legalKey = "datenschutz" }) { Text("Datenschutz") }
                    }
                    Button(onClick = { showAbout = false }, modifier = Modifier.fillMaxWidth()) { Text("Schließen") }
                }
            },
        )
    }

    legalKey?.let { key ->
        val cached = legalCache[key]
        legalDocument(key, cached?.title, cached?.html)
    }?.let { document ->
        LegalDialog(document = document, onDismiss = { legalKey = null })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box {
                            Image(
                                painter = painterResource(R.drawable.mcaffen_logo),
                                contentDescription = "McAffen",
                                modifier = Modifier
                                    .size(32.dp)
                                    .clickable { isMenuExpanded = !isMenuExpanded },
                            )
                            DropdownMenu(
                                expanded = isMenuExpanded,
                                onDismissRequest = { isMenuExpanded = false },
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Einstellungen") },
                                    onClick = {
                                        isMenuExpanded = false
                                        selectedTab = Tab.SETTINGS
                                    },
                                )
                                DropdownMenuItem(
                                    text = { Text("Über") },
                                    onClick = {
                                        isMenuExpanded = false
                                        showAbout = true
                                    },
                                )
                            }
                        }
                        Text(selectedTab.title, Modifier.padding(start = 8.dp))
                    }
                },
                actions = {
                    IconButton(onClick = vm::refresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "Aktualisieren")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            NavigationBar {
                Tab.entries.filter { it != Tab.SETTINGS }.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = {
                            Icon(
                                when (tab) {
                                    Tab.LEADERBOARD -> Icons.Default.EmojiEvents
                                    Tab.VOTES -> Icons.Default.HowToVote
                                    Tab.STATUS -> Icons.Default.Speed
                                    Tab.SETTINGS -> Icons.Default.Settings
                                },
                                contentDescription = tab.title,
                            )
                        },
                        label = { Text(tab.title) },
                    )
                }
            }
        },
    ) { padding ->
        if (loading && players.isEmpty() && voteSites.isEmpty() && status == null) {
            LoadingState(Modifier.padding(padding))
        } else {
            when (selectedTab) {
                Tab.LEADERBOARD -> LeaderboardScreen(
                    players = players,
                    myName = myName,
                    loading = loading,
                    onRefresh = vm::refresh,
                    modifier = Modifier.padding(padding),
                )
                Tab.VOTES -> VoteScreen(
                    players = players,
                    voteSites = voteSites,
                    storedName = myName,
                    onStoreName = settings::setPlayerName,
                    loading = loading,
                    onRefresh = vm::refresh,
                    modifier = Modifier.padding(padding),
                )
                Tab.STATUS -> StatusScreen(
                    status = status,
                    loading = loading,
                    onRefresh = vm::refresh,
                    modifier = Modifier.padding(padding),
                )
                Tab.SETTINGS -> SettingsScreen(
                    version = BuildConfig.VERSION_NAME,
                    darkMode = settings.darkMode.collectAsStateWithLifecycle().value,
                    onDarkModeChange = settings::setDarkMode,
                    color = settings.color.collectAsStateWithLifecycle().value,
                    onColorChange = settings::setColor,
                    onRefresh = vm::refresh,
                    onOpenLegal = { legalKey = it },
                    playerName = myName,
                    onPlayerNameChange = settings::setPlayerName,
                    legalLastSync = legalCache.values.maxOfOrNull { it.updatedAt } ?: 0L,
                    legalCached = legalCache.isNotEmpty(),
                    modifier = Modifier.padding(padding),
                )
            }
        }
    }
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()
        Text("Lade McAffen-Daten …", Modifier.padding(top = 12.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LeaderboardScreen(
    players: List<PlayerStat>,
    myName: String,
    loading: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by remember { mutableStateOf("") }
    val filtered = players.filter { it.name.contains(query, ignoreCase = true) }
    PullToRefreshBox(isRefreshing = loading, onRefresh = onRefresh, modifier = modifier) {
        Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                label = { Text("Spieler suchen") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
            )
            if (players.isEmpty()) {
                EmptyHint("Noch keine Ranglistendaten verfügbar.", Modifier.padding(top = 20.dp))
            } else if (filtered.isEmpty()) {
                EmptyHint("Kein Spieler gefunden.", Modifier.padding(top = 20.dp))
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(filtered, key = { it.name }) { player ->
                        val position = players.indexOfFirst { it.name == player.name } + 1
                        val isMe = myName.isNotBlank() && player.name.equals(myName, ignoreCase = true)
                        Card(
                            Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isMe) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                },
                            ),
                        ) {
                            Row(
                                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    RankBadge(position)
                                    Column {
                                        Text(player.name, fontWeight = FontWeight.Medium)
                                        if (isMe) {
                                            Text(
                                                "Du",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary,
                                            )
                                        }
                                    }
                                }
                                Text(
                                    "${formatScore(player.score)} Punkte",
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RankBadge(position: Int) {
    val medalColor = when (position) {
        1 -> Color(0xFFFFB300)
        2 -> Color(0xFFB0BEC5)
        3 -> Color(0xFFB87333)
        else -> null
    }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        medalColor?.let { color ->
            Icon(
                Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = color,
            )
        }
        Text(
            "#$position",
            fontWeight = FontWeight.Bold,
            color = medalColor ?: MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun formatScore(score: Int): String =
    NumberFormat.getIntegerInstance(Locale.GERMANY).format(score)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VoteScreen(
    players: List<PlayerStat>,
    voteSites: List<VoteSite>,
    storedName: String,
    onStoreName: (String) -> Unit,
    loading: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(storedName) }
    var searchedName by remember { mutableStateOf<String?>(null) }
    val player = players.firstOrNull { it.name.equals(searchedName, ignoreCase = true) }
    val canVote = player != null && name.trim().equals(searchedName, ignoreCase = true)
    val selectedPlayerName = player?.name
    val canSearch = name.isNotBlank() && players.isNotEmpty()

    PullToRefreshBox(isRefreshing = loading, onRefresh = onRefresh, modifier = modifier) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Dein Minecraft-Name", style = MaterialTheme.typography.titleMedium)
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
                supportingText = {
                    Text("Dein Name wird gespeichert, damit du ihn nicht jedes Mal neu eingeben musst.")
                },
            )
            Button(
                onClick = { searchedName = name.trim().takeIf(String::isNotEmpty) },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank() && players.isNotEmpty(),
            ) {
                Icon(Icons.Default.Search, contentDescription = null)
                Text("  Suchen")
            }
            when {
                searchedName != null && player != null ->
                    Text("Spieler gefunden. Du kannst jetzt voten.")
                searchedName != null ->
                    Text("Spieler nicht gefunden. Prüfe die Schreibweise.")
                !canSearch ->
                    Text(
                        "Gib deinen Minecraft-Namen ein, um voten zu können.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
            }
            Text("Für jede Abstimmung erhältst du Belohnungen wie Vote-Schlüssel und Coins.")
            if (voteSites.isEmpty()) {
                EmptyHint("Keine Vote-Seiten verfügbar.")
            } else {
            voteSites.forEach { site ->
                val voteUri = selectedPlayerName?.let { playerName ->
                    site.url.toVoteUri(playerName)
                }
                Card(
                    Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(site.site, fontWeight = FontWeight.Medium)
                            Text(
                                "Vote-Schlüssel & Coins",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Button(
                            onClick = { voteUri?.let { context.startActivity(Intent(Intent.ACTION_VIEW, it)) } },
                            enabled = canVote && voteUri != null,
                        ) {
                            Text("Voten")
                            Icon(
                                Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = null,
                                Modifier.padding(start = 4.dp).size(16.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}
}

/** Ersetzt {user}-Platzhalter in Vote-URLs durch den Spielernamen und prüft auf HTTPS. */
private fun String.toVoteUri(playerName: String): Uri? {
    val placeholderRegex = Regex("\\{user(name)?\\}", RegexOption.IGNORE_CASE)
    val encoded = Uri.encode(playerName)
    val replaced = replace(placeholderRegex, encoded)
    val hasPlaceholder = placeholderRegex.containsMatchIn(this)
    var uri = runCatching { Uri.parse(replaced.trim()) }.getOrNull() ?: return null
    if (uri.scheme == "http") {
        uri = uri.buildUpon().scheme("https").build()
    }
    val valid = uri.scheme.equals("https", ignoreCase = true) && !uri.host.isNullOrBlank()
    if (!valid) return null
    // Fallback für Seiten ohne Platzhalter: Namen als Query-Parameter anhängen.
    if (!hasPlaceholder && playerName.isNotBlank()) {
        uri = uri.buildUpon().appendQueryParameter("username", encoded).build()
    }
    return uri
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatusScreen(
    status: ServerStatus?,
    loading: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    PullToRefreshBox(isRefreshing = loading, onRefresh = onRefresh, modifier = modifier) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (status == null) {
                EmptyHint("Noch keine Statusdaten verfügbar.")
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatusCard(
                        Modifier.weight(1f),
                        icon = Icons.Default.Group,
                        value = formatScore(status.onlinePlayers),
                        label = "Spieler online",
                    )
                    StatusCard(
                        Modifier.weight(1f),
                        icon = Icons.Default.Dns,
                        value = status.networkServers.toString(),
                        label = "Netzwerk-Server",
                    )
                }
            }
            ServerAddressCard()
            CommunityLinksCard(
                onOpen = { url -> context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) },
            )
            if (status != null) {
                Text("Aktuelle Meldungen", style = MaterialTheme.typography.titleMedium)
                if (status.messages.filter(String::isNotBlank).isEmpty()) {
                    EmptyHint("Keine Meldungen.")
                }
                status.messages.filter(String::isNotBlank).forEach { message ->
                    Card(Modifier.fillMaxWidth()) {
                        Row(
                            Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                Icons.Default.Campaign,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                            Text(message, Modifier.padding(start = 12.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ServerAddressCard() {
    val clipboard = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }
    LaunchedEffect(copied) {
        if (copied) {
            kotlinx.coroutines.delay(2000)
            copied = false
        }
    }
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(
                    Icons.Default.SportsEsports,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Column {
                    Text(
                        "Java-Edition",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Text(
                        "mcaffen.de",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            Button(onClick = {
                clipboard.setText(AnnotatedString("mcaffen.de"))
                copied = true
            }) {
                Icon(Icons.Default.ContentCopy, contentDescription = null)
                Text(if (copied) " Kopiert" else " Kopieren")
            }
        }
    }
}

@Composable
private fun CommunityLinksCard(onOpen: (String) -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Community", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { onOpen("https://mcaffen.de") }) { Text("Website") }
                TextButton(onClick = { onOpen("https://discord.com/invite/tpyHQqBzVD") }) { Text("Discord") }
                TextButton(onClick = { onOpen("https://www.youtube.com/@McAffenofficial") }) { Text("YouTube") }
            }
        }
    }
}

@Composable
private fun StatusCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
) {
    Card(
        modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SettingsScreen(
    version: String,
    darkMode: String,
    onDarkModeChange: (String) -> Unit,
    color: String,
    onColorChange: (String) -> Unit,
    onRefresh: () -> Unit,
    onOpenLegal: (String) -> Unit,
    playerName: String,
    onPlayerNameChange: (String) -> Unit,
    legalLastSync: Long,
    legalCached: Boolean,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            "Einstellungen",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Card {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Darstellung", style = MaterialTheme.typography.titleMedium)
                Text("Dark-Mode", style = MaterialTheme.typography.titleSmall)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemeOptions.darkModes.forEach { (value, label) ->
                        FilterChip(
                            selected = darkMode == value,
                            onClick = { onDarkModeChange(value) },
                            label = { Text(label) },
                        )
                    }
                }
                Text("Farbschema", style = MaterialTheme.typography.titleSmall)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemeOptions.colors.forEach { (value, label) ->
                        FilterChip(
                            selected = color == value,
                            onClick = { onColorChange(value) },
                            label = { Text(label) },
                        )
                    }
                }
            }
        }
        Card {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Profil", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = playerName,
                    onValueChange = onPlayerNameChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Dein Minecraft-Name") },
                    supportingText = {
                        Text("Wird auf der Vote-Seite vorausgefüllt und hebt dich in der Rangliste hervor.")
                    },
                )
            }
        }
        Card {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("App", style = MaterialTheme.typography.titleMedium)
                Text("Version: $version")
                Text("Server: mcaffen.de")
                Text("API: https://mcaffen.de/api")
            }
        }
        Card {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Aktionen", style = MaterialTheme.typography.titleMedium)
                Button(onClick = onRefresh, modifier = Modifier.fillMaxWidth()) {
                    Text("Daten aktualisieren")
                }
                OutlinedButton(
                    onClick = {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/McAffen/McAffen-App")),
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Auf GitHub öffnen")
                }
            }
        }
        Card {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Rechtliches", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Rechtstexte werden automatisch mit mcaffen.de synchronisiert." +
                        if (legalCached) {
                            val formatter = SimpleDateFormat("dd.MM.yyyy, HH:mm", Locale.getDefault())
                            " Zuletzt: ${formatter.format(Date(legalLastSync))} Uhr."
                        } else {
                            " Aktuell werden die integrierten Texte angezeigt."
                        },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedButton(onClick = { onOpenLegal("impressum") }, modifier = Modifier.fillMaxWidth()) {
                    Text("Impressum")
                }
                OutlinedButton(onClick = { onOpenLegal("datenschutz") }, modifier = Modifier.fillMaxWidth()) {
                    Text("Datenschutzerklärung")
                }
                OutlinedButton(onClick = { onOpenLegal("agb") }, modifier = Modifier.fillMaxWidth()) {
                    Text("Allgemeine Geschäftsbedingungen (AGB)")
                }
            }
        }
    }
}

@Composable
private fun EmptyHint(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        modifier,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
