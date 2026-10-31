package de.mcaffen.app.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import androidx.core.content.ContextCompat
import de.mcaffen.app.BuildConfig
import de.mcaffen.app.R
import de.mcaffen.app.data.ApkVerification
import de.mcaffen.app.data.McAffenRepository
import de.mcaffen.app.data.SettingsStore
import de.mcaffen.app.data.UpdateCheckScheduler
import de.mcaffen.app.data.UpdateConfig
import de.mcaffen.app.data.UpdateInstaller
import de.mcaffen.app.model.AppRelease
import de.mcaffen.app.model.PlayerStat
import de.mcaffen.app.model.ServerInfo
import de.mcaffen.app.model.ServerStatus
import de.mcaffen.app.model.VoteSite
import de.mcaffen.app.ui.ThemeOptions
import java.io.File
import java.io.IOException
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

/** Fortschritt des direkten In-App-Updates (APK-Download + System-Install-Prompt). */
sealed interface DownloadState {
    data object Idle : DownloadState
    data class Downloading(val progress: Int) : DownloadState
    data class Ready(val apk: File) : DownloadState
    /** APK heruntergeladen, aber nicht als Update über diese Installation nutzbar. */
    data class Blocked(val verification: ApkVerification) : DownloadState
    data class Failed(val message: String) : DownloadState
}

