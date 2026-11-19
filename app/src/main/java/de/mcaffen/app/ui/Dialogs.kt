package de.mcaffen.app.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.mcaffen.app.BuildConfig
import de.mcaffen.app.data.ApkVerification
import de.mcaffen.app.data.UpdateInstaller
import de.mcaffen.app.model.AppRelease
import de.mcaffen.app.ui.components.SoftCard

/** Fortschritt des direkten In-App-Updates (APK-Download + System-Install-Prompt). */
sealed interface DownloadState {
    data object Idle : DownloadState
    data class Downloading(val progress: Int) : DownloadState
    data class Ready(val apk: java.io.File) : DownloadState
    /** APK heruntergeladen, aber nicht als Update über diese Installation nutzbar. */
    data class Blocked(val verification: ApkVerification) : DownloadState
    data class Failed(val message: String) : DownloadState
}

/**
 * Moderner Update-Dialog: Versionsübergang, Release-Notes, Downloadgröße,
 * Fortschritt und klare nächste Schritte.
 */
@Composable
internal fun UpdateDialog(
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
    val context = LocalContext.current
    var cellularConfirmed by remember { mutableStateOf(false) }
    val onCellular = remember {
        runCatching {
            val cm = context.getSystemService(android.net.ConnectivityManager::class.java)
            val network = cm?.activeNetwork
            val caps = network?.let { cm.getNetworkCapabilities(it) }
            caps?.hasTransport(android.net.NetworkCapabilities.TRANSPORT_CELLULAR) == true &&
                caps.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI) != true
        }.getOrDefault(false)
    }
    val downloading = state is DownloadState.Downloading
    val blocked = state as? DownloadState.Blocked
    val failed = state as? DownloadState.Failed
    val hasApk = !release.assetUrl.isNullOrBlank()
    // Kritische Updates (Marker in den Release-Notes) dürfen nicht weggeklickt werden.
    val dismissible = !release.critical && !downloading
    AlertDialog(
        onDismissRequest = { if (dismissible) onDismiss() },
        shape = RoundedCornerShape(28.dp),
        title = {
            Text(
                val strings = LocalStrings.current
                when {
                    release.critical -> strings["update.required"]
                    blocked != null -> strings["update.blocked"]
                    failed != null -> strings["update.failed"]
                    else -> strings["update.available"]
                },
                style = MaterialTheme.typography.headlineSmall,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    VersionBadge(LocalStrings.current["update.current"], BuildConfig.VERSION_NAME)
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = LocalStrings.current["update.cd.updatedTo"],
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    VersionBadge(LocalStrings.current["update.new"], release.tagName.removePrefix("v"), highlighted = true)
                }
                if (release.critical) {
                    Text(
                        LocalStrings.current["update.critical"],
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                val noteLines = updateNoteLines(release.notes)
                if (noteLines.isNotEmpty()) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        Text(
                            "Was ist neu?",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        // Struktur statt Textblock: Überschriften fett, Punkte mit
                        // Einzug, Absätze in ruhiger Farbe.
                        noteLines.forEach { line ->
                            when (line.kind) {
                                NoteLine.Kind.HEADING -> Text(
                                    line.text,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 4.dp),
                                )
                                NoteLine.Kind.BULLET -> Row(
                                    Modifier.padding(start = if (line.level > 0) 14.dp else 2.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Box(
                                        Modifier
                                            .padding(top = 6.dp)
                                            .size(4.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary),
                                    )
                                    Text(
                                        line.text,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                                NoteLine.Kind.DIVIDER -> HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                )
                                NoteLine.Kind.PARAGRAPH -> Text(
                                    line.text,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
                Text(
                    LocalStrings.current["update.explanation"],
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (hasApk) {
                    Text(
                        LocalStrings.current["update.warning"],
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                formatBytes(release.assetSize)?.let { size ->
                    Text(
                        formatString("update.size", size),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (onCellular && state is DownloadState.Idle) {
                    Text(
                        LocalStrings.current["update.cellular"],
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
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
                                formatString("update.downloadingPercent", state.progress),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        } else {
                            LinearProgressIndicator(Modifier.fillMaxWidth())
                            Text(LocalStrings.current["update.downloading"], style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    is DownloadState.Failed -> Text(
                        formatString("update.error", state.message),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                    is DownloadState.Blocked -> Text(
                        val strings = LocalStrings.current
                        when (state.verification) {
                            ApkVerification.Corrupt -> strings["update.corrupt"]
                            ApkVerification.Incompatible -> strings["update.incompatible"]
                            else -> strings["update.otherKey"]
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                    else -> {}
                }
                if (!canInstall && state is DownloadState.Idle && hasApk) {
                    Text(
                        LocalStrings.current["update.allow"],
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        confirmButton = {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                when {
                    val strings = LocalStrings.current
                    downloading -> TextButton(onClick = {}, enabled = false) { Text(strings["update.loading"]) }
                    failed != null || blocked?.verification == ApkVerification.Corrupt ->
                        Button(onClick = onRetry) { Text(strings["update.retry"]) }
                    !hasApk || (blocked != null && blocked.verification != ApkVerification.Corrupt) ->
                        Button(onClick = onRetry) { Text(strings["update.download"]) }
                    !canInstall -> Button(onClick = onEnableInstalls) { Text(strings["update.allowInstalls"]) }
                    onCellular && !cellularConfirmed -> Button(onClick = { cellularConfirmed = true }) {
                        Text(strings["update.confirmCellular"])
                    }
                    else -> Button(onClick = onInstall) { Text(strings["update.installNow"]) }
                }
                // GitHub nur noch als Notfallweg – der normale Weg ist der
                // direkte Download und die Installation in der App.
                TextButton(onClick = onOpenGitHub) { Text(LocalStrings.current["update.github"]) }
            }
        },
        dismissButton = {
            if (dismissible) {
                TextButton(onClick = onLater) { Text(LocalStrings.current["update.later"]) }
            }
        },
    )
}

/** Kleiner Versions-Chip: Label darüber, kompakte Versionsnummer in der Mitte. */
@Composable
private fun VersionBadge(label: String, version: String, highlighted: Boolean = false) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (highlighted) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        },
    ) {
        Column(
            Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                version,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
            )
        }
    }
}

/** „Über“-Dialog mit Markenlogo, Version, Rechtstexten und GitHub-Link. */
@Composable
internal fun AboutDialog(
    version: String,
    onOpenLegal: (String) -> Unit,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onClose,
        shape = RoundedCornerShape(28.dp),
        title = { Text(LocalStrings.current["about.title"], style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AboutContent()
                SoftCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    contentPadding = 12.dp,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        "Open Source auf GitHub – Feedback und Pull Requests sind willkommen.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    TextButton(
                        onClick = {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/McAffen/McAffen-App")),
                            )
                        },
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                    ) { Text("github.com/McAffen/McAffen-App") }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val strings = LocalStrings.current
                    TextButton(onClick = { onOpenLegal("impressum") }) { Text(strings["settings.legal.imprint"]) }
                    TextButton(onClick = { onOpenLegal("datenschutz") }) { Text(strings["settings.legal.privacy"]) }
                    TextButton(onClick = { onOpenLegal("agb") }) { Text(strings["settings.legal.terms"]) }
                }
            }
        },
        confirmButton = {
            Button(onClick = onClose) { Text(LocalStrings.current["common.close"]) }
        },
    )
}

/** Rechtstext-Dialog als Brücke zum bestehenden Legal-Screen. */
@Composable
internal fun LegalDialogBridge(document: LegalDocument, onDismiss: () -> Unit) {
    LegalDialog(document = document, onDismiss = onDismiss)
}
