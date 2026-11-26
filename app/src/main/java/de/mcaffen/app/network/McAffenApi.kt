package de.mcaffen.app.network

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Antwort von `api/web.php?p=/ndu/top&sort=<wert>` – die einzige funktionierende
 * Rangliste der Website (die alten Pfade `/leaderboard`, `/top` liefern 404).
 * `sort` gibt der Server zurück; `value` ist die Zahl passend zum Sortierfeld.
 */
data class LeaderboardResponse(
    val enabled: Boolean = false,
    val sort: String? = null,
    val diff: String? = null,
    val list: List<LeaderboardEntry> = emptyList(),
)

/** Ein Platz der Rangliste. `skin` ist ein Textur-Hash (nur auf der Website genutzt). */
data class LeaderboardEntry(
    val name: String = "",
    val value: Long? = null,
    val skin: String? = null,
) {
    val points: Long get() = value ?: 0L
}

/**
 * Ein Server aus `api/web.php?p=/status`.
 * `players` ist ein Objekt ({online, max}) oder – defensiv – eine Zahl.
 */
data class RawStatusServer(
    val id: String? = null,
    val main: Boolean? = null,
    val name: String? = null,
    val host: String? = null,
    val type: String? = null,
    val version: String? = null,
    val online: Boolean = false,
    val maintenance: Boolean = false,
    /**
     * Spielerzahl. Die aktuelle API liefert eine Zahl, die ältere ein Objekt
     * (`{"online": 3, "max": 20}`); beides wird akzeptiert, siehe [playerCount].
     */
    val players: Any? = null,
)

/** Spieler online auf diesem Server – egal ob die API eine Zahl oder ein Objekt sendet. */
val RawStatusServer.playerCount: Int
    get() = when (val value = players) {
        is Number -> value.toInt()
        is Map<*, *> -> (value["online"] as? Number)?.toInt() ?: 0
        else -> 0
    }

/** Antwort von `api/web.php?p=/status` (Gesamtbild des Netzwerks). */
data class RawStatus(
    val onlinePlayers: Int? = null,
    val networkServers: Int? = null,
    val overall: String? = null,
    val checkedAt: String? = null,
    val servers: List<RawStatusServer> = emptyList(),
    val messages: List<String> = emptyList(),
)

/** Antwort von `api/web.php?p=/votes/player/{name}` (serverseitig bestätigte Votes). */
data class RawPlayerVotes(
    val enabled: Boolean = false,
    val name: String? = null,
    val streak: Int = 0,
    val votes: List<RawPlayerVote> = emptyList(),
)

data class RawPlayerVote(
    val site: String? = null,
    val id: String? = null,
    val time: String? = null,
    val timestamp: Long? = null,
)

/**
 * Endpunkte der mcaffen.de-Website.
 *
 * Die Website liefert ihre Daten über einen einzigen JSON-Endpunkt:
 * `https://mcaffen.de/api/web.php?p=<pfad>`. Die alten Pfade
 * (`/rangliste`, `/voten`, `/status`) geben inzwischen HTML zurück.
 */
interface McAffenApi {
    /**
     * Rangliste über die dokumentierte API: `/api/rangliste?sort=waves|kills|games|coins`.
     * Antwort ist eine schlichte Liste `[{name, score}]` – kein `p=`-Parameter mehr,
     * deshalb entfällt die frühere Kodierungsfalle.
     */
    @GET("rangliste")
    suspend fun getLeaderboard(
        @Query("sort") sort: String = "waves",
        // `jump` = Jump'n'Run (sort: score, runs, blocks); ohne Angabe Nacht der Untoten.
        @Query("spiel") game: String? = null,
    ): List<RankRow>

    /** Netzwerkstatus: `/api/status` (gleiches Format wie zuvor). */
    @GET("status")
    suspend fun getServerStatus(): RawStatus

    /** Spielerprofil mit NDU-Statistiken, Rängen, Votes und Skin. */
    @GET("spieler/{name}")
    suspend fun getPlayer(@Path("name") name: String): PlayerProfile

    /** Server-Info: Adresse, Version, Discord, Socials, Logo. */
    @GET("info")
    suspend fun getInfo(): ServerInfoDto

