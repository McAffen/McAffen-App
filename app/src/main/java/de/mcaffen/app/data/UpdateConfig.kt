package de.mcaffen.app.data

import de.mcaffen.app.network.RepoInfo

/**
 * Zentrale Konfiguration des automatischen GitHub-Update-Systems.
 * Owner, Repository, Prüfintervalle und der Marker für kritische Updates
 * werden ausschließlich hier gepflegt – keine anderen Dateien nötig.
 */
object UpdateConfig {
    /** GitHub-Repository, aus dem die Updates bezogen werden. */
    val OWNER: String = RepoInfo.OWNER
    val REPO: String = RepoInfo.REPO

    /** Basis-URL der GitHub-Releases-API. */
    const val RELEASE_API_BASE_URL = "https://api.github.com/"

    /** Mindestabstand zwischen automatischen Update-Prüfungen (keine unnötigen API-Anfragen). */
    const val MIN_CHECK_INTERVAL_MS = 30L * 60 * 1000

    /** Zyklus der regelmäßigen Hintergrund-Prüfung über WorkManager. */
    const val BACKGROUND_CHECK_INTERVAL_MS = 12L * 60 * 60 * 1000

    /**
     * Marker in den Release-Notes, der ein Update zum Pflicht-Update macht:
     * Der Dialog lässt sich dann nicht mehr mit „Später" schließen.
     */
    const val CRITICAL_MARKER = "[kritisch]"

    /** Ein Release gilt als kritisch, wenn die Notes den Marker enthalten (Groß-/Kleinschreibung egal). */
    fun isCriticalRelease(notes: String?): Boolean =
        notes != null && notes.contains(CRITICAL_MARKER, ignoreCase = true)

    /**
     * Vergleicht zwei Versionsnummern (v-Prefix wird ignoriert, fehlende Teile = 0):
     * true, wenn [candidate] strikt neuer als [current] ist – also nur dann ein
     * Update angeboten wird, wenn es wirklich eine höhere Version gibt.
     */
    fun isNewerVersion(candidate: String, current: String): Boolean {
        // Datumsversionen wie 1.261118.1 sind nicht numerisch vergleichbar
        // (261118 > 0.35, obwohl der Build neuer ist). Deshalb: Sobald ein
        // Name nicht das klassische X.Y.Z-Schema hat, zählt er als neuer –
        // das eigentliche Update entscheidet ohnehin der versionCode.
        if (!CLASSIC_VERSION.matches(candidate.trim()) || !CLASSIC_VERSION.matches(current.trim())) {
            return candidate.trim() != current.trim()
        }
        fun parts(value: String) = value.trim().removePrefix("v").split('.')
            .map { it.takeWhile(Char::isDigit).toIntOrNull() ?: 0 }
        val newer = parts(candidate)
        val installed = parts(current)
        for (index in 0 until maxOf(newer.size, installed.size)) {
            val candidatePart = newer.getOrElse(index) { 0 }
            val currentPart = installed.getOrElse(index) { 0 }
            if (candidatePart != currentPart) return candidatePart > currentPart
        }
        return false
    }

    /** Klassisches Schema v1.0.35 – nur damit ist ein numerischer Vergleich sinnvoll. */
    private val CLASSIC_VERSION = Regex("^v?[0-9]{1,2}\\.[0-9]{1,3}\\.[0-9]{1,3}$")
}
