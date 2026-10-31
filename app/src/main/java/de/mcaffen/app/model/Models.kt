package de.mcaffen.app.model

import androidx.compose.ui.graphics.Color

// Data model for player statistics
data class PlayerStat(
    val name: String,
    val score: Int,
)

// Data model for vote sites
data class VoteSite(
    val site: String,
    val url: String,
)

// Data model for server status
data class ServerStatus(
    val onlinePlayers: Int,
    val networkServers: Int,
    val messages: List<String>,
)

// Data model for app releases, extended to hold the download URL of the APK asset if available
data class AppRelease(
    val tagName: String,
    val htmlUrl: String,
    val name: String?,
    // URL of the APK asset on GitHub releases; may be null if no asset found
    val assetUrl: String? = null,
)
