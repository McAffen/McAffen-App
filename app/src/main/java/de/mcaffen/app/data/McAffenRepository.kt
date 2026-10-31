package de.mcaffen.app.data

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import de.mcaffen.app.model.AppRelease
import de.mcaffen.app.model.ServerInfo
import de.mcaffen.app.model.ServerStatus
import de.mcaffen.app.network.GitHubReleaseApi
import de.mcaffen.app.network.McAffenApi
import de.mcaffen.app.network.RepoInfo
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.OkHttpClient
import okhttp3.Request

/** Rohformat eines Rechtstextes aus dem MC_LEGAL-Objekt der Website. */
data class LegalRaw(val title: String?, val html: String?)

class McAffenRepository(
    private val api: McAffenApi,
    private val releaseApi: GitHubReleaseApi,
    private val dao: CacheDao,
    private val client: OkHttpClient,
    moshi: Moshi,
) {
    val players = dao.players().map { items -> items.map(PlayerEntity::toModel) }
    val voteSites = dao.voteSites().map { items -> items.map(VoteSiteEntity::toModel) }

    /** Status inklusive per-Server-Liste; servers steckt als JSON im Room-Cache. */
    val status: Flow<ServerStatus?> = dao.status().map { entity ->
        entity?.let { cached ->
            val servers = cached.servers?.takeIf(String::isNotBlank)?.let { json ->
                runCatching { serversAdapter.fromJson(json) }.getOrNull()
            }.orEmpty()
            ServerStatus(cached.onlinePlayers, cached.networkServers, cached.messages.lines(), servers)
        }
    }

    /** Synchronisierte Rechtstexte nach ID (impressum, datenschutz, agb). */
    val legalCache: Flow<Map<String, LegalCacheEntity>> =
        dao.legalCache().map { items -> items.associateBy { it.id } }

    private val legalAdapter = moshi.adapter<Map<String, LegalRaw>>(
        Types.newParameterizedType(Map::class.java, String::class.java, LegalRaw::class.java),
    )

    private val serversAdapter = moshi.adapter<List<ServerInfo>>(
        Types.newParameterizedType(List::class.java, ServerInfo::class.java),
    )

    suspend fun latestRelease(): AppRelease {
        val release = releaseApi.getLatestRelease(RepoInfo.OWNER, RepoInfo.REPO)
        val asset = release.assets.firstOrNull { it.browser_download_url.endsWith(".apk", ignoreCase = true) }
        return AppRelease(release.tag_name, release.html_url, release.name, asset?.browser_download_url)
    }

    suspend fun refresh() = coroutineScope {
        val leaderboard = async { api.getLeaderboard() }
        val votes = async { api.getVoteSites() }
        val serverStatus = async { api.getServerStatus() }
        val oldScores = dao.currentPlayers().associate { it.name to it.score }
        dao.savePlayers(
            leaderboard.await().map { stat ->
                PlayerEntity(stat.name, stat.score, oldScores[stat.name]?.takeIf { it != stat.score })
            },
        )
        dao.saveVoteSites(votes.await().map { VoteSiteEntity(it.site, it.url) })
        val current = serverStatus.await()
        dao.saveStatus(
            ServerStatusEntity(
                onlinePlayers = current.onlinePlayers,
                networkServers = current.networkServers,
                messages = current.messages.joinToString("\n"),
                servers = current.servers.takeIf { it.isNotEmpty() }?.let { serversAdapter.toJson(it) },
            ),
        )
    }

    /**
     * Lädt die aktuellen Rechtstexte von https://mcaffen.de/ und speichert sie
     * als Cache. Die Startseite bettet die Texte als window.MC_LEGAL-JSON ein.
     * Gibt true zurück, wenn neue Texte gespeichert wurden.
     */
    suspend fun refreshLegal(): Boolean {
        val request = Request.Builder()
            .url("https://mcaffen.de/")
            .build()
        val json = try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return false
                response.body?.string() ?: return false
            }
        } catch (error: Exception) {
            return false
        }
        val match = LEGAL_JSON_REGEX.find(json) ?: return false
        val documents = try {
            legalAdapter.fromJson(match.groupValues[1])
        } catch (error: Exception) {
            return false
        } ?: return false
        val now = System.currentTimeMillis()
        val items = documents.mapNotNull { (id, raw) ->
            val html = raw.html?.takeIf(String::isNotBlank) ?: return@mapNotNull null
            LegalCacheEntity(
                id = id,
                title = raw.title?.takeIf(String::isNotBlank) ?: id,
                html = html,
                updatedAt = now,
            )
        }
        if (items.isEmpty()) return false
        dao.saveLegal(items)
        return true
    }

    private companion object {
        /** Die Website bettet die Rechtstexte als JSON mit \u003C-escapeten Tags ein. */
        val LEGAL_JSON_REGEX = Regex("window\\.MC_LEGAL\\s*=\\s*(\\{[\\s\\S]*?\\});")
    }
}
