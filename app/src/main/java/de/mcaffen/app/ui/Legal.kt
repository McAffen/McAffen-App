package de.mcaffen.app.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** Ein Textabschnitt mit optionaler Überschrift. */
data class LegalSection(val title: String?, val paragraphs: List<String>)

/** Ein juristisches Dokument (Impressum, Datenschutz, AGB). */
data class LegalDocument(
    val title: String,
    val sourceUrl: String,
    val sections: List<LegalSection>,
)

private fun text(vararg paragraphs: String) = paragraphs.toList()

val impressumDocument = LegalDocument(
    title = "Impressum",
    sourceUrl = "https://mcaffen.de/#impressum",
    sections = listOf(
        LegalSection(
            null,
            text(
                "Angaben gemäß § 5 DDG. Verantwortlich für die Website und die damit verbundenen Online-Angebote des Minecraft-Servers McAffen.de.",
                "Verantwortlich:\nMarcel Franz",
                "Anschrift:\nSophienstraße 16\n24768 Rendsburg\nDeutschland",
                "E-Mail:\nmarcelfranz662@gmail.com",
                "Telefon:\n0162/3905746",
                "Verantwortlich für den Inhalt:\nMarcel Franz",
            ),
        ),
    ),
)

val datenschutzDocument = LegalDocument(
    title = "Datenschutzerklärung",
    sourceUrl = "https://mcaffen.de/#datenschutz",
    sections = listOf(
        LegalSection(null, text("Stand: Oktober 2026")),
        LegalSection("1. Verantwortlicher", text("Marcel Franz\nSophienstraße 16\n24768 Rendsburg, Deutschland\nE-Mail: marcelfranz662@gmail.com")),
        LegalSection("2. Geltungsbereich", text("Diese Datenschutzerklärung gilt für die Website mcaffen.de und die damit verbundenen Angebote des McAffen-Netzwerks.")),
        LegalSection("3. Hosting", text("Unsere Website wird bei ProHosting24 gehostet. Dabei können technische Daten verarbeitet werden, die für den Betrieb, die Sicherheit und die Auslieferung der Website erforderlich sind.")),
        LegalSection("4. Cloudflare", text("Wir verwenden Cloudflare für DNS, Proxy-Dienste und technische Schutzfunktionen. Dabei können insbesondere IP-Adressen und Verbindungsdaten verarbeitet werden.")),
        LegalSection("5. Server-Logdaten", text("Beim Aufruf der Website können unter anderem IP-Adresse, Zeitpunkt des Zugriffs, angeforderte Seite, Browserinformationen und Betriebssystem verarbeitet werden. Die Daten dienen dem technischen Betrieb und der Sicherheit.")),
        LegalSection("6. Cookies", text("Die Website verwendet das Sitzungscookie PHPSESSID für Funktionen wie Login, Adminbereich und Tickets. Es dient der Zuordnung einer Sitzung.")),
        LegalSection("7. Lokaler Browserspeicher", text("Im localStorage können Einstellungen wie Sprache, Effekte, der Minecraft-Name auf der Vote-Seite, die Ticketübersicht und die Cookie-Auswahl gespeichert werden.")),
        LegalSection("8. Cookie-Auswahl", text("Der Cookie-Banner bietet die Optionen „Akzeptieren“ und „Nur notwendige“. Die Auswahl wird im Browser gespeichert. Technisch notwendige Funktionen können weiterhin verwendet werden. Die konkrete Wirkung der Auswahl hängt davon ab, welche Dienste tatsächlich geladen werden.")),
        LegalSection("9. Minecraft-Köpfe", text("Auf bestimmten Seiten werden Minecraft-Köpfe von mc-heads.net geladen. Dabei kann deine IP-Adresse an diesen Drittanbieter übertragen werden. Ob diese Verbindung bei „Nur notwendige“ unterbunden wird, hängt von der technischen Umsetzung ab.")),
        LegalSection("10. Schriftarten und eigene Ressourcen", text("Schriftarten sowie die übrigen lokal eingebundenen Ressourcen werden nach unserem derzeitigen Kenntnisstand auf unserem eigenen Server bereitgestellt.")),
        LegalSection("11. Externe Einbindungen", text("Eingebundene Inhalte von Drittanbietern, beispielsweise Discord oder YouTube, können beim Laden eine Verbindung zu deren Servern herstellen. Dabei können Daten wie die IP-Adresse übertragen werden. Für die weitere Verarbeitung gelten die Datenschutzbestimmungen des jeweiligen Anbieters.")),
        LegalSection("12. Externe Links", text("Unsere Website enthält Links zu externen Angeboten. Beim Anklicken eines Links gelten die Datenschutzbestimmungen des jeweiligen Anbieters.")),
        LegalSection("13. Minecraft-Server", text("Beim Spielen auf unseren Minecraft-Servern können Daten wie Spielername, UUID, IP-Adresse, Inventare, Spielstatistiken, Chatnachrichten, Befehle, Abstimmungen und wirtschaftliche Aktivitäten verarbeitet werden.")),
        LegalSection("14. Moderation und Sanktionen", text("Zur Durchsetzung unserer Serverregeln können wir Informationen über Verwarnungen, Mutes, Bans, Entbannungsanträge und gemeldete Verstöße verarbeiten.")),
        LegalSection("15. Öffentliche Spielerinformationen", text("Spielernamen, Online-Status und bestimmte Spielstatistiken können öffentlich auf unserer Website oder innerhalb des Netzwerks angezeigt werden.")),
        LegalSection("16. Tickets und Entbannungsanträge", text("Bei Support-Tickets und Entbannungsanträgen verarbeiten wir die übermittelten Angaben sowie die für die Bearbeitung erforderlichen Informationen. Die Daten werden nach Prüfung des Falls manuell gelöscht, soweit keine weitere Aufbewahrung erforderlich ist.")),
        LegalSection("17. Discord und Verknüpfungen", text("Bei der Nutzung von Discord-Funktionen oder der Verknüpfung eines Discord-Kontos mit Minecraft können Discord-Kennungen und zugehörige Verknüpfungsdaten verarbeitet werden.")),
        LegalSection("18. Datenbanken", text("Für den Betrieb unserer Angebote werden Datenbanken, darunter MongoDB und MySQL, eingesetzt. Darin können beispielsweise Spielerdaten, Einstellungen, Statistiken und Supportinformationen gespeichert werden.")),
        LegalSection("19. Speicherdauer", text("Daten werden nur so lange gespeichert, wie sie für den jeweiligen Zweck benötigt werden oder gesetzliche beziehungsweise berechtigte Aufbewahrungsgründe bestehen. Die Löschung erfolgt je nach Datenart und System teilweise manuell.")),
        LegalSection("20. Keine Werbe- oder Analyse-Tracker", text("Nach unserem derzeitigen Kenntnisstand verwenden wir keine Werbe- oder Analyse-Tracker wie Google Analytics. Technisch notwendige Funktionen und eingebundene Drittanbieter sind davon zu unterscheiden.")),
        LegalSection("21. Deine Rechte", text("Du hast im Rahmen der gesetzlichen Voraussetzungen das Recht auf Auskunft, Berichtigung, Löschung, Einschränkung der Verarbeitung, Datenübertragbarkeit und Widerspruch. Außerdem kannst du dich bei einer zuständigen Datenschutzaufsichtsbehörde beschweren.")),
        LegalSection("22. Kontakt und Änderungen", text("Bei Fragen zum Datenschutz erreichst du uns unter McAffe13@proton.me. Wir können diese Datenschutzerklärung anpassen, wenn sich unsere Website, die eingesetzten Dienste oder die rechtlichen Anforderungen ändern.")),
    ),
)

