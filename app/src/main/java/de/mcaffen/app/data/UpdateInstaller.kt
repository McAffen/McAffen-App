package de.mcaffen.app.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import java.io.File
import okhttp3.OkHttpClient
import okhttp3.Request

/**
 * Lädt die neueste APK herunter und startet den System-Install-Prompt,
 * damit die App direkt über die installierte Version aktualisiert wird
 * (die Daten bleiben erhalten; keine Deinstallation nötig, solange die
 * Releases mit demselben Keystore signiert sind).
 */
object UpdateInstaller {
    private const val MIME_APK = "application/vnd.android.package-archive"

    /** Ort, an dem die heruntergeladene Update-APK abgelegt wird. */
    fun targetFile(context: Context): File =
        File(File(context.cacheDir, "updates").apply { mkdirs() }, "mcaffen-update.apk")

    /** Kann die App direkt installieren? Ab Android 8 braucht sie "Unbekannte Apps". */
    fun canInstallDirectly(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.O ||
            context.packageManager.canRequestPackageInstalls()

    /** Öffnet die Systemeinstellungen, in denen die Installation erlaubt wird. */
    fun openUnknownSourcesSettings(context: Context) {
        val intent = Intent(
            Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
            Uri.parse("package:${context.packageName}"),
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(intent) }
    }

    /**
     * Lädt [url] herunter, schreibt in [targetFile] (erst temporär, dann atomar
     * umbenannt). [onProgress] meldet den Stand in Prozent; -1 = unbekannt.
     */
    suspend fun download(
        context: Context,
        client: OkHttpClient,
        url: String,
        onProgress: (Int) -> Unit,
    ): File {
        val target = targetFile(context)
        val temp = File(target.parentFile, target.name + ".part")
        val request = Request.Builder().url(url).build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IllegalStateException("Download fehlgeschlagen (HTTP ${response.code})")
            }
            val body = response.body ?: throw IllegalStateException("Leere Antwort beim Download")
            val total = body.contentLength()
            var copied = 0L
            body.byteStream().use { input ->
                temp.outputStream().use { output ->
                    val buffer = ByteArray(8 * 1024)
                    while (true) {
                        val read = input.read(buffer)
                        if (read == -1) break
                        output.write(buffer, 0, read)
                        copied += read
                        if (total > 0) {
                            onProgress(((copied * 100L) / total).toInt().coerceIn(0, 100))
                        }
                    }
                }
            }
        }
        temp.renameTo(target) || temp.copyTo(target, overwrite = true).let { temp.delete() }
        return target
    }

    /** Startet den System-Install-Prompt für die neue APK (Update über bestehender App). */
    fun launchInstall(context: Context, apk: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", apk)
        val intent = Intent(Intent.ACTION_VIEW)
            .setDataAndType(uri, MIME_APK)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(intent) }
    }
}