class McAffenViewModel(
    private val repository: McAffenRepository,
    private val settings: SettingsStore,
) : ViewModel() {
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
    private val _download = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val download = _download.asStateFlow()

    /** Einmalige Snackbar-Meldungen rund um den Update-Check (z. B. offline). */
    private val _updateNotice = MutableStateFlow<String?>(null)
    val updateNotice = _updateNotice.asStateFlow()

    fun clearUpdateNotice() {
        _updateNotice.value = null
    }

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

    /**
     * Prüft GitHub auf ein neueres Release. Automatische Prüfungen (App-Start,
     * Rückkehr in die App, WorkManager) sind gedrosselt und erfordern, dass der
     * Nutzer sie in den Einstellungen aktiviert hat. Mit [force] (Button
     * "Jetzt prüfen") wird immer geprüft und das Ergebnis per Snackbar gemeldet.
     */
    fun checkForUpdate(force: Boolean = false) {
        if (_update.value != null) return
        if (!force) {
            if (!settings.updateChecksEnabled.value) return
            val sinceLast = System.currentTimeMillis() - settings.lastUpdateCheck.value
            if (sinceLast in 0 until UpdateConfig.MIN_CHECK_INTERVAL_MS) return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val result = runCatching { repository.latestRelease() }
            settings.touchUpdateCheck()
            result.onSuccess { release ->
                val skipped = settings.skippedUpdate.value
                val isNewer = UpdateConfig.isNewerVersion(release.tagName, BuildConfig.VERSION_NAME)
                // Kritische Updates dringen auch an "Später" vorbei.
                if (release.critical || (isNewer && release.tagName != skipped)) {
                    _update.value = release
                }
            }.onFailure { error ->
                if (force) {
                    _updateNotice.value = if (error is IOException) {
                        "Keine Internetverbindung – Update-Prüfung nicht möglich."
                    } else {
                        "Update-Prüfung fehlgeschlagen: ${error.message ?: error::class.simpleName}"
                    }
                }
            }
        }
    }

    fun dismissUpdate() {
        _update.value = null
        if (_download.value !is DownloadState.Ready) _download.value = DownloadState.Idle
    }

    /**
     * In-App-Update: lädt die neue APK herunter, verifiziert sie (Größe, Paket,
     * Signatur) und startet anschließend den System-Install-Prompt.
     */
    fun startUpdate(context: Context) {
        val apkUrl = _update.value?.assetUrl ?: return
        viewModelScope.launch(Dispatchers.IO) {
            _download.value = DownloadState.Downloading(0)
            runCatching {
                repository.downloadUpdateApk(
                    context = context.applicationContext,
                    url = apkUrl,
                ) { progress ->
                    _download.value = DownloadState.Downloading(progress)
                }
            }.fold(
                onSuccess = { apk ->
                    val verification = UpdateInstaller.verifyApk(context.applicationContext, apk)
                    _download.value = if (verification == ApkVerification.Ok) {
                        DownloadState.Ready(apk)
                    } else {
                        DownloadState.Blocked(verification)
                    }
                },
                onFailure = { error ->
                    _download.value = DownloadState.Failed(
                        when (error) {
                            is IOException -> "Keine Internetverbindung – Download wurde abgebrochen."
                            else -> error.message ?: "Download fehlgeschlagen"
                        },
                    )
                },
            )
        }
    }

    fun resetDownload() {
        _download.value = DownloadState.Idle
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
                McAffenViewModel(repository, settings) as T
        },
    )
    val players by vm.players.collectAsStateWithLifecycle(emptyList())
    val voteSites by vm.voteSites.collectAsStateWithLifecycle(emptyList())
    val status by vm.status.collectAsStateWithLifecycle(null)
    val loading by vm.loading.collectAsStateWithLifecycle()
    val error by vm.error.collectAsStateWithLifecycle()
    val update by vm.update.collectAsStateWithLifecycle()
    val updateNotice by vm.updateNotice.collectAsStateWithLifecycle()
    val legalCache by vm.legalCache.collectAsStateWithLifecycle(emptyMap())
    val myName by settings.playerName.collectAsStateWithLifecycle()
    val voteTimes by settings.voteTimes.collectAsStateWithLifecycle()
    val onboardingDone by settings.onboardingDone.collectAsStateWithLifecycle()
    val updateChecksEnabled by settings.updateChecksEnabled.collectAsStateWithLifecycle()
    val lastUpdateCheck by settings.lastUpdateCheck.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(Tab.LEADERBOARD) }
    var isMenuExpanded by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }
    var legalKey by remember { mutableStateOf<String?>(null) }
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { vm.refresh() }
    LaunchedEffect(error) { error?.let { snackbar.showSnackbar(it) } }
    LaunchedEffect(updateNotice) {
        updateNotice?.let {
            snackbar.showSnackbar(it)
            vm.clearUpdateNotice()
        }
    }

    // Update-Prüfung beim Start und optional beim Zurückkehren in die App:
    // ON_RESUME feuert auch beim ersten Start, das Rate-Limit hält die Frequenz begrenzt.
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) vm.checkForUpdate()
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }

    val downloadState by vm.download.collectAsStateWithLifecycle()
    LaunchedEffect(downloadState) {
        when (val state = downloadState) {
            is DownloadState.Ready -> {
                // APK verifiziert → System-Install-Prompt; Nutzer bestätigt,
                // Android ersetzt die App und behält alle Daten.
                UpdateInstaller.launchInstall(context, state.apk)
                vm.resetDownload()
            }
            else -> {}
        }
    }

    update?.let { release ->
        UpdateDialog(
            release = release,
            downloadState = downloadState,
            canInstall = UpdateInstaller.canInstallDirectly(context),
            onInstall = { vm.startUpdate(context) },
            onEnableInstalls = { UpdateInstaller.openUnknownSourcesSettings(context) },
            onRetry = { vm.startUpdate(context) },
            onOpenGitHub = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(release.htmlUrl))) },
            onOpenGitHubAndSkip = {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(release.htmlUrl)))
                settings.skipUpdate(release.tagName)
                vm.dismissUpdate()
            },
            onLater = {
                settings.skipUpdate(release.tagName)
                vm.dismissUpdate()
            },
            onDismiss = vm::dismissUpdate,
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
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    val forward = targetState.ordinal >= initialState.ordinal
                    (slideInHorizontally(tween(durationMillis = 240)) { fraction ->
                        if (forward) fraction / 3 else -fraction / 3
                    } + fadeIn(tween(durationMillis = 240))) togetherWith
                        slideOutHorizontally(tween(durationMillis = 180)) { fraction ->
                            if (forward) -fraction / 3 else fraction / 3
                        } + fadeOut(tween(durationMillis = 180))
                },
                label = "tabContent",
            ) { tab ->
                when (tab) {
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
                        voteTimes = voteTimes,
                        onMarkVoted = { site -> settings.markVoted(site) },
                        storedName = myName,
                        onStoreName = settings::setPlayerName,
                        loading = loading,
                        onRefresh = vm::refresh,
                        modifier = Modifier.padding(padding),
                    )
                    Tab.STATUS -> StatusScreen(
                        status = status,
                        settings = settings,
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
                        updateChecksEnabled = updateChecksEnabled,
                        onUpdateChecksChange = { enabled ->
                            settings.setUpdateChecksEnabled(enabled)
                            UpdateCheckScheduler.update(context)
                        },
                        onCheckForUpdate = { vm.checkForUpdate(force = true) },
                        lastUpdateCheck = lastUpdateCheck,
                        modifier = Modifier.padding(padding),
                    )
                }
            }
        }
    }

    if (!onboardingDone) {
        OnboardingDialog(
            onConfirm = { name ->
                if (name.isNotBlank()) settings.setPlayerName(name.trim())
                settings.setOnboardingDone()
            },
            onSkip = { settings.setOnboardingDone() },
        )
    }
}

