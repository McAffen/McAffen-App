package de.mcaffen.app.data

import de.mcaffen.app.model.ServerStatus
import de.mcaffen.app.model.AppRelease
import de.mcaffen.app.network.McAffenApi
import de.mcaffen.app.network.GitHubReleaseApi
import de.mcaffen.app.network.RepoInfo
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class McAffenRepository(
    private val api: McAffenApi,
    private val releaseApi: GitHubReleaseApi,
    private val dao: CacheDao,
) {
    val players = dao.players().map { items -> items.map(PlayerEntity::toModel) }
    val voteSites = dao.voteSites().map { items -> items.map(VoteSiteEntity::toModel) }
    val status = dao.status().map { it?.toModel() }

    suspend fun latestRelease(): AppRelease {
        val release = releaseApi.getLatestRelease(RepoInfo.OWNER, RepoInfo.REPO)
        val asset = release.assets.firstOrNull { it.browser_download_url.endsWith(".apk", ignoreCase = true) }
        return AppRelease(release.tag_name, release.html_url, release.name, asset?.browser_download_url)
    }

    suspend fun refresh() = coroutineScope {
        val leaderboard = async { api.getLeaderboard() }
        val votes = async { api.getVoteSites() }
        val serverStatus = async { api.getServerStatus() }
        dao.savePlayers(leaderboard.await().map { PlayerEntity(it.name, it.score) })
        dao.saveVoteSites(votes.await().map { VoteSiteEntity(it.site, it.url) })
        val current = serverStatus.await()
        dao.saveStatus(
            ServerStatusEntity(
                onlinePlayers = current.onlinePlayers,
                networkServers = current.networkServers,
                messages = current.messages.joinToString("\n"),
            ),
        )
    }
}
