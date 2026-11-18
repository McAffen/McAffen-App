package de.mcaffen.app.model

import androidx.compose.ui.graphics.Color
import android.net.Uri

// Data model for player statistics
data class PlayerStat(
    val name: String,
    val score: Int,
    // Punktestand aus der vorherigen Synchronisierung, für Trend-Anzeige (null = neu).
    val previousScore: Int? = null,
)

// Data model for vote sites
data class VoteSite(
    val site: String,
    val url: String,
    /** Wartezeit bis zum nächsten Vote in Stunden. */
    val hours: Int = 24,
    /** Belohnung pro Vote, wie sie die Website nennt (optional). */
    val reward: String? = null,
) {
    /** Vote-Link mit eingesetztem Spielernamen (die Website nutzt `{user}`). */
    fun urlFor(playerName: String): String =
        url.replace("{user}", Uri.encode(playerName.trim()))
}

// Ein einzelner Server im McAffen-Netzwerk (aus /api/status "servers")
data class ServerInfo(
    val name: String,
    val type: String,
    val online: Boolean,
    val players: Int,
    val version: String? = null,
)

// Data model for server status
data class ServerStatus(
    val onlinePlayers: Int,
    val networkServers: Int,
    val messages: List<String>,
    val servers: List<ServerInfo> = emptyList(),
)

// Data model for app releases
data class AppRelease(
    val tagName: String,
    val htmlUrl: String,
    val name: String?,
    // URL of the APK asset on GitHub releases; may be null if no asset found
    val assetUrl: String? = null,
    // Release-Notes (Beschreibung des Releases im Markdown-Rohformat)
    val notes: String? = null,
    // APK-Downloadgröße in Bytes, wie von GitHub angegeben (null = unbekannt)
    val assetSize: Long? = null,
    // Pflicht-Update: Notes enthalten den [kritisch]-Marker, "Später" wird ausgeblendet
    val critical: Boolean = false,
)
