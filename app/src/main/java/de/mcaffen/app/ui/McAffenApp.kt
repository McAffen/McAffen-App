package de.mcaffen.app.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import de.mcaffen.app.data.McAffenRepository
import de.mcaffen.app.model.PlayerStat
import de.mcaffen.app.model.ServerStatus
import de.mcaffen.app.model.VoteSite
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

private enum class Tab(val title: String) {
    LEADERBOARD("Rangliste"),
    VOTES("Voten"),
    STATUS("Status"),
}

class McAffenViewModel(private val repository: McAffenRepository) : ViewModel() {
    val players = repository.players
    val voteSites = repository.voteSites
    val status = repository.status
    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()
    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun refresh() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            _loading.value = true
            _error.value = null
            runCatching { repository.refresh() }
                .onFailure { _error.value = "Daten konnten nicht geladen werden. Cache wird angezeigt." }
            _loading.value = false
        }
    }
}

@Composable
fun McAffenApp(repository: McAffenRepository) {
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
    var selectedTab by remember { mutableStateOf(Tab.LEADERBOARD) }
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { vm.refresh() }
    LaunchedEffect(error) { error?.let { snackbar.showSnackbar(it) } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(selectedTab.title) },
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
                Tab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = {
                            Icon(
                                when (tab) {
                                    Tab.LEADERBOARD -> Icons.Default.EmojiEvents
                                    Tab.VOTES -> Icons.Default.HowToVote
                                    Tab.STATUS -> Icons.Default.Speed
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
                Tab.VOTES -> VoteScreen(voteSites, Modifier.padding(padding))
                Tab.STATUS -> StatusScreen(status, Modifier.padding(padding))
            }
        }
    }
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
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
        LazyColumn(contentPadding = PaddingValues(vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filtered, key = { it.name }) { player ->
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(player.name, fontWeight = FontWeight.Medium)
                        Text("${player.score} Punkte")
                    }
                }
            }
        }
    }
}

@Composable
private fun VoteScreen(voteSites: List<VoteSite>, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Column(modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Dein Minecraft-Name", style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
        var name by remember { mutableStateOf("") }
        OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), singleLine = true, placeholder = { Text("Spielername") })
        Text("Für jede Abstimmung erhältst du Belohnungen wie Vote-Schlüssel und Coins.")
        voteSites.forEach { site ->
            Button(
                onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(site.url))) },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank(),
            ) { Text("Bei ${site.site} voten") }
        }
        Button(
            onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("app://connector_openai_deep_research"))) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(Icons.Default.Settings, contentDescription = null)
            Text("  Deep Research öffnen")
        }
    }
}

@Composable
private fun StatusScreen(status: ServerStatus?, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (status == null) {
            Text("Noch keine Statusdaten verfügbar.")
        } else {
            Text("Online", style = androidx.compose.material3.MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("${status.onlinePlayers} Spieler online")
            Text("${status.networkServers} Netzwerk-Server")
            Text("Aktuelle Meldungen", style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
            status.messages.filter(String::isNotBlank).forEach { message ->
                Card(Modifier.fillMaxWidth()) { Text(message, Modifier.padding(16.dp)) }
            }
        }
    }
}
