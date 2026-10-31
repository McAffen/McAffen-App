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

/** Absätze eines Rechtstextes. */
data class LegalContent(val intro: String?, val paragraphs: List<String>)

/** Ein juristisches Dokument (Impressum, Datenschutz, AGB). */
data class LegalDocument(
    val title: String,
    val sourceUrl: String,
    val content: LegalContent,
)

private fun text(vararg paragraphs: String) = paragraphs.toList()

val impressumDocument = LegalDocument(
    title = "Impressum",
    sourceUrl = "https://mcaffen.de/#impressum",
    content = LegalContent(
        intro = "Angaben gemäß § 5 DDG. Verantwortlich für die Website und die damit verbundenen Online-Angebote des Minecraft-Servers McAffen.de.",
        paragraphs = text(
            "Verantwortlich:\nMarcel Franz",
            "Anschrift:\nSophienstraße 16\n24768 Rendsburg\nDeutschland",
            "E-Mail:\nmarcelfranz662@gmail.com",
            "Telefon:\n0162/3905746",
            "Verantwortlich für den Inhalt:\nMarcel Franz",
        ),
    ),
)

val datenschutzDocument = LegalDocument(
    title = "Datenschutzerklärung",
    sourceUrl = "https://mcaffen.de/#datenschutz",
    content = LegalContent(
        intro = "Stand: Oktober 2026",
        paragraphs = text(
            "1. Verantwortlicher\nMarcel Franz\nSophienstraße 16\n24768 Rendsburg, Deutschland\nE-Mail: marcelfranz662@gmail.com",
            "2. Geltungsbereich\nDiese Datenschutzerklärung gilt für die Website mcaffen.de und die damit verbundenen Angebote des McAffen-Netzwerks.",
            "3. Hosting\nUnsere Website wird bei ProHosting24 gehostet. Dabei können technische Daten verarbeitet werden, die für den Betrieb, die Sicherheit und die Auslieferung der Website erforderlich sind.",
            "4. Cloudflare\nWir verwenden Cloudflare für DNS, Proxy-Dienste und technische Schutzfunktionen. Dabei können insbesondere IP-Adressen und Verbindungsdaten verarbeitet werden.",
            "5. Server-Logdaten\nBeim Aufruf der Website können unter anderem IP-Adresse, Zeitpunkt des Zugriffs, angeforderte Seite, Browserinformationen und Betriebssystem verarbeitet werden. Die Daten dienen dem technischen Betrieb und der Sicherheit.",
            "6. Cookies\nDie Website verwendet das Sitzungscookie PHPSESSID für Funktionen wie Login, Adminbereich und Tickets. Es dient der Zuordnung einer Sitzung.",
            "7. Lokaler Browserspeicher\nIm localStorage können Einstellungen wie Sprache, Effekte, der Minecraft-Name auf der Vote-Seite, die Ticketübersicht und die Cookie-Auswahl gespeichert werden.",
            "8. Cookie-Auswahl\nDer Cookie-Banner bietet die Optionen „Akzeptieren“ und „Nur notwendige“. Die Auswahl wird im Browser gespeichert. Technisch notwendige Funktionen können weiterhin verwendet werden. Die konkrete Wirkung der Auswahl hängt davon ab, welche Dienste tatsächlich geladen werden.",
            "9. Minecraft-Köpfe\nAuf bestimmten Seiten werden Minecraft-Köpfe von mc-heads.net geladen. Dabei kann deine IP-Adresse an diesen Drittanbieter übertragen werden. Ob diese Verbindung bei „Nur notwendige“ unterbunden wird, hängt von der technischen Umsetzung ab.",
            "10. Schriftarten und eigene Ressourcen\nSchriftarten sowie die übrigen lokal eingebundenen Ressourcen werden nach unserem derzeitigen Kenntnisstand auf unserem eigenen Server bereitgestellt.",
            "11. Externe Einbindungen\nEingebundene Inhalte von Drittanbietern, beispielsweise Discord oder YouTube, können beim Laden eine Verbindung zu deren Servern herstellen. Dabei können Daten wie die IP-Adresse übertragen werden. Für die weitere Verarbeitung gelten die Datenschutzbestimmungen des jeweiligen Anbieters.",
            "12. Externe Links\nUnsere Website enthält Links zu externen Angeboten. Beim Anklicken eines Links gelten die Datenschutzbestimmungen des jeweiligen Anbieters.",
            "13. Minecraft-Server\nBeim Spielen auf unseren Minecraft-Servern können Daten wie Spielername, UUID, IP-Adresse, Inventare, Spielstatistiken, Chatnachrichten, Befehle, Abstimmungen und wirtschaftliche Aktivitäten verarbeitet werden.",
            "14. Moderation und Sanktionen\nZur Durchsetzung unserer Serverregeln können wir Informationen über Verwarnungen, Mutes, Bans, Entbannungsanträge und gemeldete Verstöße verarbeiten.",
            "15. Öffentliche Spielerinformationen\nSpielernamen, Online-Status und bestimmte Spielstatistiken können öffentlich auf unserer Website oder innerhalb des Netzwerks angezeigt werden.",
            "16. Tickets und Entbannungsanträge\nBei Support-Tickets und Entbannungsanträgen verarbeiten wir die übermittelten Angaben sowie die für die Bearbeitung erforderlichen Informationen. Die Daten werden nach Prüfung des Falls manuell gelöscht, soweit keine weitere Aufbewahrung erforderlich ist.",
            "17. Discord und Verknüpfungen\nBei der Nutzung von Discord-Funktionen oder der Verknüpfung eines Discord-Kontos mit Minecraft können Discord-Kennungen und zugehörige Verknüpfungsdaten verarbeitet werden.",
            "18. Datenbanken\nFür den Betrieb unserer Angebote werden Datenbanken, darunter MongoDB und MySQL, eingesetzt. Darin können beispielsweise Spielerdaten, Einstellungen, Statistiken und Supportinformationen gespeichert werden.",
            "19. Speicherdauer\nDaten werden nur so lange gespeichert, wie sie für den jeweiligen Zweck benötigt werden oder gesetzliche beziehungsweise berechtigte Aufbewahrungsgründe bestehen. Die Löschung erfolgt je nach Datenart und System teilweise manuell.",
            "20. Keine Werbe- oder Analyse-Tracker\nNach unserem derzeitigen Kenntnisstand verwenden wir keine Werbe- oder Analyse-Tracker wie Google Analytics. Technisch notwendige Funktionen und eingebundene Drittanbieter sind davon zu unterscheiden.",
            "21. Deine Rechte\nDu hast im Rahmen der gesetzlichen Voraussetzungen das Recht auf Auskunft, Berichtigung, Löschung, Einschränkung der Verarbeitung, Datenübertragbarkeit und Widerspruch. Außerdem kannst du dich bei einer zuständigen Datenschutzaufsichtsbehörde beschweren.",
            "22. Kontakt und Änderungen\nBei Fragen zum Datenschutz erreichst du uns unter McAffe13@proton.me. Wir können diese Datenschutzerklärung anpassen, wenn sich unsere Website, die eingesetzten Dienste oder die rechtlichen Anforderungen ändern.",
        ),
    ),
)