/** Moderne Update-Oberfläche: Versionsübergang, Release-Notes, Downloadgröße und Fortschritt. */
@Composable
private fun UpdateDialog(
    release: AppRelease,
    downloadState: DownloadState,
    canInstall: Boolean,
    onInstall: () -> Unit,
    onEnableInstalls: () -> Unit,
    onRetry: () -> Unit,
    onOpenGitHub: () -> Unit,
    onOpenGitHubAndSkip: () -> Unit,
    onLater: () -> Unit,
    onDismiss: () -> Unit,
) {
    val state = downloadState
    val downloading = state is DownloadState.Downloading
    val blocked = state as? DownloadState.Blocked
    val failed = state as? DownloadState.Failed
    val hasApk = !release.assetUrl.isNullOrBlank()
    // Kritische Updates (Marker in den Release-Notes) dürfen nicht weggeklickt werden.
    val dismissible = !release.critical && !downloading
    AlertDialog(
        onDismissRequest = { if (dismissible) onDismiss() },
        title = {
            Text(
                when {
                    release.critical -> "Update erforderlich ⚠️"
                    blocked != null -> "Update blockiert"
                    failed != null -> "Update fehlgeschlagen"
                    else -> "Update verfügbar 🚀"
                },
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    VersionBadge("Aktuell", BuildConfig.VERSION_NAME)
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "aktualisiert auf",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    VersionBadge(
                        "Neu",
                        release.tagName.removePrefix("v"),
                        highlighted = true,
                    )
                }
                if (release.critical) {
                    Text(
                        "Dies ist ein kritisches Update und muss installiert werden.",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                updateNotesText(release.notes)?.let { notes ->
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .heightIn(max = 160.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text("Was ist neu?", style = MaterialTheme.typography.labelLarge)
                        Text(notes, style = MaterialTheme.typography.bodySmall)
                    }
                }
                Text(
                    "Das Update wird direkt in der App heruntergeladen und über der " +
                        "installierten Version installiert – deine Daten und Einstellungen " +
                        "bleiben erhalten.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                formatBytes(release.assetSize)?.let { size ->
                    Text(
                        "Downloadgröße: $size",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                when (state) {
                    is DownloadState.Downloading -> {
                        if (state.progress >= 0) {
                            LinearProgressIndicator(
                                progress = { (state.progress / 100f).coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth(),
                            )
                            Text(
                                "Lädt herunter … ${state.progress}%",
                                style = MaterialTheme.typography.bodySmall,
                            )
                        } else {
                            LinearProgressIndicator(Modifier.fillMaxWidth())
                            Text("Lädt herunter …", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    is DownloadState.Failed -> Text(
                        "Fehler: ${state.message}\nDu kannst es erneut versuchen.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                    is DownloadState.Blocked -> Text(
                        when (state.verification) {
                            ApkVerification.Corrupt ->
                                "Die heruntergeladene Datei ist unvollständig oder beschädigt. " +
                                    "Versuche es lieber direkt über GitHub."
                            ApkVerification.Incompatible ->
                                "Diese APK passt nicht als Update über die installierte Version " +
                                    "(Paket-ID oder Version). Lade sie über GitHub …"
                            else ->
                                "Die APK ist mit einem anderen Schlüssel signiert als die " +
                                    "installierte App. Android erlaubt die Überinstallation dann " +
                                    "nicht. Öffne das Release auf GitHub, um sie zu laden."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                    else -> {}
                }
                if (!canInstall && state is DownloadState.Idle && hasApk) {
                    Text(
                        "Einmalig erforderlich: Erlaube der McAffen-App, „Apps aus unbekannten " +
                            "Quellen“ zu installieren. Danach aktualisiert sich die App direkt selbst.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        confirmButton = {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                when {
                    downloading -> TextButton(onClick = {}, enabled = false) { Text("Lädt …") }
                    failed != null || blocked?.verification == ApkVerification.Corrupt ->
                        Button(onClick = onRetry) { Text("Erneut versuchen") }
                    !hasApk || (blocked != null && blocked.verification != ApkVerification.Corrupt) ->
                        Button(onClick = onOpenGitHubAndSkip) { Text("Auf GitHub laden") }
                    !canInstall -> Button(onClick = onEnableInstalls) { Text("Installation erlauben") }
                    else -> Button(onClick = onInstall) { Text("Jetzt aktualisieren") }
                }
                if (hasApk && !downloading) {
                    TextButton(onClick = onOpenGitHub) { Text("Release auf GitHub ansehen") }
                }
            }
        },
        dismissButton = {
            if (dismissible) {
                TextButton(onClick = onLater) { Text("Später") }
            }
        },
    )
}

/** Kleiner Versions-Chip: Label darüber, kompakte Versionsnummer in der Mitte. */
@Composable
private fun VersionBadge(label: String, version: String, highlighted: Boolean = false) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (highlighted) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
    ) {
        Column(
            Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(version, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

/** Release-Notes leserlich aufbereiten: Markdown-Bilder/Links/Hervorhebungen entfernen. */
private fun updateNotesText(notes: String?): String? {
    if (notes.isNullOrBlank()) return null
    return notes
        .replace(Regex(Regex.escape(UpdateConfig.CRITICAL_MARKER), RegexOption.IGNORE_CASE), "")
        .replace(Regex("!\\[[^\\]]*\\]\\([^)]*\\)"), "") // Markdown-Bilder
        .replace(Regex("\\[([^\\]]+)\\]\\([^)]*\\)"), "$1") // [Link](url) → Text
        .replace("**", "")
        .split('\n')
        .joinToString("\n") { line -> line.trimStart('#').trim() }
        .replace(Regex("\\n{3,}"), "\n\n")
        .trim()
        .takeIf(String::isNotBlank)
}

/** Bytes leserlich formatieren (z. B. "8,4 MB") für die Downloadgröße-Anzeige. */
private fun formatBytes(bytes: Long?): String? {
    if (bytes == null || bytes <= 0) return null
    val megabytes = bytes / 1024.0 / 1024.0
    return when {
        megabytes >= 1.0 -> String.format(Locale.getDefault(), "%.1f MB", megabytes)
        else -> String.format(Locale.getDefault(), "%.0f KB", bytes / 1024.0)
    }
}

/** Freundliche Darstellung, wie lange die letzte Update-Prüfung her ist. */
private fun lastCheckText(timestamp: Long): String {
    if (timestamp <= 0L) return "Noch nie"
    val minutes = (System.currentTimeMillis() - timestamp) / 60_000
    return when {
        minutes <= 0 -> "gerade eben"
        minutes == 1L -> "vor 1 Minute"
        minutes < 60 -> "vor $minutes Minuten"
        minutes < 120 -> "vor 1 Stunde"
        minutes < 60 * 24 -> "vor ${minutes / 60} Stunden"
        minutes < 60 * 48 -> "vor 1 Tag"
        else -> SimpleDateFormat("dd.MM.yyyy, HH:mm", Locale.getDefault()).format(Date(timestamp)) + " Uhr"
    }
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {    Column(
        modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()
        Text("Lade McAffen-Daten …", Modifier.padding(top = 12.dp))
    }
}

/** Erststart-Begrüßung: Spielername direkt erfassen, damit alles personalisiert ist. */
@Composable
private fun OnboardingDialog(
    onConfirm: (String) -> Unit,
    onSkip: () -> Unit,
) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onSkip,
        title = {
            Text(
                "Willkommen bei McAffen 🐵",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Trag deinen Minecraft-Namen ein – dann siehst du sofort deinen Rang in der " +
                        "Rangliste, deine Punkte und musst beim Voten nie wieder tippen.",
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Dein Minecraft-Name") },
                    singleLine = true,
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(name) }, enabled = name.isNotBlank()) { Text("Los geht's") }
        },
        dismissButton = {
            TextButton(onClick = onSkip) { Text("Später") }
        },
    )
}

private const val PODIUM_SIZE = 3

private const val GOLD = 0xFFFFB300
private const val SILVER = 0xFFB0BEC5
private const val BRONZE = 0xFFB87333
private const val GREEN_TREND = 0xFF2E7D32
private const val RED_TREND = 0xFFC62828

private fun medalColor(position: Int): Color? = when (position) {
    1 -> Color(GOLD)
    2 -> Color(SILVER)
    3 -> Color(BRONZE)
    else -> null
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
            when {
                players.isEmpty() && !loading ->
                    EmptyHint("Noch keine Ranglistendaten verfügbar.", Modifier.padding(top = 20.dp))
                loading && players.isEmpty() ->
                    ShimmerList(Modifier.padding(top = 16.dp))
                filtered.isEmpty() ->
                    EmptyHint("Kein Spieler gefunden.", Modifier.padding(top = 20.dp))
                else -> Column(
                    Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    if (query.isBlank() && myName.isNotBlank()) {
                        MyProfileCard(
                            myName = myName,
                            myStat = myStat,
                            myIndex = myIndex,
                            leaderScore = leaderScore,
                            canJump = players.size > PODIUM_SIZE && myIndex >= PODIUM_SIZE,
                            onJump = ::jumpToMe,
                        )
                    }
                    if (query.isBlank()) {
                        Podium(players.take(PODIUM_SIZE))
                    }
                    val listedPlayers = when {
                        query.isNotBlank() -> filtered
                        players.size > PODIUM_SIZE -> players.drop(PODIUM_SIZE)
                        else -> emptyList()
                    }
                    if (listedPlayers.isNotEmpty()) {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(listedPlayers, key = { it.name }) { player ->
                                val position =
                                    players.indexOfFirst { it.name == player.name } + 1
                                PlayerRow(
                                    player = player,
                                    position = position,
                                    leaderScore = leaderScore,
                                    isMe = player.name.equals(myName, ignoreCase = true),
                                )
                            }
                        }
                    } else {
                        Text(
                            "Alle Platzierungen sind im Podium 👑",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp),
                        )
                    }
                }
            }
        }
    }
}

/** Zeigt dezent, wer die Person selbst ist: Rang, Punkte, Fortschritt Richtung Platz 1. */
@Composable
private fun MyProfileCard(
    myName: String,
    myStat: PlayerStat?,
    myIndex: Int,
    leaderScore: Int,
    canJump: Boolean,
    onJump: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PlayerAvatar(myName, 52.dp)
            Column(Modifier.weight(1f)) {
                if (myStat != null) {
                    Text(myName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "Rang #${myIndex + 1} · ${formatScore(myStat.score)} Punkte",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        TrendChip(myStat)
                    }
                    when {
                        myIndex == 0 -> Text(
                            "Du führst die Rangliste! 👑",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(GOLD),
                        )
                        leaderScore > myStat.score -> Text(
                            "${formatScore(leaderScore - myStat.score)} Punkte bis Rang 1",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                } else {
                    Text(myName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "Du bist (noch) nicht in der Rangliste. Vote täglich, um Punkte zu sammeln!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
            if (myStat != null && canJump) {
                FilledTonalButton(onClick = onJump) { Text("Springen") }
            }
        }
    }
}

/** Top-3-Podium: Platz 2 – Platz 1 – Platz 3, mit Minecraft-Köpfen. */
@Composable
private fun Podium(players: List<PlayerStat>) {
    if (players.isEmpty()) return
    val visualOrder = buildList {
        players.getOrNull(1)?.let { add(1) }
        players.getOrNull(0)?.let { add(0) }
        players.getOrNull(2)?.let { add(2) }
    }
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        visualOrder.forEach { index ->
            PodiumSlot(player = players[index], position = index + 1, isChampion = index == 0, Modifier.weight(1f))
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
    Card(
        modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (isChampion) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
        ),
    ) {
        Column(
            Modifier.fillMaxWidth().padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(
                    Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = medal ?: MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(if (isChampion) 24.dp else 18.dp),
                )
                Text(
                    "#$position",
                    fontWeight = FontWeight.Bold,
                    color = medal ?: MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Box(contentAlignment = Alignment.Center) {
                if (isChampion) {
                    Box(
                        Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .border(2.dp, Color(GOLD), CircleShape),
                    )
                }
                PlayerAvatar(player.name, if (isChampion) 56.dp else 46.dp)
            }
            Text(
                player.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = if (isChampion) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                "${formatScore(player.score)} Punkte",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** Eine Zeile der Rangliste: Kopf, Rang, Trend-Pfeil und Fortschrittsbalken Richtung Führender. */
@Composable
private fun PlayerRow(
    player: PlayerStat,
    position: Int,
    leaderScore: Int,
    isMe: Boolean,
) {
    val medal = medalColor(position)
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isMe) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
        ),
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "#$position",
                        fontWeight = FontWeight.Bold,
                        color = medal ?: MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    PlayerAvatar(player.name, 40.dp)
                    Column {
                        Text(
                            player.name,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
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
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "${formatScore(player.score)} P",
                        fontWeight = FontWeight.SemiBold,
                    )
                    TrendChip(player)
                }
            }
            if (leaderScore > 0) {
                val fraction = (player.score / leaderScore.coerceAtLeast(1).toFloat()).coerceIn(0.04f, 1f)
                val animatedFraction by animateFloatAsState(
                    targetValue = fraction,
                    animationSpec = spring(stiffness = Spring.StiffnessLow),
                    label = "scoreBar",
                )
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)),
                ) {
                    Box(
                        Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(animatedFraction)
                            .clip(RoundedCornerShape(2.dp))
                            .background(medal ?: MaterialTheme.colorScheme.primary),
                    )
                }
            }
        }
    }
}

/** Kleiner Trend-Chip: ↑ Punktezuwachs, ↓ Punkteverlust, NEU für neue Einträge. */
@Composable
private fun TrendChip(player: PlayerStat) {
    val previous = player.previousScore
    if (previous == null && player.score <= 0) return
    if (previous == player.score) return

    val trend = when {
        previous == null -> Triple(GREEN_TREND, "NEU", Icons.AutoMirrored.Filled.TrendingUp)
        player.score > previous -> Triple(GREEN_TREND, "+${formatScore(player.score - previous)}", Icons.AutoMirrored.Filled.TrendingUp)
        else -> Triple(RED_TREND, "−${formatScore(previous - player.score)}", Icons.AutoMirrored.Filled.TrendingDown)
    }
    val color = Color(trend.first)
    Surface(color = color.copy(alpha = 0.14f), shape = RoundedCornerShape(50)) {
        Row(
            Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Icon(trend.third, contentDescription = null, tint = color, modifier = Modifier.size(13.dp))
            Text(
                trend.second,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = color,
            )
        }
    }
}

/** Minecraft-Spielerkopf von mc-heads.net (so zeigt es auch die Website). */
@Composable
private fun PlayerAvatar(name: String, size: Dp, modifier: Modifier = Modifier) {
    SubcomposeAsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data("https://mc-heads.net/avatar/${Uri.encode(name)}/128")
            .crossfade(true)
            .build(),
        contentDescription = "Minecraft-Kopf von $name",
        modifier = modifier.size(size).clip(CircleShape),
        loading = { AvatarFallback(size) },
        error = { AvatarFallback(size) },
    )
}

@Composable
private fun AvatarFallback(size: Dp) {
    Box(
        Modifier.size(size).background(MaterialTheme.colorScheme.surfaceVariant),
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

/** Shimmer-Platzhalterkarten, solange die Rangliste lädt. */
@Composable
private fun ShimmerList(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 650, easing = LinearEasing)),
        label = "shimmerAlpha",
    )
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(6) {
            Card(Modifier.fillMaxWidth()) {
                Row(
                    Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha)),
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(
                            Modifier
                                .fillMaxWidth(0.55f)
                                .height(12.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha)),
                        )
                        Box(
                            Modifier
                                .fillMaxWidth(0.35f)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha * 0.7f)),
                        )
                    }
                }
            }
        }
    }
}

