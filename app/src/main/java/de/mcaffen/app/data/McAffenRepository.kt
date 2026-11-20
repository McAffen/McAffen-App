package de.mcaffen.app.data

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import de.mcaffen.app.model.AppRelease
import de.mcaffen.app.model.PlayerStat
import de.mcaffen.app.model.ServerInfo
import de.mcaffen.app.model.ServerStatus
import de.mcaffen.app.network.McAffenApi
import de.mcaffen.app.ui.AppLanguage
import de.mcaffen.app.ui.Strings
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import android.content.Context
import java.io.File
import okhttp3.OkHttpClient
import okhttp3.Request

/** Rangliste des zweiten Spiels: Sortierschlüssel wie `jump:score` → `spiel=jump&sort=score`. */
const val JUMP_PREFIX = "jump:"
private const val JUMP_GAME = "jump"

/** Rohformat eines Rechtstextes aus dem MC_LEGAL-Objekt der Website. */
data class LegalRaw(val title: String?, val html: String?)

class McAffenRepository(
    private val api: McAffenApi,
    private val dao: CacheDao,
    private val client: OkHttpClient,
    moshi: Moshi,
    private val language: () -> AppLanguage = { AppLanguage.GERMAN },
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

    suspend fun latestRelease(): AppRelease = UpdateChecker.fetchLatest()

    /**
     * lädt die APK eines GitHub-Releases herunter und liefert die fertige Datei
     * für den Install-Prompt (siehe UpdateInstaller).
     */
    suspend fun downloadUpdateApk(
        context: Context,
        url: String,
        onProgress: (Int) -> Unit = {},
    ): File = UpdateInstaller.download(context, client, url, onProgress)

    /**
     * Lädt die Rangliste für einen bestimmten Wert (waves, kills, games, coins)
     * und liefert die Spieler direkt zurück – ohne den Room-Cache zu verändern,
     * damit die Haupt-Rangliste davon unberührt bleibt.
     */
    suspend fun leaderboard(sort: String): List<PlayerStat> {
        // „jump:score“ = Jump'n'Run-Rangliste, alles andere gehört zu Nacht der Untoten.
        val game = if (sort.startsWith(JUMP_PREFIX)) JUMP_GAME else null
        val rows = api.getLeaderboard(sort.removePrefix(JUMP_PREFIX), game)
        return rows
            .filter { it.name.isNotBlank() }
            .map { PlayerStat(it.name, it.score) }
    }

    /** Ergebnis der letzten Ranglisten-Aktualisierung (für Diagnose in der App). */
    private val _lastBoardResult = MutableStateFlow<BoardResult?>(null)
    val lastBoardResult = _lastBoardResult.asStateFlow()

    data class BoardResult(val entries: Int, val error: String?, val at: Long)


    // --- Neue API-Endpunkte (https://mcaffen.de/api) ---------------------

    /** Alle Ranglisten direkt von `/api/rangliste?sort=…` (kein Cache). */
    suspend fun board(sort: String): List<PlayerStat> = leaderboard(sort)

    /** Partner und Creator. */
    suspend fun partners() = api.getPartners()

    /** Spielerprofil mit NDU-Statistiken, Rängen, Votes und Skin. */
    suspend fun player(name: String) = api.getPlayer(name.trim())

    /** Server-Info: Adresse, Version, Discord, Socials, Logo. */
    suspend fun info() = api.getInfo()

    /** News-Beiträge. */
    suspend fun news() = api.getNews()

    /** Events und wöchentliche Termine. */
    suspend fun events() = api.getEvents()

    /** Spielmodi. */
    suspend fun gameModes() = api.getGameModes()

    /** Team. */
    suspend fun team() = api.getTeam()

    /** FAQ. */
    suspend fun faq() = api.getFaq()

    /** Regeln. */
    suspend fun rules() = api.getRules()

    /** Ideen. */
    suspend fun ideas() = api.getIdeas()

    suspend fun refresh() = coroutineScope {
        // Rangliste und Status liegen auf demselben Endpunkt; jeder Teil wird
        // einzeln abgesichert, damit ein Fehler den anderen nicht mitreißt.
        val leaderboard = async { runCatching { api.getLeaderboard("waves") } }
        val serverStatus = async { runCatching { api.getServerStatus() } }

        leaderboard.await().onFailure { error ->
            // Nicht still verschlucken: der Nutzer soll sehen, warum nichts kommt.
            android.util.Log.w("McAffen", "Rangliste fehlgeschlagen: ${error.javaClass.simpleName}: ${error.message}", error)
        }
        leaderboard.await().onSuccess { rows ->
            val entries = rows
                .filter { it.name.isNotBlank() }
                .sortedByDescending { it.score }
            val oldScores = dao.currentPlayers().associate { it.name to it.score }
            val mapped = entries.map { entry ->
                PlayerEntity(entry.name, entry.score, oldScores[entry.name]?.takeIf { it != entry.score })
            }
            // Eine leere Rangliste (z. B. Server gerade zurückgesetzt) darf den
            // letzten Stand nicht löschen – aber wenn der Server antwortet, wird
            // die Liste vollständig ersetzt, damit keine alten Plätze stehen bleiben.
            if (mapped.isNotEmpty()) dao.savePlayers(mapped)
            _lastBoardResult.value = BoardResult(mapped.size, null, System.currentTimeMillis())
        }
        leaderboard.await().onFailure { error ->
            _lastBoardResult.value = BoardResult(
                0,
                "${error.javaClass.simpleName}: ${error.message ?: "ohne Meldung"}",
                System.currentTimeMillis(),
            )
        }

        serverStatus.await().onSuccess { raw ->
            val infos = raw.servers.map { server ->
                ServerInfo(
                    name = server.name ?: server.id ?: "Server",
                    type = server.type ?: if (server.main == true) "main" else "",
                    online = server.online,
                    players = server.players?.online ?: 0,
                    version = server.version,
                )
            }
            val online = raw.servers.sumOf { it.players?.online ?: 0 }
            val networkServers = raw.servers.size.takeIf { it > 0 } ?: infos.size
            val messages = raw.messages
                .ifEmpty { infos.filter { it.online }.map { Strings.of(language()).format("status.fallback.playersOnline", it.name, it.players) } }
            dao.saveStatus(
                ServerStatusEntity(
                    onlinePlayers = online,
                    networkServers = networkServers,
                    messages = messages.joinToString("\n"),
                    servers = infos.takeIf { it.isNotEmpty() }?.let { serversAdapter.toJson(it) },
                ),
            )
        }

        // Vote-Seiten liefert die Website nur eingebettet aus; sie stehen deshalb
        // fest in der App (wie auf mcaffen.de) und werden einmalig gespeichert.
        // Immer schreiben (REPLACE), damit Korrekturen an den Seiten auch bei
        // bereits installierten Apps ankommen – z. B. entfernte, tote Seiten.
        dao.saveVoteSites(
            DefaultVoteSites.ALL.map { VoteSiteEntity(it.site, it.url, it.hours, it.reward) },
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
