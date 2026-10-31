package de.mcaffen.app.data

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
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

        // Klick auf die Benachrichtigung öffnet die App normal (kein Deep Link nötig).
        val contentIntent = (context.packageManager.getLaunchIntentForPackage(context.packageName) ?: Intent())
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.mcaffen_logo)
            .setContentTitle(if (release.critical) "Kritisches Update: ${release.tagName}" else "Update verfügbar: ${release.tagName}")
            .setContentText(release.name ?: "Eine neue Version der McAffen-App ist draußen.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        runCatching { manager.notify(NOTIFICATION_ID, notification) }
    }

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                "McAffen-Updates",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply { description = "Informiert über neue McAffen-App-Versionen" },
        )
    }
}
