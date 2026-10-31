package de.mcaffen.app.model

import androidx.compose.ui.graphics.Color

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
)

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

// Data model for app releases, extended to hold the download URL of the APK asset if available
data class AppRelease(
    val tagName: String,
    val htmlUrl: String,
    val name: String?,
    // URL of the APK asset on GitHub releases; may be null if no asset found
    val assetUrl: String? = null,
)
