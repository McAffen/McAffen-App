package de.mcaffen.app.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import de.mcaffen.app.BuildConfig

/**
 * Reagiert auf die Aktionen in der Update-Benachrichtigung:
 * „Später“ merkt sich die Version (damit sie nicht wieder aufpoppt) und
 * schließt die Benachrichtigung.
 */
class UpdateDismissReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != UpdateNotifier.ACTION_LATER) return
        val settings = SettingsStore(context)
        val tag = intent.getStringExtra(UpdateNotifier.EXTRA_TAG).orEmpty()
        if (tag.isNotBlank()) settings.skipUpdate(tag)
        UpdateNotifier.clear(context)
    }
}

/** Kleiner Helfer: hat dieses Release einen höheren Stand als die App? */
internal fun isNewerRelease(tag: String): Boolean =
    UpdateConfig.isNewerVersion(tag, BuildConfig.VERSION_NAME)
