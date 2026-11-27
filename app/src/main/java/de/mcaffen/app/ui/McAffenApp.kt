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
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import de.mcaffen.app.ui.screens.PlayerScreen
import de.mcaffen.app.ui.screens.InfoContent
import de.mcaffen.app.ui.screens.InfoScreen
import de.mcaffen.app.ui.screens.ModesScreen
import de.mcaffen.app.ui.screens.NewsScreen
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Newspaper
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import coil.compose.AsyncImage
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
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
import de.mcaffen.app.data.UpdateChecker
import de.mcaffen.app.data.UpdateConfig
import de.mcaffen.app.data.UpdateInstaller
import de.mcaffen.app.data.UpdateNotifier
import de.mcaffen.app.model.AppRelease
import de.mcaffen.app.ui.components.BootSplash
import de.mcaffen.app.ui.components.BrandLogo
import de.mcaffen.app.ui.components.PlayerAvatar
import de.mcaffen.app.ui.components.WelcomeFlow
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private enum class Tab(val titleKey: String) {
    LEADERBOARD("tab.leaderboard"),
    NEWS("tab.news"),
    VOTES("tab.votes"),
    STATUS("tab.status"),
    INFO("tab.info"),
    SETTINGS("tab.settings"),
}

/** Spielerprofil als eigenes Blatt, erreichbar über den Kopf oben rechts. */
private enum class PlayerSheet { SEARCH, SETTINGS }

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

    /** Diagnose der letzten Ranglisten-Aktualisierung (sichtbar im Leer-Zustand). */
    val boardResult = repository.lastBoardResult

    // --- Neue Bereiche: Spieler, News, Spielmodi -------------------------
    private val _profile = MutableStateFlow<de.mcaffen.app.network.PlayerProfile?>(null)
    val profile = _profile.asStateFlow()

    private val _profileLoading = MutableStateFlow(false)
    val profileLoading = _profileLoading.asStateFlow()

    private val _profileError = MutableStateFlow<String?>(null)
    val profileError = _profileError.asStateFlow()

    private val _news = MutableStateFlow<List<de.mcaffen.app.network.NewsPostDto>>(emptyList())
    val news = _news.asStateFlow()

    private val _newsLoading = MutableStateFlow(false)
    val newsLoading = _newsLoading.asStateFlow()

    private val _newsError = MutableStateFlow<String?>(null)
    val newsError = _newsError.asStateFlow()

    private val _modes = MutableStateFlow<List<de.mcaffen.app.network.GameModeDto>>(emptyList())
    val modes = _modes.asStateFlow()

    private val _modesLoading = MutableStateFlow(false)
    val modesLoading = _modesLoading.asStateFlow()

    /** Texte in der aktiven Sprache (das ViewModel kennt keine Composables). */
    private val strings: Strings
        get() = Strings.of(AppLanguage.resolve(settings.language.value))

    private fun translated(key: String): String = strings[key]

    /** Spielerprofil über `/api/spieler/{name}` laden. */
    fun loadPlayer(name: String) {
        val clean = name.trim()
        if (clean.isBlank()) {
            _profileError.value = translated("player.enterName")
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            _profileLoading.value = true
            _profileError.value = null
            runCatching { repository.player(clean) }
                .onSuccess { _profile.value = it }
                .onFailure {
                    _profile.value = null
                    _profileError.value = strings.format("player.empty", clean)
                }
            _profileLoading.value = false
        }
    }

    /** News laden (`/api/news`). */
    fun loadNews(force: Boolean = false) {
        if (!force && _news.value.isNotEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            _newsLoading.value = true
            _newsError.value = null
            runCatching { repository.news() }
                .onSuccess { _news.value = it }
                .onFailure {
                    _newsError.value = translated("news.loadError")
                }
            _newsLoading.value = false
        }
    }

    /** Spielmodi laden (`/api/spielmodi`). */
    fun loadModes(force: Boolean = false) {
        if (!force && _modes.value.isNotEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            _modesLoading.value = true
            runCatching { repository.gameModes() }
                .onSuccess { _modes.value = it }
            _modesLoading.value = false
        }
    }

    private val _info = MutableStateFlow(InfoContent())
    val info = _info.asStateFlow()

    private val _infoLoading = MutableStateFlow(false)
    val infoLoading = _infoLoading.asStateFlow()

    private val _infoError = MutableStateFlow<String?>(null)
    val infoError = _infoError.asStateFlow()

    /** Events, FAQ, Regeln, Team und Partner laden; jeder Teil scheitert einzeln. */
    fun loadInfo(force: Boolean = false) {
        if (!force && !_info.value.isEmpty) return
        viewModelScope.launch(Dispatchers.IO) {
            _infoLoading.value = true
            val loaded = coroutineScope {
                val events = async { runCatching { repository.events() }.getOrNull() }
                val faq = async { runCatching { repository.faq() }.getOrNull() }
                val rules = async { runCatching { repository.rules() }.getOrNull() }
                val team = async { runCatching { repository.team() }.getOrNull() }
                val partners = async { runCatching { repository.partners() }.getOrNull() }
                InfoContent(
                    events = events.await() ?: de.mcaffen.app.network.EventsDto(),
                    faq = faq.await().orEmpty(),
                    rules = rules.await().orEmpty(),
                    team = team.await().orEmpty(),
                    partners = partners.await().orEmpty(),
                )
            }
            if (!loaded.isEmpty) _info.value = loaded
            _infoError.value = if (loaded.isEmpty) strings["info.empty"] else null
            _infoLoading.value = false
        }
    }

    /** Nach einem Sprachwechsel: Status holen und bereits geladene Server-Texte erneuern. */
    fun reloadLocalized() {
        refresh()
        if (_news.value.isNotEmpty()) loadNews(force = true)
        if (_modes.value.isNotEmpty()) loadModes(force = true)
        if (!_info.value.isEmpty) loadInfo(force = true)
    }

    /** Live-Test der Rangliste: schreibt das Ergebnis in die Snackbar. */
    fun testLeaderboard() {
        viewModelScope.launch(Dispatchers.IO) {
            val result = runCatching { repository.leaderboard("waves") }
            result.onSuccess { list ->
                _updateNotice.value = strings.format(
                    "update.notice.boardOk",
                    list.size,
                    list.firstOrNull()?.name ?: "–",
                )
            }.onFailure { error ->
                _updateNotice.value = strings.format(
                    "update.notice.boardFailed",
                    error.javaClass.simpleName,
                    error.message.orEmpty(),
                )
            }
        }
    }

    /** Ranglisten-Ansicht: waves, kills, games, coins (Quelle: /ndu/top). */
    private val _boardSort = MutableStateFlow("waves")
    val boardSort = _boardSort.asStateFlow()

    private val _boardPlayers = MutableStateFlow<List<de.mcaffen.app.model.PlayerStat>>(emptyList())
    val boardPlayers = _boardPlayers.asStateFlow()

    private val _boardLoading = MutableStateFlow(false)
    val boardLoading = _boardLoading.asStateFlow()

    /**
     * Wechselt die Ranglisten-Ansicht. „waves“ ist die Standardliste aus dem
     * Cache (auch offline), die anderen Werte kommen live von der API.
     */
    fun selectBoard(sort: String) {
        if (sort == _boardSort.value && _boardPlayers.value.isNotEmpty()) return
        _boardSort.value = sort
        if (sort == "waves") {
            _boardPlayers.value = emptyList()
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            _boardLoading.value = true
            runCatching { repository.leaderboard(sort) }
                .onSuccess { _boardPlayers.value = it }
                .onFailure {
                    _boardPlayers.value = emptyList()
                    _error.value = strings.format("update.notice.boardLoadFailed", sort)
                }
            _boardLoading.value = false
        }
    }
    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()
    private val _update = MutableStateFlow<AppRelease?>(null)
    val update = _update.asStateFlow()

    /** Aus der Benachrichtigung geöffnet: sofort herunterladen und installieren. */
    private val _autoInstall = MutableStateFlow(false)
    val autoInstall = _autoInstall.asStateFlow()

    /**
     * Von der Update-Benachrichtigung aufgerufen: prüft, ob wirklich ein neueres
     * Release vorliegt, und öffnet den Dialog (mit [install] direkt den Download).
     */
    fun handleNotificationOpen(context: Context, install: Boolean) {
        viewModelScope.launch {
            val release = runCatching { UpdateChecker.fetchLatest() }.getOrNull() ?: return@launch
            if (!UpdateConfig.isNewerVersion(release.tagName, BuildConfig.VERSION_NAME)) {
                UpdateNotifier.clear(context)
                _updateNotice.value = strings.format("update.notice.upToDate", BuildConfig.VERSION_NAME)
                return@launch
            }
            _update.value = release
            if (install) {
                _autoInstall.value = true
                startUpdate(context, release)
            }
        }
    }
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
                .onFailure { _error.value = translated("update.notice.refreshFailed") }
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
                        translated("update.notice.offline")
                    } else {
                        strings.format(
                            "update.notice.checkFailed",
                            error.message ?: error::class.simpleName.orEmpty(),
                        )
                    }
                }
            }
        }
    }

    /**
     * Manuell ausgelöster Update-Weg (Einstellungen): prüft sofort, lädt die APK
     * und startet anschließend die Installation – ohne Browser und ohne GitHub.
     * Ist die App schon aktuell, gibt es eine kurze Rückmeldung statt eines Dialogs.
     */
    fun checkAndInstall(context: Context) {
        viewModelScope.launch {
            _updateNotice.value = translated("update.notice.searching")
            val release = runCatching { UpdateChecker.fetchLatest() }.getOrNull()
            if (release == null) {
                _updateNotice.value = translated("update.notice.failed")
                return@launch
            }
            if (!UpdateConfig.isNewerVersion(release.tagName, BuildConfig.VERSION_NAME)) {
                _updateNotice.value = strings.format("update.notice.upToDate", BuildConfig.VERSION_NAME)
                return@launch
            }
            _updateNotice.value = null
            _update.value = release
            startUpdate(context, release)
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
    fun startUpdate(context: Context, release: AppRelease? = _update.value) {
        val apkUrl = release?.assetUrl ?: return
        viewModelScope.launch(Dispatchers.IO) {
            _download.value = DownloadState.Downloading(0)
            runCatching {
                repository.downloadUpdateApk(
                    context = context.applicationContext,
                    url = apkUrl,
                ) { progress ->
                    _download.value = DownloadState.Downloading(progress)
                    UpdateNotifier.updateProgress(context.applicationContext, progress)
                }
            }.fold(
                onSuccess = { apk ->
                    val verification = UpdateInstaller.verifyApk(context.applicationContext, apk)
                    _download.value = if (verification == ApkVerification.Ok) {
                        DownloadState.Ready(apk)
                    } else {
                        DownloadState.Blocked(verification)
                    }
                    if (verification == ApkVerification.Ok) {
                        UpdateNotifier.showReady(context.applicationContext)
                    }
                },
                onFailure = { error ->
                    _download.value = DownloadState.Failed(
                        when (error) {
                            is IOException -> translated("update.notice.downloadOffline")
                            else -> error.message ?: translated("update.notice.downloadFailed")
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
    // Sprache: Einstellung auflösen und die Texte für alle Kinder bereitstellen.
    val languageSetting by settings.language.collectAsStateWithLifecycle()
    val language = AppLanguage.resolve(languageSetting)
    CompositionLocalProvider(LocalStrings provides Strings.of(language)) {
        McAffenContent(repository, settings)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun McAffenContent(repository: McAffenRepository, settings: SettingsStore) {
    val context = LocalContext.current
    val strings = LocalStrings.current
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
    val boardSort by vm.boardSort.collectAsStateWithLifecycle()
    val boardPlayers by vm.boardPlayers.collectAsStateWithLifecycle()
    val boardLoading by vm.boardLoading.collectAsStateWithLifecycle()
    val voteBrowser by settings.voteBrowser.collectAsStateWithLifecycle()
    val boardResult by vm.boardResult.collectAsStateWithLifecycle()
    val profile by vm.profile.collectAsStateWithLifecycle()
    val profileLoading by vm.profileLoading.collectAsStateWithLifecycle()
    val profileError by vm.profileError.collectAsStateWithLifecycle()
    val news by vm.news.collectAsStateWithLifecycle()
    val newsLoading by vm.newsLoading.collectAsStateWithLifecycle()
    val newsError by vm.newsError.collectAsStateWithLifecycle()
    val modes by vm.modes.collectAsStateWithLifecycle()
    val info by vm.info.collectAsStateWithLifecycle()
    val infoLoading by vm.infoLoading.collectAsStateWithLifecycle()
    val infoError by vm.infoError.collectAsStateWithLifecycle()
    val modesLoading by vm.modesLoading.collectAsStateWithLifecycle()
    val animationsEnabled by settings.animationsEnabled.collectAsStateWithLifecycle()
    val darkMode by settings.darkMode.collectAsStateWithLifecycle()
    val colorName by settings.color.collectAsStateWithLifecycle()
    val hapticsEnabled by settings.hapticsEnabled.collectAsStateWithLifecycle()
    val compactMode by settings.compactMode.collectAsStateWithLifecycle()
    val languageSetting by settings.language.collectAsStateWithLifecycle()

    // Aus der Update-Benachrichtigung geöffnet? Dann direkt den Dialog zeigen
    // (und bei „Jetzt installieren“ sofort den Download starten).
    LaunchedEffect(Unit) {
        val activity = context as? android.app.Activity
        val intent = activity?.intent
        if (intent?.getBooleanExtra(UpdateNotifier.EXTRA_SHOW_UPDATE, false) == true) {
            val install = intent.getBooleanExtra(UpdateNotifier.EXTRA_AUTO_INSTALL, false)
            intent.removeExtra(UpdateNotifier.EXTRA_SHOW_UPDATE)
            intent.removeExtra(UpdateNotifier.EXTRA_AUTO_INSTALL)
            vm.handleNotificationOpen(context, install)
        }
    }

    var selectedTab by rememberSaveable { mutableStateOf(Tab.LEADERBOARD) }
    // Spielerprofil liegt oben rechts als Kopf-Icon, nicht in der Leiste.
    var showPlayer by rememberSaveable { mutableStateOf(false) }
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
    // Sprachwechsel: Server-Texte (Meldungen, News, Spielmodi) neu in der neuen Sprache holen.
    var languageSeen by remember { mutableStateOf(languageSetting) }
    LaunchedEffect(languageSetting) {
        if (languageSetting != languageSeen) {
            languageSeen = languageSetting
            vm.reloadLocalized()
        }
    }
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

    // Spielerprofil als Blatt über der App (kein eigener Tab mehr).
    if (showPlayer) {
        ModalBottomSheet(
            onDismissRequest = { showPlayer = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        ) {
            Box(Modifier.heightIn(min = 420.dp)) {
                PlayerScreen(
                    profile = profile,
                    loading = profileLoading,
                    error = profileError,
                    initialName = myName,
                    onSearch = vm::loadPlayer,
                )
            }
        }
    }

    // Daten der neuen Tabs erst laden, wenn der Tab geöffnet wird.
    LaunchedEffect(selectedTab) {
        when (selectedTab) {
            Tab.NEWS -> vm.loadNews()
            Tab.INFO -> vm.loadInfo()
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
        legalDocument(key, AppLanguage.resolve(languageSetting), cached?.title, cached?.html)
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
                                text = { Text(strings["menu.settings"]) },
                                leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) },
                                onClick = {
                                    isMenuExpanded = false
                                    selectedTab = Tab.SETTINGS
                                },
                            )
                            DropdownMenuItem(
                                text = { Text(strings["menu.about"]) },
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
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            strings[selectedTab.titleKey],
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        // Zweite Zeile: live die wichtigste Zahl des Tabs.
                        val subtitle = when (selectedTab) {
                            Tab.LEADERBOARD ->
                                if (players.isEmpty()) strings["subtitle.loading"]
                                else strings.format("subtitle.players", players.size)
                            Tab.NEWS ->
                                if (news.isEmpty()) strings["subtitle.postsLoading"]
                                else strings.format("subtitle.posts", news.size)
                            Tab.VOTES ->
                                if (voteSites.isEmpty()) strings["subtitle.sitesLoading"]
                                else strings.format("subtitle.sites", voteSites.size)
                            Tab.STATUS -> status?.let { strings.format("subtitle.online", it.onlinePlayers) }
                                ?: strings["subtitle.live"]
                            Tab.INFO -> strings["subtitle.info"]
                            Tab.SETTINGS -> strings.format("subtitle.version", BuildConfig.VERSION_NAME)
                        }
                        AnimatedContent(
                            targetState = subtitle,
                            transitionSpec = {
                                (fadeIn(tween(200)) + slideInVertically { it / 2 }) togetherWith
                                    (fadeOut(tween(150)) + slideOutVertically { -it / 2 })
                            },
                            label = "topSubtitle",
                        ) { text ->
                            Text(
                                text,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                            )
                        }
                    }
                },
                actions = {
                    // Spielerprofil: Kopf-Icon statt eigenem Tab.
                    IconButton(onClick = { showPlayer = true }) {
                        Box(
                            Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                            contentAlignment = Alignment.Center,
                        ) {
                            val skin = profile?.skin
                            if (!skin.isNullOrBlank()) {
                                AsyncImage(
                                    model = skin,
                                    contentDescription = strings["cd.playerProfile"],
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape),
                                )
                            } else {
                                Icon(
                                    Icons.Filled.Person,
                                    contentDescription = strings["cd.playerProfile"],
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
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
                            contentDescription = strings["cd.refresh"],
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
                    // Icon hebt und senkt sich beim Wechsel – kleine, ruhige Rückmeldung.
                    val iconScale by animateFloatAsState(
                        targetValue = if (selected) 1.12f else 1f,
                        animationSpec = spring(dampingRatio = 0.55f, stiffness = 420f),
                        label = "navIconScale",
                    )
                    val iconOffset by animateDpAsState(
                        targetValue = if (selected) (-1).dp else 0.dp,
                        animationSpec = spring(dampingRatio = 0.6f, stiffness = 500f),
                        label = "navIconOffset",
                    )
                    NavigationBarItem(
                        selected = selected,
                        onClick = { selectedTab = tab },
                        icon = {
                            Box(
                                Modifier
                                    .offset(y = iconOffset)
                                    .scale(iconScale),
                            ) {
                            Icon(
                                when (tab) {
                                    Tab.LEADERBOARD ->
                                        if (selected) Icons.Filled.EmojiEvents else Icons.Outlined.EmojiEvents
                                    Tab.NEWS ->
                                        if (selected) Icons.Filled.Newspaper else Icons.Outlined.Newspaper
                                    Tab.VOTES ->
                                        if (selected) Icons.Filled.HowToVote else Icons.Outlined.HowToVote
                                    Tab.STATUS ->
                                        if (selected) Icons.Filled.Speed else Icons.Outlined.Speed
                                    Tab.INFO ->
                                        if (selected) Icons.Filled.Info else Icons.Outlined.Info
                                    Tab.SETTINGS -> Icons.Default.Settings
                                },
                                    contentDescription = strings[tab.titleKey],
                                )
                            }
                        },
                        label = {
                            Text(
                                strings[tab.titleKey],
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
                        onRefresh = {
                            vm.refresh()
                            if (boardSort != "waves") vm.selectBoard(boardSort)
                        },
                        boardSort = boardSort,
                        boardPlayers = boardPlayers,
                        boardLoading = boardLoading,
                        onBoardSortChange = vm::selectBoard,
                        boardStatus = boardResult,
                        onTestLeaderboard = vm::testLeaderboard,
                    )
                    Tab.NEWS -> NewsScreen(
                        posts = news,
                        loading = newsLoading,
                        error = newsError,
                        onOpen = { url ->
                            runCatching {
                                context.startActivity(
                                    android.content.Intent(
                                        android.content.Intent.ACTION_VIEW,
                                        android.net.Uri.parse(url),
                                    ),
                                )
                            }
                        },
                        onReload = { vm.loadNews(force = true) },
                    )
                    Tab.INFO -> InfoScreen(
                        content = info,
                        loading = infoLoading,
                        error = infoError,
                        onReload = { vm.loadInfo(force = true) },
                        onOpen = { url ->
                            runCatching {
                                context.startActivity(
                                    android.content.Intent(
                                        android.content.Intent.ACTION_VIEW,
                                        android.net.Uri.parse(url),
                                    ),
                                )
                            }
                        },
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
                        browserMode = voteBrowser,
                    )
                    Tab.STATUS -> StatusScreen(
                        status = status,
                        settings = settings,
                        loading = loading,
                        animationsEnabled = animationsEnabled,
                        onRefresh = vm::refresh,
                        modes = modes,
                        modesLoading = modesLoading,
                        onLoadModes = { vm.loadModes() },
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
                        onInstallUpdate = { vm.checkAndInstall(context) },
                        lastUpdateCheck = lastUpdateCheck,
                        onReplayIntro = settings::replayOnboarding,
                        onOpenAbout = { showAbout = true },
                        voteBrowser = voteBrowser,
                        onVoteBrowserChange = settings::setVoteBrowser,
                        language = languageSetting,
                        onLanguageChange = settings::setLanguage,
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
            language = languageSetting,
            onLanguageChange = settings::setLanguage,
            onConfirm = { name ->
                if (name.isNotBlank()) settings.setPlayerName(name.trim())
                settings.setOnboardingDone()
            },
            onSkip = { settings.setOnboardingDone() },
        )
    }
}
