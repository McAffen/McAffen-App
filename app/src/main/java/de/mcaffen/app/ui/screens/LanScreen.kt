package de.mcaffen.app.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Lan
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.mcaffen.app.data.LanBridge
import de.mcaffen.app.ui.components.AnimatedBackdrop
import de.mcaffen.app.ui.components.PillChip
import de.mcaffen.app.ui.components.Reveal
import de.mcaffen.app.ui.components.SectionTitle
import de.mcaffen.app.ui.components.SoftCard

/**
 * LAN-Welt für Konsolen: erklärt, wie bedrock.mcaffen.de im eigenen WLAN als
 * LAN-Welt für PS5, Switch, Xbox & Co. auftaucht – inklusive echter IP-Adresse
 * dieses Geräts und Kopier-Knopf für die Zieladresse.
 */
@Composable
internal fun LanScreen(
    animationsEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var showSteps by remember { mutableStateOf(true) }
    val localIp = remember { LanBridge.localAddress(context) }
    val onWifi = remember { LanBridge.isOnWifi(context) }
    val address = "${LanBridge.HOST}:${LanBridge.PORT}"

    Box(modifier.fillMaxSize()) {
        AnimatedBackdrop(enabled = animationsEnabled)
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = 24.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item("hero") {
                Reveal(visible = animationsEnabled) {
                    SoftCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(26.dp),
                        contentPadding = 18.dp,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Box(
                                Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(15.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    Icons.Default.Lan,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }
                            Column(Modifier.weight(1f)) {
                                Text(
                                    "LAN-Welt für Konsolen",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    "PS5 · Switch · Xbox · Smart-TV",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            PillChip(
                                text = if (onWifi) "Im WLAN" else "Kein WLAN",
                                color = if (onWifi) {
                                    de.mcaffen.app.ui.McAffenBrand.colors.positive
                                } else {
                                    de.mcaffen.app.ui.McAffenBrand.colors.negative
                                },
                            )
                        }
                        Text(
                            "Der Bedrock-Server läuft im Internet – Konsolen suchen Welten aber nur " +
                                "im eigenen Netz. Mit einer Weiterleitung im WLAN erscheint " +
                                "bedrock.mcaffen.de auf der Konsole als ganz normale LAN-Welt.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            item("address") {
                Reveal(visible = animationsEnabled, fromScale = 0.97f) {
                    SoftCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        contentPadding = 16.dp,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        SectionTitle("Adressen")
                        AddressRow(label = "Ziel-Server", value = address) {
                            copyToClipboard(context, address)
                        }
                        AddressRow(
                            label = "Dieses Gerät im WLAN",
                            value = localIp ?: "nicht im WLAN",
                        ) { localIp?.let { copyToClipboard(context, it) } }
                        Text(
                            "Port ${LanBridge.PORT} ist der Standard-Port von Minecraft Bedrock. " +
                                "Auf der Konsole wird nichts eingetippt – die Welt erscheint in der Liste.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            item("toggle") {
                OutlinedButton(
                    onClick = { showSteps = !showSteps },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Icon(Icons.Default.Wifi, contentDescription = null, modifier = Modifier.size(18.dp))
                    androidx.compose.foundation.layout.Spacer(Modifier.width(8.dp))
                    Text(if (showSteps) "Anleitung ausblenden" else "Anleitung anzeigen")
                }
            }

            if (showSteps) {
                itemsIndexed(LanBridge.steps(localIp)) { index, step ->
                    Reveal(visible = animationsEnabled, fromScale = 0.98f) {
                        SoftCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = 14.dp,
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Box(
                                    Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        "${index + 1}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
                                Text(
                                    step.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                            Text(
                                step.detail,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            item("honest") {
                SoftCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = 14.dp,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    SectionTitle("Gut zu wissen")
                    Text(
                        "Die App kann die Weiterleitung nicht selbst übernehmen: Android erlaubt " +
                            "keiner normalen App einen offenen Port unter 1024 und keine " +
                            "LAN-Ankündigung ohne Systemrechte. Deshalb läuft der letzte Schritt " +
                            "über ein zweites Gerät im selben WLAN (PC, Laptop oder eine " +
                            "LAN-Proxy-App).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        "Für Handy und PC brauchst du das nicht – dort einfach " +
                            "bedrock.mcaffen.de als Server eintragen.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun AddressRow(label: String, value: String, onCopy: (() -> Unit)?) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                value,
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
            )
        }
        if (onCopy != null) {
            Button(
                onClick = onCopy,
                shape = RoundedCornerShape(12.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 12.dp,
                    vertical = 6.dp,
                ),
            ) {
                Icon(
                    Icons.Default.ContentCopy,
                    contentDescription = "Kopieren",
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    clipboard?.setPrimaryClip(ClipData.newPlainText("McAffen", text))
    Toast.makeText(context, "$text kopiert", Toast.LENGTH_SHORT).show()
}
