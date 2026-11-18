package de.mcaffen.app.data

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import androidx.core.content.ContextCompat
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import de.mcaffen.app.BuildConfig
import de.mcaffen.app.R
import de.mcaffen.app.model.AppRelease
import java.util.concurrent.TimeUnit

/**
 * Regelmäßige Update-Prüfung im Hintergrund über WorkManager (alle 12 Stunden,
 * nur mit Internet). Findet sie ein neues Release (das nicht mit „Später"
 * ausgedrückt wurde), erscheint eine Benachrichtigung, die die App öffnet.
 */
class UpdateCheckWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val context = applicationContext
        val settings = SettingsStore(context)
        if (!settings.updateChecksEnabled.value) return Result.success()

        // Rate-Limit: zwischen zwei Prüfungen liegt immer mindestens ein Mindestabstand,
        // damit keine unnötigen API-Anfragen entstehen.
        val sinceLast = System.currentTimeMillis() - settings.lastUpdateCheck.value
        if (sinceLast in 0 until UpdateConfig.MIN_CHECK_INTERVAL_MS) return Result.success()

        // Fehler werden still als "dieser Durchlauf übersprungen" behandelt;
        // die nächste periodische Ausführung probiert es erneut.
        val release = runCatching { UpdateChecker.fetchLatest() }.getOrNull()
        settings.touchUpdateCheck()

        if (release != null) {
            val isSkipped = release.tagName == settings.skippedUpdate.value
            val isNewer = UpdateConfig.isNewerVersion(release.tagName, BuildConfig.VERSION_NAME)
            if ((isNewer && !isSkipped) || release.critical) {
                UpdateNotifier.showUpdateAvailable(context, release)
            }
        }
        return Result.success()
    }
}

/**
 * Verwaltet die periodische Hintergrund-Prüfung. [update] richtet sie ein,
 * wenn automatische Prüfungen aktiviert sind – und räumt sie ab, wenn sie
 * vom Nutzer ausgeschaltet wurden. Wird vom App-Start und aus den
 * Einstellungen aufgerufen, sodass der Zustand sich selbst heilt.
 */
object UpdateCheckScheduler {
    private const val UNIQUE_NAME = "mcaffen-update-check"
    private const val ONCE_NAME = "mcaffen-update-check-once"

    /** Sofortige Einmal-Prüfung im Hintergrund (z. B. direkt nach dem Start). */
    fun checkNow(context: Context) {
        val settings = SettingsStore(context)
        if (!settings.updateChecksEnabled.value) return
        val request = androidx.work.OneTimeWorkRequestBuilder<UpdateCheckWorker>()
            .setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build(),
            )
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(ONCE_NAME, androidx.work.ExistingWorkPolicy.REPLACE, request)
    }

    fun update(context: Context) {
        val settings = SettingsStore(context)
        val workManager = WorkManager.getInstance(context)
        if (!settings.updateChecksEnabled.value) {
            workManager.cancelUniqueWork(UNIQUE_NAME)
            return
        }
        val request = PeriodicWorkRequestBuilder<UpdateCheckWorker>(
            UpdateConfig.BACKGROUND_CHECK_INTERVAL_MS,
            TimeUnit.MILLISECONDS,
        )
            // Die ersten Stunden springt der Start-Check der App an: Der Hintergrund-Lauf
            // darf sich Zeit lassen, damit keine API-Anfrage doppelt aufeinandert.
            .setInitialDelay(UpdateConfig.MIN_CHECK_INTERVAL_MS, TimeUnit.MILLISECONDS)
            .setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build(),
            )
            .build()
        workManager.enqueueUniquePeriodicWork(UNIQUE_NAME, ExistingPeriodicWorkPolicy.UPDATE, request)
    }
}

/** Benachrichtigung über ein verfügbares Update (Klick öffnet die App). */
object UpdateNotifier {
    private const val CHANNEL_ID = "mcaffen_updates"
    private const val NOTIFICATION_ID = 4242

    /** Extras, mit denen MainActivity den Update-Dialog direkt öffnet. */
    const val EXTRA_SHOW_UPDATE = "de.mcaffen.app.SHOW_UPDATE"
    const val EXTRA_AUTO_INSTALL = "de.mcaffen.app.AUTO_INSTALL"
    const val ACTION_LATER = "de.mcaffen.app.UPDATE_LATER"
    const val EXTRA_TAG = "de.mcaffen.app.UPDATE_TAG"

    fun showUpdateAvailable(context: Context, release: AppRelease) {
        val manager = NotificationManagerCompat.from(context)
        if (Build.VERSION.SDK_INT >= 33 &&
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) != PackageManager.PERMISSION_GRANTED &&
            !manager.areNotificationsEnabled()
        ) {
            // Ohne Benachrichtigungsberechtigung silent aussteigen – die App
            // zeigt das Update spätestens beim nächsten Start als Dialog.
            return
        }
        ensureChannel(context)

