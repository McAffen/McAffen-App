package de.mcaffen.app.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.HowToVote
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import de.mcaffen.app.BuildConfig
import de.mcaffen.app.data.ApkVerification
import de.mcaffen.app.data.McAffenRepository
import de.mcaffen.app.data.SettingsStore
import de.mcaffen.app.data.UpdateCheckScheduler
import de.mcaffen.app.data.UpdateConfig
import de.mcaffen.app.data.UpdateInstaller
import de.mcaffen.app.model.AppRelease
import de.mcaffen.app.ui.components.BootSplash
import de.mcaffen.app.ui.components.BrandLogo
import de.mcaffen.app.ui.components.PlayerAvatar
import de.mcaffen.app.ui.components.WelcomeFlow
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private enum class Tab(val title: String) {
    LEADERBOARD("Rangliste"),
    VOTES("Voten"),
    STATUS("Status"),
    SETTINGS("Einstellungen"),
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
                // Bei manueller Prüfung (force) wird ein zuvor übersprungenes
                // Update erneut angeboten; kritische Updates dringen immer durch.
                val ignoreSkip = force
                if (release.critical || (isNewer && (ignoreSkip || release.tagName != skipped))) {
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

/**
 * Wurzel der App: Startbildschirm, Erststart-Flow, Top-Bar, Tab-Navigation
 * mit animiertem Wechsel und die Dialoge (Update, Über, Rechtliches).
 */
@OptIn(ExperimentalMaterial3Api::class)
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
    val animationsEnabled by settings.animationsEnabled.collectAsStateWithLifecycle()
    val darkMode by settings.darkMode.collectAsStateWithLifecycle()
    val colorName by settings.color.collectAsStateWithLifecycle()
    val hapticsEnabled by settings.hapticsEnabled.collectAsStateWithLifecycle()
    val compactMode by settings.compactMode.collectAsStateWithLifecycle()

    var selectedTab by rememberSaveable { mutableStateOf(Tab.LEADERBOARD) }
    var isMenuExpanded by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }
    var legalKey by remember { mutableStateOf<String?>(null) }
    val snackbar = remember { SnackbarHostState() }
    var bestRank by remember { mutableStateOf(settings.bestRank()) }

    // Startbildschirm: kurze, animierte Begrüßung statt nacktem Spinner.
    var booting by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        delay(650)
        booting = false
    }

    LaunchedEffect(Unit) { vm.refresh() }
    LaunchedEffect(error) { error?.let { snackbar.showSnackbar(it) } }
    LaunchedEffect(updateNotice) {
        updateNotice?.let {
            snackbar.showSnackbar(it)
            vm.clearUpdateNotice()
        }
    }
    // Rekord-Rang nach jeder Aktualisierung der Rangliste merken.
    LaunchedEffect(players, myName) {
        val index = players.indexOfFirst { it.name.equals(myName, ignoreCase = true) }
        if (index >= 0) {
            settings.updateBestRank(index + 1)
            bestRank = settings.bestRank()
        }
    }

    // Update-Prüfung beim Start und beim Zurückkehren in die App:
    // ON_RESUME feuert auch beim ersten Start, das Rate-Limit begrenzt die Frequenz.
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
            onEnableInstalls = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(release.htmlUrl))) },
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
        AboutDialog(
            version = BuildConfig.VERSION_NAME,
            onOpenLegal = { key ->
                showAbout = false
                legalKey = key
            },
            onClose = { showAbout = false },
        )
    }

    legalKey?.let { key ->
        val cached = legalCache[key]
        legalDocument(key, cached?.title, cached?.html)
    }?.let { document ->
        LegalDialog(document = document, onDismiss = { legalKey = null })
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.96f),
                ),
                navigationIcon = {
                    Box {
                        Row(
                            Modifier
                                .padding(start = 6.dp)
                                .clip(RoundedCornerShape(50))
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                .padding(horizontal = 4.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            IconButton(
                                onClick = { isMenuExpanded = true },
                                modifier = Modifier.size(34.dp),
                            ) {
                                BrandLogo(Modifier.size(30.dp))
                            }
                        }
                        DropdownMenu(
                            expanded = isMenuExpanded,
                            onDismissRequest = { isMenuExpanded = false },
                            shape = RoundedCornerShape(18.dp),
                        ) {
                            DropdownMenuItem(
                                text = { Text("Einstellungen") },
                                leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) },
                                onClick = {
                                    isMenuExpanded = false
                                    selectedTab = Tab.SETTINGS
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("Über die App") },
                                leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
                                onClick = {
                                    isMenuExpanded = false
                                    showAbout = true
                                },
                            )
                        }
                    }
                },
                title = {
                    Text(
                        selectedTab.title,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                actions = {
                    val rotation by animateFloatAsState(
                        targetValue = if (loading) 360f else 0f,
                        animationSpec = if (loading) {
                            infiniteRepeatable(animation = tween(900), repeatMode = RepeatMode.Restart)
                        } else {
                            tween(300)
                        },
                        label = "refreshSpin",
                    )
                    IconButton(onClick = vm::refresh) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Aktualisieren",
                            modifier = Modifier.rotate(rotation),
                        )
                    }
                    if (myName.isNotBlank()) {
                        Box(Modifier.padding(end = 8.dp)) {
                            PlayerAvatar(
                                name = myName,
                                size = 32.dp,
                                ringColor = MaterialTheme.colorScheme.primary,
                                ringWidth = 1.5.dp,
                            )
                        }
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                tonalElevation = 0.dp,
            ) {
                Tab.entries.filter { it != Tab.SETTINGS }.forEach { tab ->
                    val selected = selectedTab == tab
                    NavigationBarItem(
                        selected = selected,
                        onClick = { selectedTab = tab },
                        icon = {
                            Icon(
                                when (tab) {
                                    Tab.LEADERBOARD ->
                                        if (selected) Icons.Filled.EmojiEvents else Icons.Outlined.EmojiEvents
                                    Tab.VOTES ->
                                        if (selected) Icons.Filled.HowToVote else Icons.Outlined.HowToVote
                                    Tab.STATUS ->
                                        if (selected) Icons.Filled.Speed else Icons.Outlined.Speed
                                    Tab.SETTINGS -> Icons.Default.Settings
                                },
                                contentDescription = tab.title,
                            )
                        },
                        label = {
                            Text(
                                tab.title,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.onSurface,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    )
                }
            }
        },
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    val forward = targetState.ordinal >= initialState.ordinal
                    (slideInHorizontally(
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    ) { fraction -> if (forward) fraction / 6 else -fraction / 6 } +
                        fadeIn(tween(240)) +
                        scaleIn(tween(260), initialScale = 0.97f)) togetherWith
                        (fadeOut(tween(160)) +
                            slideOutHorizontally(tween(220)) { fraction ->
                                if (forward) -fraction / 8 else fraction / 8
                            } +
                            scaleOut(tween(200), targetScale = 0.99f))
                },
                label = "tabContent",
            ) { tab ->
                when (tab) {
                    Tab.LEADERBOARD -> LeaderboardScreen(
                        players = players,
                        myName = myName,
                        loading = loading,
                        animationsEnabled = animationsEnabled,
                        bestRank = bestRank,
                        onRefresh = vm::refresh,
                    )
                    Tab.VOTES -> VoteScreen(
                        players = players,
                        voteSites = voteSites,
                        voteTimes = voteTimes,
                        onMarkVoted = { site -> settings.markVoted(site) },
                        storedName = myName,
                        onStoreName = settings::setPlayerName,
                        loading = loading,
                        animationsEnabled = animationsEnabled,
                        onRefresh = vm::refresh,
                    )
                    Tab.STATUS -> StatusScreen(
                        status = status,
                        settings = settings,
                        loading = loading,
                        animationsEnabled = animationsEnabled,
                        onRefresh = vm::refresh,
                    )
                    Tab.SETTINGS -> SettingsScreen(
                        version = BuildConfig.VERSION_NAME,
                        darkMode = darkMode,
                        onDarkModeChange = settings::setDarkMode,
                        color = colorName,
                        onColorChange = settings::setColor,
                        animationsEnabled = animationsEnabled,
                        onAnimationsChange = settings::setAnimationsEnabled,
                        hapticsEnabled = hapticsEnabled,
                        onHapticsChange = settings::setHapticsEnabled,
                        compactMode = compactMode,
                        onCompactModeChange = settings::setCompactMode,
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
                        onReplayIntro = settings::replayOnboarding,
                        onOpenAbout = { showAbout = true },
                    )
                }
            }

            // Startbildschirm über dem Inhalt, blendet weich aus.
            BootSplash(
                show = booting && players.isEmpty(),
                animationsEnabled = animationsEnabled,
                logo = { modifier -> BrandLogo(modifier) },
            )
        }
    }

    // Erststart: animierter Willkommens-Flow.
    if (!onboardingDone) {
        BackHandler { settings.setOnboardingDone() }
        WelcomeFlow(
            initialName = myName,
            animationsEnabled = animationsEnabled,
            onConfirm = { name ->
                if (name.isNotBlank()) settings.setPlayerName(name.trim())
                settings.setOnboardingDone()
            },
            onSkip = { settings.setOnboardingDone() },
        )
    }
}
