package de.mcaffen.app.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow

/** Persistente App-Einstellungen (SharedPreferences + StateFlow für Compose). */
class SettingsStore(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    val darkMode = MutableStateFlow(prefs.getString(KEY_DARK_MODE, null) ?: "SYSTEM")
    val color = MutableStateFlow(prefs.getString(KEY_COLOR, null) ?: "GREEN")
    val playerName = MutableStateFlow(prefs.getString(KEY_PLAYER_NAME, "").orEmpty())

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

    private companion object {
        const val PREFS_NAME = "mcaffen_settings"
        const val KEY_DARK_MODE = "dark_mode"
        const val KEY_COLOR = "color"
        const val KEY_PLAYER_NAME = "player_name"
    }
}
