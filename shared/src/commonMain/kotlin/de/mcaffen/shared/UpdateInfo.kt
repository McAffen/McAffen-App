package de.mcaffen.shared

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Update-Prüfung gegen die GitHub-Releases des Projekts.
 *
 * iOS bekommt seine Updates über den App Store; dort ist nur der *Hinweis*
 * sinnvoll („Version 1.2 ist im App Store verfügbar“). Die Download-URL des
 * APK-Assets ist nur für Android relevant.
 */
@Serializable
data class GithubAsset(
    val name: String = "",
    @SerialName("browser_download_url") val downloadUrl: String = "",
    val size: Long? = null,
)

@Serializable
data class GithubRelease(
    @SerialName("tag_name") val tagName: String = "",
    @SerialName("html_url") val htmlUrl: String = "",
    val name: String? = null,
    val body: String? = null,
    val draft: Boolean = false,
    val prerelease: Boolean = false,
    val assets: List<GithubAsset> = emptyList(),
)

/** Was die App aus einem Release macht. */
data class ReleaseInfo(
    val tag: String,
    val version: String,
    val notes: String?,
    val assetUrl: String?,
    val assetSize: Long?,
    val htmlUrl: String,
)

object UpdateLogic {

    private val classicVersion = Regex("""^v?(\d+)\.(\d+)\.(\d+)$""")

    /** Vergleicht zwei Versionen; true, wenn [candidate] neuer als [current] ist. */
    fun isNewer(current: String, candidate: String): Boolean {
        val a = parse(current) ?: return false
        val b = parse(candidate) ?: return true
        return compare(b, a) > 0
    }

    private fun parse(value: String): List<Int>? {
        val clean = value.trim().removePrefix("v")
        val match = classicVersion.find(clean) ?: return null
        return match.groupValues.drop(1).mapNotNull { it.toIntOrNull() }
    }

    private fun compare(a: List<Int>, b: List<Int>): Int {
        for (i in 0 until maxOf(a.size, b.size)) {
            val x = a.getOrElse(i) { 0 }
            val y = b.getOrElse(i) { 0 }
            if (x != y) return x.compareTo(y)
        }
        return 0
    }

    /**
     * Wandelt das GitHub-JSON in [ReleaseInfo] um und liefert nur dann ein
     * Ergebnis, wenn das Release neuer als [currentVersion] ist.
     */
    fun newerRelease(
        json: String,
        currentVersion: String,
        requireApk: Boolean = false,
    ): ReleaseInfo? {
        val release = runCatching {
            Json { ignoreUnknownKeys = true }.decodeFromString<GithubRelease>(json)
        }.getOrNull() ?: return null
        if (release.draft || release.prerelease) return null
        val version = release.tagName.removePrefix("v")
        if (!isNewer(currentVersion, version)) return null
        val apk = release.assets.firstOrNull { it.name.endsWith(".apk", ignoreCase = true) }
        if (requireApk && apk == null) return null
        return ReleaseInfo(
            tag = release.tagName,
            version = version,
            notes = release.body,
            assetUrl = apk?.downloadUrl,
            assetSize = apk?.size,
            htmlUrl = release.htmlUrl,
        )
    }

    /** Release-Notes in Zeilen zerlegen, damit die Apps sie gescheit setzen können. */
    fun noteLines(notes: String?): List<String> {
        if (notes.isNullOrBlank()) return emptyList()
        return notes
            .replace("**", "")
            .replace("`", "")
            .replace(Regex("""!\[[^\]]*\]\([^)]*\)"""), "")
            .replace(Regex("""\[([^\]]+)\]\([^)]*\)"""), "$1")
            .lines()
            .map { it.trimStart('#').trim() }
            .filter { it.isNotBlank() }
    }
}
