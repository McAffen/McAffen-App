package de.mcaffen.app.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import java.io.File
import java.security.MessageDigest
import okhttp3.OkHttpClient
import okhttp3.Request

/**
 * Ergebnis der APK-Prüfung, bevor die Installation angetippt wird.
 * Vermeidet Fehlversuche: Android würde das Update sonst erst beim
 * Installieren ablehnen (z. B. wegen geändertem Signaturschlüssel).
 */
sealed interface ApkVerification {
    /** APK vollständig, korrekte Paket-ID, höherer versionCode, gleiche Signatur. */
    data object Ok : ApkVerification

    /** Datei unvollständig, beschädigt oder kein gültiges APK – erneut versuchen. */
    data object Corrupt : ApkVerification

    /** Falsche Paket-ID oder zu niedriger versionCode → kein Update über diese Installation. */
    data object Incompatible : ApkVerification

    /** APK mit anderem Signaturschlüssel → Android lehnt die Überinstallation ab. */
    data object SignatureChanged : ApkVerification
}

/**
 * Lädt die neueste APK herunter, überprüft sie und startet den System-Install-Prompt,
 * damit die App direkt über die installierte Version aktualisiert wird
 * (die Daten bleiben erhalten; keine Deinstallation nötig, solange die
 * Releases mit demselben Keystore signiert sind).
 */
object UpdateInstaller {
    private const val MIME_APK = "application/vnd.android.package-archive"
    private const val MIN_APK_BYTES = 100_000L

    /** Ort, an dem die heruntergeladene Update-APK abgelegt wird (App-Cache, privat). */
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
     * umbenannt). [onProgress] meldet den Stand in Prozent; bei unbekannter
     * Gesamtgröße wird keine Meldung gesendet. Stale-Dateien alter Versuche
     * werden vorher entfernt, damit nie gemischte Bytes angetroffen werden.
     */
    suspend fun download(
        context: Context,
        client: OkHttpClient,
        url: String,
        onProgress: (Int) -> Unit,
    ): File {
        val target = targetFile(context)
        val temp = File(target.parentFile, target.name + ".part")
        temp.delete()
        target.delete()
        val request = Request.Builder().url(url).build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IllegalStateException("Download fehlgeschlagen (HTTP ${response.code})")
            }
            val body = response.body ?: throw IllegalStateException("Leere Antwort beim Download")
            val total = body.contentLength() // -1 = unbekannt
            var copied = 0L
            body.byteStream().use { input ->
                temp.outputStream().use { output ->
                    val buffer = ByteArray(64 * 1024)
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
            // Integrität: übertragene Bytes müssen der erwarteten Länge entsprechen.
            if (total > 0 && copied != total) {
                throw IllegalStateException("Download unvollständig ($copied/$total Bytes)")
            }
        }
        if (!temp.renameTo(target)) {
            temp.copyTo(target, overwrite = true)
            temp.delete()
        }
        return target
    }

    /**
     * Prüft die APK, bevor die Installation gestartet wird: Dateilänge, ZIP-Kennung,
     * Paket-ID, versionCode und – besonders wichtig – ob die APK mit demselben
     * Schlüssel signiert ist wie die installierte App (SHA-256-Vergleich der
     * Zertifikate). Erst wenn das passt, übernimmt Android das Update ohne Neuinstallation.
     */
    fun verifyApk(context: Context, apk: File): ApkVerification {
        if (!apk.isFile || apk.length() < MIN_APK_BYTES) return ApkVerification.Corrupt
        val signatureOk = runCatching {
            apk.inputStream().use { stream ->
                val header = ByteArray(4)
                stream.read(header) >= 2 && header[0] == 0x50.toByte() && header[1] == 0x4B.toByte()
            }
        }.getOrDefault(false)
        if (!signatureOk) return ApkVerification.Corrupt

        val packageManager = context.packageManager
        val installed = runCatching {
            packageManager.getPackageInfo(
                context.packageName,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    PackageManager.GET_SIGNING_CERTIFICATES
                } else {
                    legacySignatureFlags()
                },
            )
        }.getOrNull() ?: return ApkVerification.Ok
        val archive = runCatching {
            packageManager.getPackageArchiveInfo(
                apk.absolutePath,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    PackageManager.GET_SIGNING_CERTIFICATES
                } else {
                    legacySignatureFlags()
                },
            )
        }.getOrNull() ?: return ApkVerification.Corrupt

        // Falsche Paket-ID → sicherheitshalber gar nicht erst installieren lassen.
        if (archive.packageName != context.packageName) return ApkVerification.Incompatible

        // Android akzeptiert Updates nur mit strikt höherem versionCode.
        @Suppress("DEPRECATION")
        val archiveCode =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) archive.longVersionCode
            else archive.versionCode.toLong()
        @Suppress("DEPRECATION")
        val installedCode =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) installed.longVersionCode
            else installed.versionCode.toLong()
        if (archiveCode <= installedCode) return ApkVerification.Incompatible

        // Signaturvergleich: mindestens ein Zertifikat muss identisch sein.
        val archiveDigests = signatureDigests(archive)
        val installedDigests = signatureDigests(installed)
        if (archiveDigests.isEmpty() || installedDigests.isEmpty()) return ApkVerification.Ok
        return if (archiveDigests.any { it in installedDigests }) {
            ApkVerification.Ok
        } else {
            ApkVerification.SignatureChanged
        }
    }

    /** Startet den System-Install-Prompt für die neue APK. Wird er bestätigt,
     *  aktualisiert Android die App an Ort und Stelle – alle Daten bleiben erhalten. */
    fun launchInstall(context: Context, apk: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", apk)
        val intent = Intent(Intent.ACTION_VIEW)
            .setDataAndType(uri, MIME_APK)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(intent) }
    }

    @Suppress("DEPRECATION")
    private fun legacySignatureFlags(): Int = PackageManager.GET_SIGNATURES

    /** SHA-256-Digests aller Signatur-Zertifikate der APK/einer installierten App. */
    private fun signatureDigests(info: PackageInfo): Set<String> {
        val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            info.signingInfo?.apkContentsSigners
        } else {
            @Suppress("DEPRECATION")
            info.signatures
        } ?: return emptySet()
        return signatures.mapNotNull { signature ->
            runCatching {
                MessageDigest.getInstance("SHA-256")
                    .digest(signature.toByteArray())
                    .joinToString("") { byte -> "%02x".format(byte) }
            }.getOrNull()
        }.toSet()
    }
}
