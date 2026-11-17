package de.mcaffen.app.ui

import android.content.Intent
import android.net.Uri
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
                when {
                    release.critical -> "Update erforderlich ⚠️"
                    blocked != null -> "Update blockiert"
                    failed != null -> "Update fehlgeschlagen"
                    else -> "Update verfügbar 🚀"
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
                    VersionBadge("Aktuell", BuildConfig.VERSION_NAME)
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "aktualisiert auf",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    VersionBadge("Neu", release.tagName.removePrefix("v"), highlighted = true)
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
                if (hasApk) {
                    Text(
                        "Hinweis: Android könnte beim Installieren warnen – das passiert bei Apps, " +
                            "die nicht aus dem Play Store kommen, häufig. Tippe bei der Warnung auf " +
                            "„Details“ und wähle „Trotzdem installieren“.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
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
        title = { Text("Über McAffen-App", style = MaterialTheme.typography.titleLarge) },
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
                    TextButton(onClick = { onOpenLegal("impressum") }) { Text("Impressum") }
                    TextButton(onClick = { onOpenLegal("datenschutz") }) { Text("Datenschutz") }
                    TextButton(onClick = { onOpenLegal("agb") }) { Text("AGB") }
                }
            }
        },
        confirmButton = {
            Button(onClick = onClose) { Text("Schließen") }
        },
    )
}

/** Rechtstext-Dialog als Brücke zum bestehenden Legal-Screen. */
@Composable
internal fun LegalDialogBridge(document: LegalDocument, onDismiss: () -> Unit) {
    LegalDialog(document = document, onDismiss = onDismiss)
}