val agbDocument = LegalDocument(
    title = "AGB",
    sourceUrl = "https://mcaffen.de/#agb",
    sections = listOf(
        LegalSection(null, text("Stand: Oktober 2026")),
        LegalSection("§ 1 – Geltungsbereich", text("Diese AGB gelten für die Website McAffen.de, das McAffen-Minecraft-Netzwerk und die zugehörigen Community-Angebote. Mit der Nutzung erklärst du dich mit diesen AGB und den geltenden Serverregeln einverstanden, soweit gesetzlich zulässig.")),
        LegalSection("§ 2 – Anbieter", text("Marcel Franz\nSophienstraße 16, 24768 Rendsburg, Deutschland\nE-Mail: marcelfranz662@gmail.com")),
        LegalSection("§ 3 – Nutzung und Serverregeln", text("Alle Nutzer müssen die Serverregeln einhalten und andere Spieler respektvoll behandeln. Insbesondere sind Cheats, unerlaubte Mods, Bugusing, Item-Duplizierung, Griefing, Echtgeldhandel mit Items, Coins oder Accounts, Spam, Beleidigungen, unerlaubte Werbung und die Umgehung von Sperren verboten.\n\nDie vollständigen Regeln und erlaubten beziehungsweise verbotenen Modifikationen findest du unter mcaffen.de/regeln.")),
        LegalSection("§ 4 – Maßnahmen bei Regelverstößen", text("Bei Verstößen kann das Team je nach Schwere und Häufigkeit Verwarnungen, Mutes, temporäre oder permanente Bans sowie weitere angemessene Maßnahmen verhängen.\n\nDer veröffentlichte Strafenkatalog dient als Richtwert. Bei besonders schweren Verstößen kann das Team davon abweichen. Eine Überprüfung kann über das Ticket-System oder einen Entbannungsantrag beantragt werden.")),
        LegalSection("§ 5 – Spielstände und virtuelle Werte", text("Spielstände, Inventare, Statistiken und virtuelle Währungen sind Bestandteile des Spielangebots. Technische Fehler, Wartungsarbeiten oder andere Störungen können zu Datenverlusten oder Änderungen führen. Ein Anspruch auf dauerhafte Verfügbarkeit oder den unveränderten Erhalt besteht nicht, soweit gesetzlich zulässig.\n\nVirtuelle Währungen und Items können nicht in Echtgeld ausgezahlt werden. Der Handel mit Items, Coins oder Accounts gegen Echtgeld ist verboten.")),
        LegalSection("§ 6 – Verfügbarkeit und Änderungen", text("McAffen kann Wartungsarbeiten durchführen, Funktionen ändern und Spielmodi anpassen oder einstellen. Eine jederzeitige Verfügbarkeit wird nicht garantiert. Gesetzliche Ansprüche bleiben unberührt.")),
        LegalSection("§ 7 – Haftung", text("McAffen haftet nach den gesetzlichen Vorschriften, insbesondere bei Vorsatz, grober Fahrlässigkeit sowie Verletzungen von Leben, Körper oder Gesundheit. Bei leicht fahrlässigen Pflichtverletzungen gelten die gesetzlichen Haftungsbeschränkungen, soweit zulässig.")),
        LegalSection("§ 8 – Datenschutz", text("Informationen zur Verarbeitung personenbezogener Daten findest du in unserer Datenschutzerklärung.")),
        LegalSection("§ 9 – Schlussbestimmungen", text("Es gilt deutsches Recht, soweit zwingende gesetzliche Vorschriften, insbesondere Verbraucherschutzrechte, dem nicht entgegenstehen. Sollten einzelne Bestimmungen unwirksam sein, bleiben die übrigen Bestimmungen im gesetzlich zulässigen Umfang unberührt.\n\nKontakt: marcelfranz662@gmail.com")),
    ),
)

