package de.mcaffen.app.ui

import android.net.Uri
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import de.mcaffen.app.data.UpdateConfig

/** Punkte mit Tausendertrennzeichen der gewählten Sprache. */
internal fun formatScore(score: Int): String =
    NumberFormat.getIntegerInstance(Locale.GERMANY).format(score)

/** Punkte im Stil der gewählten App-Sprache: Deutsch „1.234“, Englisch „1,234“. */
internal fun formatScore(score: Int, language: AppLanguage): String =
    NumberFormat.getIntegerInstance(
        if (language == AppLanguage.ENGLISH) Locale.US else Locale.GERMANY,
    ).format(score)

/** Bytes leserlich formatieren (z. B. „8,4 MB“) für die Downloadgröße-Anzeige. */
internal fun formatBytes(bytes: Long?): String? {
    if (bytes == null || bytes <= 0) return null
    val megabytes = bytes / 1024.0 / 1024.0
    return when {
        megabytes >= 1.0 -> String.format(Locale.getDefault(), "%.1f MB", megabytes)
        else -> String.format(Locale.getDefault(), "%.0f KB", bytes / 1024.0)
    }
}

/** Freundliche Darstellung, wie lange die letzte Update-Prüfung her ist. */
internal fun lastCheckText(timestamp: Long, strings: Strings): String {
    if (timestamp <= 0L) return strings["time.never"]
    val minutes = (System.currentTimeMillis() - timestamp) / 60_000
    return when {
        minutes <= 0 -> strings["time.justNow"]
        minutes == 1L -> strings["time.minute"]
        minutes < 60 -> strings.format("time.minutes", minutes)
        minutes < 120 -> strings["time.hour"]
        minutes < 60 * 24 -> strings.format("time.hours", minutes / 60)
        minutes < 60 * 48 -> strings["time.day"]
        minutes < 60 * 24 * 30 -> strings.format("time.days", minutes / (60 * 24))
        else -> strings.format(
            "time.clock",
            SimpleDateFormat("dd.MM.yyyy, HH:mm", Locale.getDefault()).format(Date(timestamp)),
        )
    }
}

/** Release-Notes leserlich aufbereiten: Markdown-Bilder/Links/Hervorhebungen entfernen. */
internal fun updateNotesText(notes: String?): String? {
    if (notes.isNullOrBlank()) return null
    return notes
        .replace(Regex(Regex.escape(UpdateConfig.CRITICAL_MARKER), RegexOption.IGNORE_CASE), "")
        .replace(Regex("!\\[[^\\]]*\\]\\([^)]*\\)"), "") // Markdown-Bilder
        .replace(Regex("\\[([^\\]]+)\\]\\([^)]*\\)"), "$1") // [Link](url) → Text
        .replace("**", "")
        .replace("`", "") // Inline-Code-Ticks
        .split('\n')
        .joinToString("\n") { line -> line.trimStart('#').trim() }
        .replace(Regex("\\n{3,}"), "\n\n")
        .trim()
        .takeIf(String::isNotBlank)
}

/** Eine Zeile der Release-Notes, schon in ihre Rolle übersetzt. */
internal data class NoteLine(
    val text: String,
    val kind: Kind,
    val level: Int = 0,
) {
    enum class Kind { HEADING, BULLET, PARAGRAPH, DIVIDER }
}

/**
 * Zerlegt die Release-Notes in strukturierte Zeilen, damit der Dialog sie
 * gescheit setzen kann: Überschriften größer und fett, Aufzählungen mit
 * Punkt und Einzug, Absätze getrennt – statt eines einzigen Textblocks.
 */
internal fun updateNoteLines(notes: String?): List<NoteLine> {
    val cleaned = updateNotesText(notes) ?: return emptyList()
    val lines = mutableListOf<NoteLine>()
    cleaned.split('\n').forEach { raw ->
        val line = raw.trimEnd()
        val trimmed = line.trim()
        when {
            trimmed.isEmpty() -> Unit // Leerzeilen trennen nur die Absätze
            trimmed.startsWith("---") ->
                lines += NoteLine("", NoteLine.Kind.DIVIDER)
            trimmed.startsWith("•") || trimmed.startsWith("- ") || trimmed.startsWith("* ") -> {
                val indent = line.length - line.trimStart().length
                lines += NoteLine(
                    trimmed.removePrefix("•").removePrefix("-").removePrefix("*").trim(),
                    NoteLine.Kind.BULLET,
                    level = if (indent > 1) 1 else 0,
                )
            }
            // Kurze Zeile ohne Punkt am Ende = Überschrift
            trimmed.length <= 60 && !trimmed.endsWith(".") && !trimmed.contains(": ") ->
                lines += NoteLine(trimmed, NoteLine.Kind.HEADING)
            else -> lines += NoteLine(trimmed, NoteLine.Kind.PARAGRAPH)
        }
    }
    return lines
}

/** Ersetzt {user}-Platzhalter in Vote-URLs durch den Spielernamen und prüft auf HTTPS. */
internal fun String.toVoteUri(playerName: String): Uri? {
    val placeholderRegex = Regex("\\{user(name)?\\}", RegexOption.IGNORE_CASE)
    val encoded = Uri.encode(playerName)
    val replaced = replace(placeholderRegex, encoded)
    val hasPlaceholder = placeholderRegex.containsMatchIn(this)
    var uri = runCatching { Uri.parse(replaced.trim()) }.getOrNull() ?: return null
    if (uri.scheme == "http") {
        uri = uri.buildUpon().scheme("https").build()
    }
    val valid = uri.scheme.equals("https", ignoreCase = true) && !uri.host.isNullOrBlank()
    if (!valid) return null
    // Fallback für Seiten ohne Platzhalter: Namen als Query-Parameter anhängen.
    if (!hasPlaceholder && playerName.isNotBlank()) {
        uri = uri.buildUpon().appendQueryParameter("username", encoded).build()
    }
    return uri
}

/** Vote-Seiten erlauben pro Spieler/Tag meist einen Vote. */
internal const val VOTE_COOLDOWN_MS = 24L * 60 * 60 * 1000

/** Übrige Zeit bis zum nächsten Vote, als HH:MM:SS. */
internal fun formatRemaining(remainingMs: Long): String {
    val totalSeconds = (remainingMs / 1000).coerceAtLeast(0)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
}

/** Kompakte Restzeit für Chips (z. B. „3 Std 12 Min“ statt 03:12:44). */
internal fun formatRemainingShort(remainingMs: Long, language: AppLanguage = AppLanguage.GERMAN): String {
    val totalMinutes = (remainingMs / 60_000).coerceAtLeast(0)
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    val english = language == AppLanguage.ENGLISH
    return when {
        hours > 0 -> if (english) "$hours h $minutes min" else "$hours Std $minutes Min"
        minutes > 0 -> if (english) "$minutes min" else "$minutes Min"
        else -> if (english) "under 1 min" else "unter 1 Min"
    }
}

/** Entfernt Minecraft-Farbcodes aus Versionsstrings (z. B. §4 vor „Wartung“). */
internal fun cleanColorCodes(value: String): String =
    value.replace(Regex("§."), "").trim()