private fun formatScore(score: Int): String =
    NumberFormat.getIntegerInstance(Locale.GERMANY).format(score)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VoteScreen(
    players: List<PlayerStat>,
    voteSites: List<VoteSite>,
    voteTimes: Map<String, Long>,
    onMarkVoted: (String) -> Unit,
    storedName: String,
    onStoreName: (String) -> Unit,
    loading: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = System.currentTimeMillis()
            kotlinx.coroutines.delay(1000)
        }
    }
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        PlayerAvatar(player.name, 44.dp)
                        Text(
                            "Spieler gefunden: ${player.name} – du kannst jetzt voten!",
                            fontWeight = FontWeight.Medium,
                        )
                    }
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
                val lastVoted = voteTimes[site.site] ?: 0L
                val readyAgain = lastVoted <= 0L || lastVoted + VOTE_COOLDOWN_MS - now <= 0
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
                            val status = when {
                                lastVoted <= 0L -> "Vote-Schlüssel & Coins"
                                readyAgain -> "Bereit – du kannst wieder voten ✓"
                                else -> "Wieder möglich in ${formatRemaining(lastVoted + VOTE_COOLDOWN_MS - now)}"
                            }
                            Text(
                                status,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (lastVoted > 0L && readyAgain) {
                                    Color(GREEN_TREND)
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                            )
                        }
                        Button(
                            onClick = {
                                onMarkVoted(site.site)
                                voteUri?.let { context.startActivity(Intent(Intent.ACTION_VIEW, it)) }
                            },
                            enabled = canVote && voteUri != null,
                        ) {
                            if (lastVoted > 0L && !readyAgain) Text("Nochmal") else Text("Voten")
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

/** Vote-Seiten erlauben pro Spieler/Tag meist einen Vote. */
private const val VOTE_COOLDOWN_MS = 24L * 60 * 60 * 1000

/** Übrige Zeit bis zum nächsten Vote, als HH:MM:SS. */
private fun formatRemaining(remainingMs: Long): String {
    val totalSeconds = (remainingMs / 1000).coerceAtLeast(0)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatusScreen(
    status: ServerStatus?,
    settings: SettingsStore,
    loading: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var todayPeak by remember { mutableIntStateOf(settings.peakToday()) }
    LaunchedEffect(status) {
        status?.let { todayPeak = settings.touchPeakToday(it.onlinePlayers) }
    }
    val animatedPlayers by animateIntAsState(
        targetValue = status?.onlinePlayers ?: 0,
        animationSpec = tween(durationMillis = 800),
        label = "onlinePlayers",
    )
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
                        value = formatScore(animatedPlayers),
                        label = "Spieler online",
                    )
                    StatusCard(
                        Modifier.weight(1f),
                        icon = Icons.Default.Dns,
                        value = status.networkServers.toString(),
                        label = "Netzwerk-Server",
                    )
                }
                if (todayPeak > 0) {
                    PeakCard(todayPeak)
                }
                if (status.servers.isNotEmpty()) {
                    ServerListCard(status.servers)
                }
            }
            ServerAddressCard()
            CommunityLinksCard(
                onOpen = { url -> context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) },
                onShare = {
                    val share = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(
                            Intent.EXTRA_TEXT,
                            "Schau auf dem McAffen-Netzwerk vorbei! 🐵\nServer-IP: mcaffen.de",
                        )
                    }
                    context.startActivity(Intent.createChooser(share, "Server teilen"))
                },
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

