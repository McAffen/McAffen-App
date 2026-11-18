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

    /** Rangliste für einen Sortierwert (waves, kills, games, coins). */
    suspend fun leaderboard(sort: BoardSort = BoardSort.WAVES): List<PlayerStat> {
        val response: LeaderboardResponse = getRaw("/ndu/top&sort=${sort.key}")
        return response.list
            .filter { it.name.isNotBlank() }
            .sortedByDescending { it.points }
            .map {
                PlayerStat(
                    name = it.name,
                    score = it.points.coerceIn(0L, Int.MAX_VALUE.toLong()).toInt(),
                )
            }
    }

    /** Netzwerkstatus inklusive Serverliste. */
    suspend fun status(): ServerStatus {
        val raw: RawStatus = getRaw("/status")
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

    fun close() = http.close()

    companion object {
        const val BASE_URL = "https://mcaffen.de/api/web.php"
    }
}
