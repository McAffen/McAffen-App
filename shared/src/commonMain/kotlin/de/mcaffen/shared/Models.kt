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

// ---------------------------------------------------------------------------
// Neue, dokumentierte API (https://mcaffen.de/api)
// ---------------------------------------------------------------------------

/** Ein Ranglistenplatz von `/api/rangliste`. */
@Serializable
data class RankRow(
    val name: String = "",
    val score: Int = 0,
)

/** Nacht-der-Untoten-Werte eines Spielers. */
@Serializable
data class NduStats(
    val bestWave: Int = 0,
    val kills: Int = 0,
    val games: Int = 0,
    val coins: Int = 0,
    val rankWaves: Int = 0,
    val rankKills: Int = 0,
    val lastPlayed: String? = null,
)

/** Votes eines Spielers. */
@Serializable
data class PlayerVoteStats(
    val month: Int = 0,
    val total: Int = 0,
    val streak: Int = 0,
)

/** Antwort von `/api/spieler/{name}`. */
@Serializable
data class PlayerProfile(
    val name: String = "",
    val nachtDerUntoten: NduStats? = null,
    val votes: PlayerVoteStats? = null,
    val skin: String? = null,
)

/** Antwort von `/api/info`. */
@Serializable
data class ServerInfo2(
    val name: String = "",
    val address: String = "",
    val edition: String? = null,
    val version: String? = null,
    val website: String? = null,
    val discord: String? = null,
    val discordMembers: Int = 0,
    val socials: List<SocialLink> = emptyList(),
    val logo: String? = null,
    val shop: String? = null,
    val support: String? = null,
)

@Serializable
data class SocialLink(
    val name: String = "",
    val key: String? = null,
    val url: String = "",
)

/** Ein News-Beitrag von `/api/news`. */
@Serializable
data class NewsPost(
    val slug: String = "",
    val title: String = "",
    val excerpt: String? = null,
    val image: String? = null,
    val author: String? = null,
    val category: String? = null,
    val date: String? = null,
    val url: String? = null,
)

/** Antwort von `/api/events`. */
@Serializable
data class Events(
    val upcoming: List<EventItem> = emptyList(),
    val weekly: List<EventItem> = emptyList(),
)

@Serializable
data class EventItem(
    val title: String = "",
    val date: String? = null,
    val time: String? = null,
    val description: String? = null,
    val weekday: String? = null,
)

/** Ein Spielmodus von `/api/spielmodi`. */
@Serializable
data class GameMode(
    val name: String = "",
    val subtitle: String? = null,
    val description: String? = null,
    val tags: List<String> = emptyList(),
    val image: String? = null,
    val status: String? = null,
    val statusLabel: String? = null,
)

/** Ein Teammitglied von `/api/team`. */
@Serializable
data class TeamMember(
    val name: String = "",
    val role: String? = null,
    val description: String? = null,
    val avatar: String? = null,
)

/** Ein Partner von `/api/partner`. */
@Serializable
data class Partner(
    val name: String = "",
    val description: String? = null,
    val url: String? = null,
    val logo: String? = null,
)

/** Ein FAQ-Eintrag von `/api/faq`. */
@Serializable
data class FaqEntry(
    val question: String = "",
    val answer: String = "",
)

/** Eine Regel von `/api/regeln`. */
@Serializable
data class Rule(
    val title: String = "",
    val text: String = "",
    val category: String? = null,
)

/** Eine Idee von `/api/ideen`. */
@Serializable
data class Idea(
    val title: String = "",
    val text: String? = null,
    val status: String? = null,
    val author: String? = null,
    val votes: Int = 0,
)