        // Klick auf die Benachrichtigung öffnet die App und zeigt dort direkt
        // den Update-Dialog (Extra wird von MainActivity ausgewertet).
        val contentIntent = (
            context.packageManager.getLaunchIntentForPackage(context.packageName) ?: Intent()
            )
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            .putExtra(EXTRA_SHOW_UPDATE, true)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val installed = BuildConfig.VERSION_NAME
        val newVersion = release.tagName.removePrefix("v")
        val sizeText = release.assetSize
            ?.takeIf { it > 0 }
            ?.let { " · ${formatMegabytes(it)}" }
            .orEmpty()

        // Antipper direkt in der Benachrichtigung: startet den Download in der App.
        val installIntent = (
            context.packageManager.getLaunchIntentForPackage(context.packageName) ?: Intent()
            )
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            .putExtra(EXTRA_SHOW_UPDATE, true)
            .putExtra(EXTRA_AUTO_INSTALL, true)
        val installPending = PendingIntent.getActivity(
            context,
            1,
            installIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val bigText = buildString {
            append("Version $newVersion ist da")
            if (release.critical) append(" · Pflicht-Update")
            append(sizeText)
            append("\n")
            append(release.name?.takeIf { it.isNotBlank() } ?: "Neue Version der McAffen-App")
            val notes = release.notes
                ?.lineSequence()
                ?.filter { it.isNotBlank() && !it.startsWith("[kritisch]") }
                ?.take(3)
                ?.joinToString("\n") { "• " + it.trim().trimStart('-', '*', '#', ' ').take(120) }
            if (!notes.isNullOrBlank()) append("\n").append(notes)
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_update)
            .setColor(0xFF3F7F32.toInt())
            .setContentTitle(
                if (release.critical) {
                    "Pflicht-Update $newVersion"
                } else {
                    "Update verfügbar: $newVersion"
                },
            )
            .setContentText("Installiert: $installed · Neu: $newVersion")
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setPriority(if (release.critical) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_SYSTEM)
            .setAutoCancel(true)
            .setOnlyAlertOnce(false)
            .setProgress(0, 0, false)
            .setContentIntent(pendingIntent)
            .addAction(
                R.drawable.ic_stat_update,
                "Jetzt installieren",
                installPending,
            )
            .addAction(R.drawable.ic_stat_update, "Später", laterPending(context, release.tagName))
            .build()
        notifyIfAllowed(context, manager, NOTIFICATION_ID, notification)
    }

    /** Zeigt den Download-Fortschritt direkt in der Benachrichtigung. */
    fun updateProgress(context: Context, percent: Int) {
        val manager = NotificationManagerCompat.from(context)
        ensureChannel(context)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_update)
            .setColor(0xFF3F7F32.toInt())
            .setContentTitle("Update wird geladen")
            .setContentText("$percent % – bitte in der App bleiben")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setProgress(100, percent.coerceIn(0, 100), false)
            .setSilent(true)
            .build()
        notifyIfAllowed(context, manager, NOTIFICATION_ID, notification)
    }

    /** Fertig geladen – Hinweis, dass jetzt installiert wird. */
    fun showReady(context: Context) {
        val manager = NotificationManagerCompat.from(context)
        ensureChannel(context)
        val open = (
            context.packageManager.getLaunchIntentForPackage(context.packageName) ?: Intent()
            )
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_update)
            .setColor(0xFF3F7F32.toInt())
            .setContentTitle("Update bereit")
            .setContentText("Tippe hier, um die Installation abzuschließen.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(
                PendingIntent.getActivity(
                    context,
                    2,
                    open,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                ),
            )
            .build()
        notifyIfAllowed(context, manager, NOTIFICATION_ID, notification)
    }

    /** Entfernt die Update-Benachrichtigung (z. B. nach erfolgreicher Installation). */
    fun clear(context: Context) {
        runCatching { NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID) }
    }

    private fun laterPending(context: Context, tag: String): PendingIntent {
        val intent = Intent(context, UpdateDismissReceiver::class.java)
            .setAction(ACTION_LATER)
            .putExtra(EXTRA_TAG, tag)
        return PendingIntent.getBroadcast(
            context,
            3,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun formatMegabytes(bytes: Long): String {
        val mb = bytes / (1024.0 * 1024.0)
        return if (mb >= 10) "${mb.toInt()} MB" else String.format("%.1f MB", mb)
    }

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                "McAffen-Updates",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "Informiert über neue McAffen-App-Versionen"
                enableVibration(true)
            },
        )
    }

/**
 * Zeigt eine Benachrichtigung, wenn der Nutzer sie erlaubt hat.
 *
 * `notify()` wirft ohne POST_NOTIFICATIONS (Android 13+) eine SecurityException,
 * und Lint wertet den fehlenden Check als Fehler – deshalb hier zentral geprüft.
 */
private fun notifyIfAllowed(context: Context, manager: NotificationManagerCompat, id: Int, notification: Notification) {
    val granted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
        PackageManager.PERMISSION_GRANTED
    if (!granted) return
    try {
        manager.notify(id, notification)
    } catch (_: SecurityException) {
        // Berechtigung wurde zwischen Prüfung und Aufruf entzogen.
    }
}

}