val agbDocument = LegalDocument(
    title = "AGB",
    sourceUrl = "https://mcaffen.de/#agb",
    content = LegalContent(
        intro = "Stand: Oktober 2026",
        paragraphs = text(
            "§ 1 – Geltungsbereich\nDiese AGB gelten für die Website McAffen.de, das McAffen-Minecraft-Netzwerk und die zugehörigen Community-Angebote. Mit der Nutzung erklärst du dich mit diesen AGB und den geltenden Serverregeln einverstanden, soweit gesetzlich zulässig.",
            "§ 2 – Anbieter\nMarcel Franz\nSophienstraße 16, 24768 Rendsburg, Deutschland\nE-Mail: marcelfranz662@gmail.com",
            "§ 3 – Nutzung und Serverregeln\nAlle Nutzer müssen die Serverregeln einhalten und andere Spieler respektvoll behandeln. Insbesondere sind Cheats, unerlaubte Mods, Bugusing, Item-Duplizierung, Griefing, Echtgeldhandel mit Items, Coins oder Accounts, Spam, Beleidigungen, unerlaubte Werbung und die Umgehung von Sperren verboten.\n\nDie vollständigen Regeln und erlaubten beziehungsweise verbotenen Modifikationen findest du unter mcaffen.de/regeln.",
            "§ 4 – Maßnahmen bei Regelverstößen\nBei Verstößen kann das Team je nach Schwere und Häufigkeit Verwarnungen, Mutes, temporäre oder permanente Bans sowie weitere angemessene Maßnahmen verhängen.\n\nDer veröffentlichte Strafenkatalog dient als Richtwert. Bei besonders schweren Verstößen kann das Team davon abweichen. Eine Überprüfung kann über das Ticket-System oder einen Entbannungsantrag beantragt werden.",
            "§ 5 – Spielstände und virtuelle Werte\nSpielstände, Inventare, Statistiken und virtuelle Währungen sind Bestandteile des Spielangebots. Technische Fehler, Wartungsarbeiten oder andere Störungen können zu Datenverlusten oder Änderungen führen. Ein Anspruch auf dauerhafte Verfügbarkeit oder den unveränderten Erhalt besteht nicht, soweit gesetzlich zulässig.\n\nVirtuelle Währungen und Items können nicht in Echtgeld ausgezahlt werden. Der Handel mit Items, Coins oder Accounts gegen Echtgeld ist verboten.",
            "§ 6 – Verfügbarkeit und Änderungen\nMcAffen kann Wartungsarbeiten durchführen, Funktionen ändern und Spielmodi anpassen oder einstellen. Eine jederzeitige Verfügbarkeit wird nicht garantiert. Gesetzliche Ansprüche bleiben unberührt.",
            "§ 7 – Haftung\nMcAffen haftet nach den gesetzlichen Vorschriften, insbesondere bei Vorsatz, grober Fahrlässigkeit sowie Verletzungen von Leben, Körper oder Gesundheit. Bei leicht fahrlässigen Pflichtverletzungen gelten die gesetzlichen Haftungsbeschränkungen, soweit zulässig.",
            "§ 8 – Datenschutz\nInformationen zur Verarbeitung personenbezogener Daten findest du in unserer Datenschutzerklärung.",
            "§ 9 – Schlussbestimmungen\nEs gilt deutsches Recht, soweit zwingende gesetzliche Vorschriften, insbesondere Verbraucherschutzrechte, dem nicht entgegenstehen. Sollten einzelne Bestimmungen unwirksam sein, bleiben die übrigen Bestimmungen im gesetzlich zulässigen Umfang unberührt.\n\nKontakt: marcelfranz662@gmail.com",
        ),
    ),
)

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
                document.content.intro?.let { intro ->
                    Text(
                        intro,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                document.content.paragraphs.forEach { paragraph ->
                    Text(paragraph, style = MaterialTheme.typography.bodyMedium)
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