/** Alle Server des Netzwerks mit Online-Status, Spielern und Version. */
@Composable
private fun ServerListCard(servers: List<ServerInfo>) {
    Card(Modifier.fillMaxWidth()) {
        Column(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("Server im Netzwerk", style = MaterialTheme.typography.titleMedium)
            servers.forEach { server -> ServerRow(server) }
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
        Box(
            Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(if (server.online) Color(GREEN_TREND) else Color(RED_TREND)),
        )
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(server.name, fontWeight = FontWeight.Medium)
                if (server.type.equals("proxy", ignoreCase = true)) {
                    Text(
                        "PROXY",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                        color = if (maintenance) Color(RED_TREND) else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        Text(
            if (server.players == 1) "1 Spieler" else "${formatScore(server.players)} Spieler",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Entfernt Minecraft-Farbcodes aus Versionsstring (z. B. §4 vor "Wartung"). */
private fun cleanColorCodes(value: String): String =
    value.replace(Regex("§."), "").trim()

/** Peak des heutigen Tages als kleine Info-Karte. */
@Composable
private fun PeakCard(peak: Int) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                Icons.Default.BarChart,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Column {
                Text(
                    "Höchste Spielerzahl heute",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    "${formatScore(peak)} Spieler gleichzeitig online",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun CommunityLinksCard(onOpen: (String) -> Unit, onShare: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Community", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { onOpen("https://mcaffen.de") }) { Text("Website") }
                TextButton(onClick = { onOpen("https://discord.com/invite/tpyHQqBzVD") }) { Text("Discord") }
                TextButton(onClick = { onOpen("https://www.youtube.com/@McAffenofficial") }) { Text("YouTube") }
            }
            OutlinedButton(onClick = onShare, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Share, contentDescription = null, Modifier.size(18.dp))
                Text("  Server teilen")
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
    updateChecksEnabled: Boolean,
    onUpdateChecksChange: (Boolean) -> Unit,
    onCheckForUpdate: () -> Unit,
    lastUpdateCheck: Long,
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
                Text("Updates", style = MaterialTheme.typography.titleMedium)
                val notifPermissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission(),
                ) { }
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Automatisch nach Updates suchen", Modifier.weight(1f))
                    Switch(
                        checked = updateChecksEnabled,
                        onCheckedChange = { enabled ->
                            // Android 13+: Benachrichtigungserlaubnis nachfragen,
                            // sonst wird das gefundene Hintergrund-Update unsichtbar.
                            if (enabled && Build.VERSION.SDK_INT >= 33 &&
                                ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.POST_NOTIFICATIONS,
                                ) != PackageManager.PERMISSION_GRANTED
                            ) {
                                notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                            onUpdateChecksChange(enabled)
                        },
                    )
                }
                Text(
                    "Prüft beim Start, beim Zurückkehren in die App und im Hintergrund " +
                        "(ca. alle 12 Stunden) auf GitHub. Funde erscheinen direkt als " +
                        "Dialog oder als Benachrichtigung.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        "Zuletzt geprüft: ${lastCheckText(lastUpdateCheck)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(onClick = onCheckForUpdate) { Text("Jetzt prüfen") }
                }
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
