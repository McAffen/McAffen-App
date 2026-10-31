package de.mcaffen.app.network

import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Data class representing a single asset in a GitHub release.
 * Name and size are used to pick the APK and show its download size.
 */
data class ReleaseAsset(
    val browser_download_url: String,
    /** Dateiname des Assets (z. B. "McAffen-v1.0.4.apk"), optional. */
    val name: String? = null,
    /** Größe des Assets in Bytes (falls von GitHub geliefert). */
    val size: Long? = null,
)

/**
 * Data class representing the GitHub release returned by the releases/latest endpoint.
 * The 'assets' list contains all files attached to the release.
 */
data class Release(
    val tag_name: String,
    val html_url: String,
    val name: String? = null,
    /** Release-Beschreibung (Markdown-Rohformat) – Basis für die Release-Notes in der App. */
    val body: String? = null,
    /** /releases/latest liefert nie Pre-Releases; wir filtern dennoch defensiv. */
    val prerelease: Boolean? = null,
    val draft: Boolean? = null,
    val assets: List<ReleaseAsset> = emptyList(),
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