private val offlineDocuments = mapOf(
    "impressum" to impressumDocument,
    "datenschutz" to datenschutzDocument,
    "agb" to agbDocument,
)

val LEGAL_IDS = offlineDocuments.keys.toList()

/**
 * Erzeugt das Dokument zur Anzeige: Die mit mcaffen.de synchronisierte Version
 * (falls vorhanden) oder der integrierte Offline-Text als Fallback.
 */
fun legalDocument(id: String, cachedTitle: String?, cachedHtml: String?): LegalDocument {
    val offline = offlineDocuments[id] ?: impressumDocument
    if (cachedHtml.isNullOrBlank()) return offline
    val sections = htmlToSections(cachedHtml) ?: return offline
    return offline.copy(
        title = cachedTitle?.takeIf(String::isNotBlank) ?: offline.title,
        sections = sections,
    )
}

/** Wandelt den HTML-Inhalt eines Rechtstextes in Abschnitte mit Absätzen um. */
fun htmlToSections(html: String): List<LegalSection>? {
    val headings = headingRegex.findAll(html).toList()
    if (headings.isEmpty()) {
        val paragraphs = paragraphsOf(html)
        return paragraphs.takeIf { it.isNotEmpty() }?.let { listOf(LegalSection(null, it)) }
    }
    val sections = mutableListOf<LegalSection>()
    paragraphsOf(html.substring(0, headings.first().range.first)).takeIf { it.isNotEmpty() }
        ?.let { sections.add(LegalSection(null, it)) }
    headings.forEachIndexed { index, heading ->
        val chunkEnd = if (index < headings.lastIndex) headings[index + 1].range.first else html.length
        val body = html.substring(heading.range.last + 1, chunkEnd)
        sections.add(LegalSection(stripHtml(heading.groupValues[1]).ifBlank { null }, paragraphsOf(body)))
    }
    return sections.ifEmpty { null }
}

private val headingRegex = Regex("<h[123][^>]*>([\\s\\S]*?)</h[123]>")
private val paragraphRegex = Regex("<p[^>]*>([\\s\\S]*?)</p>")

private fun paragraphsOf(chunk: String): List<String> =
    paragraphRegex.findAll(chunk)
        .mapNotNull { stripHtml(it.groupValues[1]).takeIf(String::isNotBlank) }
        .toList()

private fun stripHtml(value: String): String =
    value.replace(Regex("<br\\s*/?>"), "\n")
        .replace(Regex("<[^>]*>"), "")
        .replace("&nbsp;", " ")
        .replace("&amp;", "&")
        .replace("&quot;", "\"")
        .replace(Regex("&#39;|&apos;"), "'")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace(Regex("\\n{3,}"), "\n\n")
        .trim()

@Composable
fun LegalDialog(document: LegalDocument, onDismiss: () -> Unit) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                document.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(
                Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                document.sections.forEach { section ->
                    section.title?.let { title ->
                        Text(
                            title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    section.paragraphs.forEach { paragraph ->
                        Text(paragraph, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Text(
                    "Vollständige und aktuelle Version: ${document.sourceUrl}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(document.sourceUrl)))
                onDismiss()
            }) { Text("Im Web öffnen") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Schließen") }
        },
    )
}
