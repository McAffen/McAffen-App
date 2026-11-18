package de.mcaffen.shared

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Gemeinsame Modelle für Android und iOS. Die Feldnamen entsprechen exakt der
 * Antwort von `https://mcaffen.de/api/web.php?p=…`.
 */

@Serializable
data class LeaderboardEntry(
    val name: String = "",
    val value: Long? = null,
    val skin: String? = null,
) {
    val points: Long get() = value ?: 0L
}

@Serializable
data class LeaderboardResponse(
    val enabled: Boolean = false,
    val sort: String? = null,
    val diff: String? = null,
    val list: List<LeaderboardEntry> = emptyList(),
)

@Serializable
data class RawPlayers(
    val online: Int = 0,
    val max: Int = 0,
)

@Serializable
data class RawStatusServer(
    val id: String? = null,
    val main: Boolean? = null,
    val name: String? = null,
    val host: String? = null,
    val type: String? = null,
    val version: String? = null,
    val online: Boolean = false,
    val maintenance: Boolean = false,
    val players: RawPlayers? = null,
)

@Serializable
data class RawStatus(
    val overall: String? = null,
    @SerialName("checkedAt") val checkedAt: String? = null,
    val servers: List<RawStatusServer> = emptyList(),
    val messages: List<String> = emptyList(),
)

@Serializable
data class RawPlayerVote(
    val site: String? = null,
    val id: String? = null,
    val time: String? = null,
    val timestamp: Long? = null,
)

@Serializable
data class RawPlayerVotes(
    val enabled: Boolean = false,
    val name: String? = null,
    val streak: Int = 0,
    val votes: List<RawPlayerVote> = emptyList(),
)

/** Eine Vote-Seite samt Cooldown und Belohnungstext. */
data class VoteSite(
    val site: String,
    val url: String,
    val hours: Int = 24,
    val reward: String? = null,
)

/** Ein Ranglistenplatz, wie ihn die Oberfläche braucht. */
data class PlayerStat(
    val name: String,
    val score: Int,
    val previousScore: Int? = null,
)

/** Ein Server im Netzwerk, wie ihn die Oberfläche braucht. */
data class ServerInfo(
    val name: String,
    val type: String,
    val online: Boolean,
    val players: Int,
    val version: String? = null,
)

data class ServerStatus(
    val onlinePlayers: Int,
    val networkServers: Int,
    val messages: List<String>,
    val servers: List<ServerInfo> = emptyList(),
)

/** Ranglisten-Werte, die der Server unterstützt. */
enum class BoardSort(val key: String, val label: String) {
    WAVES("waves", "Wellen"),
    KILLS("kills", "Kills"),
    GAMES("games", "Spiele"),
    COINS("coins", "Coins"),
}
