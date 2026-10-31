package de.mcaffen.app.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.res.painterResource
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
import de.mcaffen.app.model.AppRelease
import de.mcaffen.app.model.PlayerStat
import de.mcaffen.app.model.ServerStatus
import de.mcaffen.app.model.VoteSite
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun McAffenApp(repository: McAffenRepository) {
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
    var selectedTab by remember { mutableStateOf(Tab.LEADERBOARD) }
    var isMenuExpanded by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }
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
                Button(onClick = { showAbout = false }) { Text("Schließen") }
            },
        )
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
                Tab.LEADERBOARD -> LeaderboardScreen(players, Modifier.padding(padding))
                Tab.VOTES -> VoteScreen(players, voteSites, Modifier.padding(padding))
                Tab.STATUS -> StatusScreen(status, Modifier.padding(padding))
                Tab.SETTINGS -> SettingsScreen(
                    version = BuildConfig.VERSION_NAME,
                    onRefresh = vm::refresh,
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

@Composable
private fun LeaderboardScreen(players: List<PlayerStat>, modifier: Modifier = Modifier) {
    var query by remember { mutableStateOf("") }
    val filtered = players.filter { it.name.contains(query, ignoreCase = true) }
    Column(modifier.fillMaxSize().padding(horizontal = 16.dp)) {
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
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    "#$position",
                                    fontWeight = FontWeight.Bold,
                                    color = when (position) {
                                        1 -> Color(0xFFFFD54F)
                                        2 -> Color(0xFFCFD8DC)
                                        3 -> Color(0xFFBC8546)
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                )
                                Text(player.name, fontWeight = FontWeight.Medium)
                            }
                            Text("${player.score} Punkte")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VoteScreen(
    players: List<PlayerStat>,
    voteSites: List<VoteSite>,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var searchedName by remember { mutableStateOf<String?>(null) }
    val player = players.firstOrNull { it.name.equals(searchedName, ignoreCase = true) }
    val canVote = player != null && name.trim().equals(searchedName, ignoreCase = true)
    val selectedPlayerName = player?.name

    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Dein Minecraft-Name", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
                searchedName = null
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("Spielername") },
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
        }
        Text("Für jede Abstimmung erhältst du Belohnungen wie Vote-Schlüssel und Coins.")
        if (voteSites.isEmpty()) {
            EmptyHint("Keine Vote-Seiten verfügbar.")
        } else {
            voteSites.forEach { site ->
                val voteUri = selectedPlayerName?.let { playerName ->
                    site.url.toHttpsUri()?.buildUpon()
                        ?.appendQueryParameter("username", playerName)
                        ?.build()
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
                                Icons.Default.OpenInNew,
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

private fun String.toHttpsUri(): Uri? {
    val uri = runCatching { Uri.parse(trim()) }.getOrNull() ?: return null
    return uri.takeIf { it.scheme.equals("https", ignoreCase = true) && !it.host.isNullOrBlank() }
}

@Composable
private fun StatusScreen(status: ServerStatus?, modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (status == null) {
            EmptyHint("Noch keine Statusdaten verfügbar.")
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatusCard(
                    Modifier.weight(1f),
                    icon = Icons.Default.Group,
                    value = status.onlinePlayers.toString(),
                    label = "Spieler online",
                )
                StatusCard(
                    Modifier.weight(1f),
                    icon = Icons.Default.Dns,
                    value = status.networkServers.toString(),
                    label = "Netzwerk-Server",
                )
            }
            Text("Aktuelle Meldungen", style = MaterialTheme.typography.titleMedium)
            if (status.messages.filter(String::isNotBlank).isEmpty()) {
                EmptyHint("Keine Meldungen.")
            }
            status.messages.filter(String::isNotBlank).forEach { message ->
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
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

@Composable
private fun SettingsScreen(
    version: String,
    onRefresh: () -> Unit,
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
