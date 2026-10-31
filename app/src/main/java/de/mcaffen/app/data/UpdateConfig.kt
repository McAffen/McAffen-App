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
}
