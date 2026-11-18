package de.mcaffen.shared

import io.ktor.client.engine.HttpClientEngine

/** Plattform-Engine: OkHttp auf Android, NSURLSession auf iOS. */
expect fun defaultEngine(): HttpClientEngine

/** Gemeinsame API, wie sie die Apps benutzen: `McAffenApi()` in Swift. */
class McAffenApi {
    private val client = McAffenClient(defaultEngine())

    suspend fun leaderboard(sort: BoardSort): List<PlayerStat> = client.leaderboard(sort)

    suspend fun status(): ServerStatus = client.status()

    suspend fun playerVotes(name: String): RawPlayerVotes = client.playerVotes(name)

    fun close() = client.close()
}
