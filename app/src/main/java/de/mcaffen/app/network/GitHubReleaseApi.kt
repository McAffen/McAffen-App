package de.mcaffen.app.network

import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Data class representing a single asset in a GitHub release. We only need the download URL.
 */
data class ReleaseAsset(
    val browser_download_url: String,
)

/**
 * Data class representing the GitHub release returned by the releases/latest endpoint.
 * The 'assets' list contains all files attached to the release.
 */
data class Release(
    val tag_name: String,
    val html_url: String,
    val name: String?,
    val assets: List<ReleaseAsset>,
)

/**
 * Retrofit interface to fetch the latest release from a GitHub repository.
 */
interface GitHubReleaseApi {
    @GET("repos/{owner}/{repo}/releases/latest")
    suspend fun getLatestRelease(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
    ): Release
}
