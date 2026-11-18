package de.mcaffen.app.network

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Rohformat eines Ranglisten-Eintrags von `api/web.php?p=/leaderboard`.
 * Je nach Modus heißt der Wert `value` (ELO) oder `score` – beides wird akzeptiert.
 */
data class LeaderboardEntry(
    val name: String = "",
    val value: Long? = null,
    val score: Long? = null,
    val elo: Long? = null,
) {
    /** Punktestand in der Einheit des gewählten Modus (ELO, Stunden, Level, Siege, Geld). */
    val points: Long
        get() = value ?: score ?: elo ?: 0L
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
    /** Rangliste; Modus: global, survival, skyblock, bedwars, minigames, citybuild. */
    @GET("web.php")
    suspend fun getLeaderboard(
        @Query("p") path: String = "/leaderboard?mode=global&period=season&limit=100",
    ): List<LeaderboardEntry>

    /** Serverseitiges Netzwerk-Status-Objekt inkl. Serverliste. */
    @GET("web.php")
    suspend fun getServerStatus(@Query("p") path: String = "/status"): RawStatus

    /** Bestätigte Votes eines Spielers (nur wenn das Vote-Plugin aktiv ist). */
    @GET("web.php")
    suspend fun getPlayerVotes(@Query("p") path: String): RawPlayerVotes
}
