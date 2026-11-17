package de.mcaffen.app.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import de.mcaffen.app.BuildConfig
import de.mcaffen.app.ui.McAffenBrand
import de.mcaffen.app.ui.ThemeOptions
import de.mcaffen.app.ui.components.AnimatedBackdrop
import de.mcaffen.app.ui.components.BrandLogo
import de.mcaffen.app.ui.components.PillChip
import de.mcaffen.app.ui.components.SectionTitle
import de.mcaffen.app.ui.components.SoftCard
import de.mcaffen.app.ui.lastCheckText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Einstellungen: Profil, Darstellung mit Live-Vorschau, Effekte, Updates,
 * Aktionen, Rechtliches und App-Info.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SettingsScreen(
    version: String,
    darkMode: String,
    onDarkModeChange: (String) -> Unit,
    color: String,
    onColorChange: (String) -> Unit,
    animationsEnabled: Boolean,
    onAnimationsChange: (Boolean) -> Unit,
    hapticsEnabled: Boolean,
    onHapticsChange: (Boolean) -> Unit,
    compactMode: Boolean,
    onCompactModeChange: (Boolean) -> Unit,
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
    onReplayIntro: () -> Unit,
    onOpenAbout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val notifPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { }

    Box(modifier.fillMaxSize()) {
        AnimatedBackdrop(enabled = animationsEnabled)
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item("profile") {
                SoftCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(26.dp),
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.surfaceContainerHigh,
                    ),
                    contentPadding = 18.dp,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        BrandLogo(Modifier.size(52.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                "McAffen",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                            )
                            Text(
                                if (playerName.isBlank()) "Noch kein Spielername" else playerName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        PillChip(text = "v$version", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            item("name") {
                SettingsCard(
                    title = "Profil",
                    icon = Icons.Default.Person,
                ) {
                    OutlinedTextField(
                        value = playerName,
                        onValueChange = onPlayerNameChange,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        label = { Text("Dein Minecraft-Name") },
                        supportingText = {
                            Text("Wird beim Voten vorausgefüllt und hebt dich in der Rangliste hervor.")
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        ),
                    )
                }
            }

            item("appearance") {
                SettingsCard(
                    title = "Darstellung",
                    icon = Icons.Default.Palette,
                ) {
                    Text(
                        "Dark-Mode",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    SegmentedOptions(
                        options = ThemeOptions.darkModes,
                        selected = darkMode,
                        onSelect = onDarkModeChange,
                    )
                    Text(
                        "Farbschema",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        ThemeOptions.colors.forEach { (value, label) ->
                            ColorSwatch(
                                label = label,
                                color = ThemeOptions.previewColor(value),
                                selected = color == value,
                                onClick = { onColorChange(value) },
                            )
                        }
                    }
                }
            }

            item("effects") {
                SettingsCard(
                    title = "Effekte & Komfort",
                    icon = Icons.Default.AutoAwesome,
                ) {
                    SettingsSwitchRow(
                        title = "Animationen",
                        subtitle = "Übergänge, Pulse und Effekte in der App.",
                        checked = animationsEnabled,
                        onCheckedChange = onAnimationsChange,
                    )
                    SettingsSwitchRow(
                        title = "Haptisches Feedback",
                        subtitle = "Sanftes Vibrieren bei Aktionen.",
                        icon = Icons.Default.Vibration,
                        checked = hapticsEnabled,
                        onCheckedChange = onHapticsChange,
                    )
                    SettingsSwitchRow(
                        title = "Kompakte Ansicht",
                        subtitle = "Weniger Abstände und kleinere Karten.",
                        checked = compactMode,
                        onCheckedChange = onCompactModeChange,
                    )
                }
            }

            item("updates") {
                SettingsCard(
                    title = "Updates",
                    icon = Icons.Default.SystemUpdate,
                ) {
                    SettingsSwitchRow(
                        title = "Automatisch nach Updates suchen",
                        subtitle = "Beim Start, beim Zurückkehren und ca. alle 12 Stunden im Hintergrund.",
                        checked = updateChecksEnabled,
                        onCheckedChange = { enabled ->
                            // Android 13+: Benachrichtigungserlaubnis nachfragen,
                            // sonst bliebe ein Hintergrund-Update unsichtbar.
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

            item("actions") {
                SettingsCard(
                    title = "Aktionen",
                    icon = Icons.Default.Refresh,
                ) {
                    Button(
                        onClick = onRefresh,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text("  Daten aktualisieren", fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = onReplayIntro,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text("  Einführung noch einmal ansehen")
                    }
                    OutlinedButton(
                        onClick = onOpenAbout,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text("  Über die App")
                    }
                    OutlinedButton(
                        onClick = {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/McAffen/McAffen-App")),
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Text("  Auf GitHub öffnen")
                    }
                }
            }

            item("legal") {
                SettingsCard(
                    title = "Rechtliches",
                    icon = Icons.Default.Gavel,
                ) {
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
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        LegalChip("Impressum") { onOpenLegal("impressum") }
                        LegalChip("Datenschutz") { onOpenLegal("datenschutz") }
                        LegalChip("AGB") { onOpenLegal("agb") }
                    }
                }
            }

            item("info") {
                SoftCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    contentPadding = 16.dp,
                ) {
                    SectionTitle("App-Info")
                    InfoRow("Version", version)
                    InfoRow("Server", "mcaffen.de")
                    InfoRow("API", "https://mcaffen.de/api")
                    Text(
                        "Inoffizielle App für den Minecraft-Server mcaffen.de. Nicht mit Mojang oder Microsoft verbunden.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit,
) {
    SoftCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        contentPadding = 16.dp,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(17.dp),
                )
            }
            Text(title, style = MaterialTheme.typography.titleMedium)
        }
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) { content() }
    }
}

/** Umschalter-Zeile mit Titel, Erklärung und Icon. */
@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: ImageVector? = null,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (icon != null) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
            )
        }
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(
                subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
            ),
        )
    }
}

/** Segmentierte Auswahl mit gleitender Markierung. */
@Composable
private fun SegmentedOptions(
    options: List<Pair<String, String>>,
    selected: String,
    onSelect: (String) -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        options.forEach { (value, label) ->
            val active = value == selected
            val bg by animateColorAsState(
                targetValue = if (active) MaterialTheme.colorScheme.primary else Color.Transparent,
                label = "segBg",
            )
            val fg by animateColorAsState(
                targetValue = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                label = "segFg",
            )
            Box(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(bg)
                    .clickable { onSelect(value) }
                    .padding(vertical = 9.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    label,
                    style = MaterialTheme.typography.labelLarge,
                    color = fg,
                    fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                )
            }
        }
    }
}

/** Farbpunkt mit Name und Auswahl-Häkchen. */
@Composable
private fun ColorSwatch(
    label: String,
    color: Color,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val ringWidth by animateDpAsState(
        targetValue = if (selected) 3.dp else 1.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "ringWidth",
    )
    Row(
        Modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) color.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(color, color.copy(alpha = 0.7f))))
                .border(ringWidth, if (selected) color else MaterialTheme.colorScheme.outlineVariant, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(13.dp),
                )
            }
        }
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
        )
    }
}

@Composable
private fun LegalChip(label: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.width(8.dp))
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

/** „Über“-Overlay-Inhalt mit Markenlogo, Version und Kurzbeschreibung. */
@Composable
internal fun AboutContent() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BrandLogo(Modifier.size(44.dp))
            Column(Modifier.padding(start = 12.dp)) {
                Text("McAffen-App", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Version ${BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Text(
            "Inoffizielle App für den Minecraft-Server mcaffen.de – Rangliste, Voten und " +
                "Live-Status in einer Oberfläche.",
            style = MaterialTheme.typography.bodySmall,
        )
        PillChip(text = "Keine Zugangsdaten · kein Tracking", color = McAffenBrand.colors.positive)
        Spacer(Modifier.height(2.dp))
    }
}
