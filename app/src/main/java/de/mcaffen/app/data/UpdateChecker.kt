package de.mcaffen.app.data

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import de.mcaffen.app.model.AppRelease
import de.mcaffen.app.network.GitHubReleaseApi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * Leichtgewichtiger Update-Prüfer gegen die GitHub-Releases-API
 * (`releases/latest`, inklusive Filter für Pre-Releases und Drafts).
 *
 * Eigenständig gebaut, damit sowohl die App (ViewModel) als auch der
 * WorkManager-Worker ihn nutzen können, ohne die komplette App-Abhängigkeit
 * (Room etc.) im Hintergrundprozess aufbauen zu müssen.
 */
object UpdateChecker {
    private val api: GitHubReleaseApi by lazy {
        Retrofit.Builder()
            .baseUrl(UpdateConfig.RELEASE_API_BASE_URL)
            .client(OkHttpClient.Builder().build())
            .addConverterFactory(
                MoshiConverterFactory.create(
                    Moshi.Builder().add(KotlinJsonAdapterFactory()).build(),
                ),
            )
            .build()
            .create(GitHubReleaseApi::class.java)
    }

    /**
     * Liefert das neueste Release des konfigurierten Repositories.
     * Wirft bei API-Fehlern oder wenn nur Pre-Releases existieren eine Exception
     * – die Aufrufer behandeln das als "kein Update verfügbar/Prüfung fehlgeschlagen".
     */
    suspend fun fetchLatest(): AppRelease {
        val release = api.getLatestRelease(UpdateConfig.OWNER, UpdateConfig.REPO)
        // Der /latest-Endpoint liefert ohnehin nie Pre-Releases oder Drafts;
        // wir filtern zusätzlich defensiv, falls sich das Verhalten ändert.
        if (release.draft == true || release.prerelease == true) {
            throw IllegalStateException("Es gibt nur Pre-Releases – diese werden ignoriert")
        }
        val asset = release.assets.firstOrNull { asset ->
            asset.name?.endsWith(".apk", ignoreCase = true) == true ||
                asset.browser_download_url.endsWith(".apk", ignoreCase = true)
        }
        val notes = release.body?.takeIf(String::isNotBlank)
        return AppRelease(
            tagName = release.tag_name,
            htmlUrl = release.html_url,
            name = release.name?.takeIf(String::isNotBlank),
            assetUrl = asset?.browser_download_url,
            assetSize = asset?.size,
            notes = notes,
            critical = UpdateConfig.isCriticalRelease(notes),
        )
    }
}
