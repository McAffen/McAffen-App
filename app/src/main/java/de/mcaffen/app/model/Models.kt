package de.mcaffen.app.model

data class PlayerStat(
    val name: String,
    val score: Int,
)

data class VoteSite(
    val site: String,
    val url: String,
)

data class ServerStatus(
    val onlinePlayers: Int,
    val networkServers: Int,
    val messages: List<String>,
)

data class AppRelease(
    val tagName: String,
    val htmlUrl: String,
    val name: String?,
)
