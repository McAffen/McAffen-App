package de.mcaffen.app.network

import retrofit2.http.GET
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
    val players: RawPlayers? = null,
)

data class RawPlayers(
    val online: Int = 0,
    val max: Int = 0,
)

/** Antwort von `api/web.php?p=/status` (Gesamtbild des Netzwerks). */
data class RawStatus(
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
     * Rangliste des NDU-Servers. `sort`: waves, kills, games, coins.
     * `diff` (optional): easy, normal, hardcore.
     */
    // `encoded = true` ist hier entscheidend: Retrofit würde `/` und `&` sonst
    // kodieren (%2F, %26) und der Server antwortet mit 404. Der Pfad muss so
    // ankommen, wie die Website ihn aufruft: p=/ndu/top&sort=waves
    @GET("web.php")
    suspend fun getLeaderboard(
        @Query("p", encoded = true) path: String = "/ndu/top&sort=waves",
    ): LeaderboardResponse

    /** Rangliste nach einem bestimmten Wert (waves, kills, games, coins). */
    @GET("web.php")
    suspend fun getLeaderboardSorted(
        @Query("p", encoded = true) path: String,
    ): LeaderboardResponse

    /** Serverseitiges Netzwerk-Status-Objekt inkl. Serverliste. */
    @GET("web.php")
    suspend fun getServerStatus(
        @Query("p", encoded = true) path: String = "/status",
    ): RawStatus

    /** Bestätigte Votes eines Spielers (nur wenn das Vote-Plugin aktiv ist). */
    @GET("web.php")
    suspend fun getPlayerVotes(
        @Query("p", encoded = true) path: String,
    ): RawPlayerVotes
}
