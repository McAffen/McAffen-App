package de.mcaffen.app.data

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import de.mcaffen.app.R

/**
 * Öffnet Vote-Seiten auf die vom Nutzer gewählte Art:
 * eingebauter Browser (Custom Tab, bleibt in der App, mit "Zurück" zurück
 * zur Vote-Liste) oder der normale Browser des Geräts.
 */
object VoteBrowser {

    const val IN_APP = "IN_APP"
    const val EXTERNAL = "BROWSER"

    fun open(context: Context, uri: Uri, mode: String) {
        if (mode == EXTERNAL) {
            openExternal(context, uri)
            return
        }
        runCatching {
            val intent = CustomTabsIntent.Builder()
                .setShowTitle(true)
                .setUrlBarHidingEnabled(true)
                .setShareState(CustomTabsIntent.SHARE_STATE_ON)
                .build()
            intent.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.launchUrl(context, uri)
        }.onFailure {
            // Kein Custom-Tab-fähiger Browser installiert → normaler Browser.
            openExternal(context, uri)
        }
    }

    private fun openExternal(context: Context, uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            ContextCompat.startActivity(context, intent, null)
        } catch (_: ActivityNotFoundException) {
            // Kein Browser vorhanden – nichts weiter möglich.
        }
    }
}
