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

    /** Update-Version, die der Nutzer mit "Später" verworfen hat (wird nicht neu gefragt). */
    val skippedUpdate = MutableStateFlow(prefs.getString(KEY_SKIPPED_UPDATE, null))

    /** Wann wurde welche Vote-Seite zuletzt vom Nutzer geöffnet (2-stündige/24-h-Rhythmus der Vote-Seiten). */
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

    fun skipUpdate(tag: String) {
        prefs.edit().putString(KEY_SKIPPED_UPDATE, tag).apply()
        skippedUpdate.value = tag
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

    private companion object {
        const val PREFS_NAME = "mcaffen_settings"
        const val KEY_DARK_MODE = "dark_mode"
        const val KEY_COLOR = "color"
        const val KEY_PLAYER_NAME = "player_name"
        const val KEY_ONBOARDED = "onboarding_done"
        const val KEY_SKIPPED_UPDATE = "update_skipped"
        const val KEY_VOTE_TIMES = "vote_times"
        const val KEY_PEAK = "peak_today"
        const val DAY_FORMAT = "yyyyMMdd"
    }
}