    /** News-Beiträge. */
    @GET("news")
    suspend fun getNews(): List<NewsPostDto>

    /** Ein einzelner Beitrag. */
    @GET("news/{slug}")
    suspend fun getNewsPost(@Path("slug") slug: String): NewsPostDto

    /** Kommende Events und wöchentliche Termine. */
    @GET("events")
    suspend fun getEvents(): EventsDto

    /** Spielmodi. */
    @GET("spielmodi")
    suspend fun getGameModes(): List<GameModeDto>

    /** Team. */
    @GET("team")
    suspend fun getTeam(): List<TeamMemberDto>

    /** Partner. */
    @GET("partner")
    suspend fun getPartners(): List<PartnerDto>

    /** FAQ. */
    @GET("faq")
    suspend fun getFaq(): List<FaqEntryDto>

    /** Regeln. */
    @GET("regeln")
    suspend fun getRules(): List<RuleDto>

    /** Ideen. */
    @GET("ideen")
    suspend fun getIdeas(): List<IdeaDto>

    /** Bestätigte Votes eines Spielers (nur wenn das Vote-Plugin aktiv ist). */
    @GET("web.php")
    suspend fun getPlayerVotes(
        @Query("p", encoded = true) path: String,
    ): RawPlayerVotes
}

// ---------------------------------------------------------------------------
// Neue, dokumentierte API: https://mcaffen.de/api
// Read-only, kein Login, ?lang=en für englische Texte.
// ---------------------------------------------------------------------------

/** Ein Platz von `/api/rangliste?sort=waves|kills|games|coins`. */
data class RankRow(
    val name: String = "",
    val score: Int = 0,
)

/** Nacht-der-Untoten-Werte eines Spielers (`/api/spieler/{name}`). */
data class NduStats(
    val bestWave: Int = 0,
    val kills: Int = 0,
    val games: Int = 0,
    val coins: Int = 0,
    val rankWaves: Int = 0,
    val rankKills: Int = 0,
    val lastPlayed: String? = null,
)

data class PlayerVoteStats(
    val month: Int = 0,
    val total: Int = 0,
    val streak: Int = 0,
)

data class PlayerProfile(
    val name: String = "",
    val nachtDerUntoten: NduStats? = null,
    val votes: PlayerVoteStats? = null,
    val skin: String? = null,
)

data class SocialLink(
    val name: String = "",
    val key: String? = null,
    val url: String = "",
)

data class ServerInfoDto(
    val name: String = "",
    val address: String = "",
    val edition: String? = null,
    val version: String? = null,
    val website: String? = null,
    val discord: String? = null,
    val discordMembers: Int = 0,
    val socials: List<SocialLink> = emptyList(),
    val logo: String? = null,
    val support: String? = null,
)

data class NewsPostDto(
    val slug: String = "",
    val title: String = "",
    val excerpt: String? = null,
    val image: String? = null,
    val author: String? = null,
    val category: String? = null,
    val date: String? = null,
    val url: String? = null,
)

data class EventItemDto(
    val title: String = "",
    val date: String? = null,
    val time: String? = null,
    val description: String? = null,
    val weekday: String? = null,
)

data class EventsDto(
    val upcoming: List<EventItemDto> = emptyList(),
    val weekly: List<EventItemDto> = emptyList(),
)

data class GameModeDto(
    val name: String = "",
    val subtitle: String? = null,
    val description: String? = null,
    val tags: List<String> = emptyList(),
    val image: String? = null,
    val status: String? = null,
    val statusLabel: String? = null,
)

data class TeamMemberDto(
    val name: String = "",
    val role: String? = null,
    val description: String? = null,
    val avatar: String? = null,
)

data class PartnerDto(
    val name: String = "",
    val description: String? = null,
    val url: String? = null,
    val logo: String? = null,
)

data class FaqEntryDto(
    val question: String = "",
    val answer: String = "",
)

data class RuleDto(
    val title: String = "",
    val text: String = "",
    val category: String? = null,
)

data class IdeaDto(
    val title: String = "",
    val text: String? = null,
    val status: String? = null,
    val author: String? = null,
    val votes: Int = 0,
)
