package de.mcaffen.app.data

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import org.json.JSONObject

/** Persistente App-Einstellungen (SharedPreferences + StateFlow für Compose). */
class SettingsStore(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    val darkMode = MutableStateFlow(prefs.getString(KEY_DARK_MODE, null) ?: "SYSTEM")
    val color = MutableStateFlow(prefs.getString(KEY_COLOR, null) ?: "GREEN")
    val playerName = MutableStateFlow(prefs.getString(KEY_PLAYER_NAME, "").orEmpty())
    val onboardingDone = MutableStateFlow(prefs.getBoolean(KEY_ONBOARDED, false))
    val animationsEnabled = MutableStateFlow(prefs.getBoolean(KEY_ANIMATIONS, true))
    val hapticsEnabled = MutableStateFlow(prefs.getBoolean(KEY_HAPTICS, true))
    val compactMode = MutableStateFlow(prefs.getBoolean(KEY_COMPACT, false))

    /**
     * Sprache der Oberfläche: "SYSTEM" (folgt dem Gerät), "de" oder "en".
     * Aufgelöst wird sie in [de.mcaffen.app.ui.AppLanguage].
     */
    val language = MutableStateFlow(prefs.getString(KEY_LANGUAGE, null) ?: "SYSTEM")

    /** Update-Version, die der Nutzer mit "Später" verworfen hat (wird nicht neu gefragt). */
    val skippedUpdate = MutableStateFlow(prefs.getString(KEY_SKIPPED_UPDATE, null))

    /** Automatische Update-Prüfungen an/aus (Nutzerauswahl in den Einstellungen). */
    val updateChecksEnabled = MutableStateFlow(prefs.getBoolean(KEY_UPDATE_CHECKS, true))

    /** Zeitstempel der letzten Update-Prüfung – begrenzt die API-Anfragen (Rate-Limit). */
    val lastUpdateCheck = MutableStateFlow(prefs.getLong(KEY_LAST_UPDATE_CHECK, 0L))

    /**
     * Wie Vote-Seiten geöffnet werden:
     * "IN_APP"  = eingebauter Browser (Custom Tab, direkt in der App),
     * "BROWSER" = der normale Browser des Geräts.
     */
    val voteBrowser = MutableStateFlow(prefs.getString(KEY_VOTE_BROWSER, null) ?: "IN_APP")

    fun setVoteBrowser(value: String) {
        prefs.edit().putString(KEY_VOTE_BROWSER, value).apply()
        voteBrowser.value = value
    }

    val voteTimes = MutableStateFlow(readVoteTimes())

    fun setDarkMode(value: String) {
        prefs.edit().putString(KEY_DARK_MODE, value).apply()
        darkMode.value = value
    }

    fun setColor(value: String) {
        prefs.edit().putString(KEY_COLOR, value).apply()
        color.value = value
    }

    fun setPlayerName(value: String) {
        prefs.edit().putString(KEY_PLAYER_NAME, value).apply()
        playerName.value = value
    }

    fun setOnboardingDone() {
        prefs.edit().putBoolean(KEY_ONBOARDED, true).apply()
        onboardingDone.value = true
    }

    /** Onboarding erneut zeigen (Einstellungen → „Einführung noch einmal ansehen“). */
    fun replayOnboarding() {
        prefs.edit().putBoolean(KEY_ONBOARDED, false).apply()
        onboardingDone.value = false
    }

    /** Bester bisher erreichter Rang – für die „neuer Rekord“-Feier im Profil. */
    fun bestRank(): Int = prefs.getInt(KEY_BEST_RANK, 0)

    fun updateBestRank(rank: Int) {
        if (rank <= 0) return
        val current = bestRank()
        if (current == 0 || rank < current) {
            prefs.edit().putInt(KEY_BEST_RANK, rank).apply()
        }
    }

    fun setAnimationsEnabled(value: Boolean) {
        prefs.edit().putBoolean(KEY_ANIMATIONS, value).apply()
        animationsEnabled.value = value
    }

    fun setHapticsEnabled(value: Boolean) {
        prefs.edit().putBoolean(KEY_HAPTICS, value).apply()
        hapticsEnabled.value = value
    }

    fun setCompactMode(value: Boolean) {
        prefs.edit().putBoolean(KEY_COMPACT, value).apply()
        compactMode.value = value
    }

    /** Sprache setzen ("SYSTEM", "de", "en"). */
    fun setLanguage(value: String) {
        prefs.edit().putString(KEY_LANGUAGE, value).apply()
        language.value = value
    }

    fun skipUpdate(tag: String) {
        prefs.edit().putString(KEY_SKIPPED_UPDATE, tag).apply()
        skippedUpdate.value = tag
    }

    fun setUpdateChecksEnabled(value: Boolean) {
        prefs.edit().putBoolean(KEY_UPDATE_CHECKS, value).apply()
        updateChecksEnabled.value = value
    }

    /** Merkt sich den Zeitpunkt der letzten Update-Prüfung (für das Rate-Limit). */
    fun touchUpdateCheck() {
        val now = System.currentTimeMillis()
        prefs.edit().putLong(KEY_LAST_UPDATE_CHECK, now).apply()
        lastUpdateCheck.value = now
    }

    fun voteTime(site: String): Long = voteTimes.value[site] ?: 0L

    fun markVoted(site: String, timestamp: Long = System.currentTimeMillis()) {
        val json = JSONObject(prefs.getString(KEY_VOTE_TIMES, null) ?: "{}")
        json.put(site, timestamp)
        prefs.edit().putString(KEY_VOTE_TIMES, json.toString()).apply()
        voteTimes.value = readVoteTimes()
    }

    /** Höchste heute gesehene Spielerzahl (wird pro Tag neu gezählt). */
    fun peakToday(): Int {
        val stored = prefs.getString(KEY_PEAK, null) ?: return 0
        val (day, value) = stored.split(":")
        if (day != dayKey()) return 0
        return value.toIntOrNull() ?: 0
    }

    fun touchPeakToday(players: Int): Int {
        val peak = maxOf(peakToday(), players)
        prefs.edit().putString(KEY_PEAK, "${dayKey()}:$peak").apply()
        return peak
    }

    private fun readVoteTimes(): Map<String, Long> {
        val json = JSONObject(prefs.getString(KEY_VOTE_TIMES, null) ?: "{}")
        val result = mutableMapOf<String, Long>()
        json.keys().forEach { key -> result[key] = json.optLong(key, 0L) }
        return result
    }

    private fun dayKey(): String =
        SimpleDateFormat(DAY_FORMAT, Locale.US).format(Date())

    internal companion object {
        const val PREFS_NAME = "mcaffen_settings"
        const val KEY_VOTE_BROWSER = "vote_browser"
        const val KEY_DARK_MODE = "dark_mode"
        const val KEY_COLOR = "color"
        const val KEY_PLAYER_NAME = "player_name"
        const val KEY_ONBOARDED = "onboarding_done"
        const val KEY_ANIMATIONS = "animations_enabled"
        const val KEY_HAPTICS = "haptics_enabled"
        const val KEY_COMPACT = "compact_mode"
        const val KEY_LANGUAGE = "language"
        const val KEY_SKIPPED_UPDATE = "update_skipped"
        const val KEY_UPDATE_CHECKS = "update_checks_enabled"
        const val KEY_LAST_UPDATE_CHECK = "update_last_check"
        const val KEY_VOTE_TIMES = "vote_times"
        const val KEY_PEAK = "peak_today"
        const val KEY_BEST_RANK = "best_rank"
        const val DAY_FORMAT = "yyyyMMdd"
    }
}
