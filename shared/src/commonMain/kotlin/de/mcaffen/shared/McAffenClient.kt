package de.mcaffen.shared

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.URLBuilder
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Zugriff auf die mcaffen.de-API – einmal geschrieben, von Android und iOS
 * benutzt.
 *
 * Wichtig: Die Website erwartet den Pfad als *rohen* Query-Wert, also
 * `?p=/ndu/top&sort=waves`. Deshalb wird der Wert über `encodedParameters`
 * gesetzt; sonst würde `/` zu `%2F` und der Server antwortet mit 404.
 */
class McAffenClient(engine: HttpClientEngine) {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    private val http = HttpClient(engine) {
        expectSuccess = false
        install(ContentNegotiation) { json(json) }
        install(HttpTimeout) {
            requestTimeoutMillis = 15_000
            connectTimeoutMillis = 10_000
            socketTimeoutMillis = 15_000
        }
    }

    private suspend inline fun <reified T> getRaw(path: String): T {
        val url = URLBuilder(BASE_URL).apply {
            encodedParameters.append("p", path)
        }.build()
        return http.get(url).body()
    }

    /** Rangliste: `/api/rangliste?sort=waves|kills|games|coins`. */
    suspend fun leaderboard(sort: BoardSort = BoardSort.WAVES): List<PlayerStat> {
        val url = URLBuilder("$API_BASE/rangliste").apply {
            parameters.append("sort", sort.key)
        }.build()
        val rows: List<RankRow> = http.get(url).body()
        return rows
            .filter { it.name.isNotBlank() }
            .map { PlayerStat(name = it.name, score = it.score) }
    }

    /** Netzwerkstatus inklusive Serverliste. */
    suspend fun status(): ServerStatus {
        val raw: RawStatus = http.get("$API_BASE/status").body()
        val servers = raw.servers.map { server ->
            ServerInfo(
                name = server.name ?: server.id ?: "Server",
                type = server.type ?: "",
                online = server.online && !server.maintenance,
                players = server.players?.online ?: 0,
                version = server.version,
            )
        }
        return ServerStatus(
            onlinePlayers = servers.filter { it.online }.sumOf { it.players },
            networkServers = servers.size,
            messages = raw.messages,
            servers = servers,
        )
    }

    /** Serverseitig bestätigte Votes eines Spielers. */
    suspend fun playerVotes(name: String): RawPlayerVotes =
        getRaw("/votes/player/${name.trim()}")


    /** Über den Server: Adresse, Version, Discord, Socials, Logo. */
    suspend fun info(): ServerInfo2 = http.get("$API_BASE/info").body()

    /** Ein Spieler mit Nacht-der-Untoten-Statistiken, Rängen, Votes und Skin. */
    suspend fun player(name: String): PlayerProfile =
        http.get("$API_BASE/spieler/${encodeSegment(name)}").body()

    /** News-Beiträge (Blog). */
    suspend fun news(): List<NewsPost> = http.get("$API_BASE/news").body()

    /** Ein einzelner Beitrag. */
    suspend fun news(slug: String): NewsPost =
        http.get("$API_BASE/news/${encodeSegment(slug)}").body()

    /** Kommende Events und wöchentliche Termine. */
    suspend fun events(): Events = http.get("$API_BASE/events").body()

    /** Spielmodi. */
    suspend fun gameModes(): List<GameMode> = http.get("$API_BASE/spielmodi").body()

    /** Team-Mitglieder. */
    suspend fun team(): List<TeamMember> = http.get("$API_BASE/team").body()

    /** Partner. */
    suspend fun partners(): List<Partner> = http.get("$API_BASE/partner").body()

    /** Häufige Fragen. */
    suspend fun faq(): List<FaqEntry> = http.get("$API_BASE/faq").body()

    /** Regeln. */
    suspend fun rules(): List<Rule> = http.get("$API_BASE/regeln").body()

    /** Ideen-Vorschläge. */
    suspend fun ideas(): List<Idea> = http.get("$API_BASE/ideen").body()

    /** Übersicht aller Endpunkte (Selbstbeschreibung der API). */
    suspend fun overview(): String = http.get("$API_BASE").body()

    /** Pfad-Segmente (Spielername, Slug) URL-sicher machen. */
    private fun encodeSegment(value: String): String =
        value.trim().replace(" ", "%20")

    fun close() = http.close()

    companion object {
        const val BASE_URL = "https://mcaffen.de/api/web.php"
        /** Offizielle, dokumentierte API (read-only, kein Login). */
        const val API_BASE = "https://mcaffen.de/api"
    }
}
