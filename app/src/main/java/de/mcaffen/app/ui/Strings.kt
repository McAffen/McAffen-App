package de.mcaffen.app.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import de.mcaffen.app.data.SettingsStore
import java.util.Locale

/**
 * Sprachen der App. Deutsch ist die Ausgangssprache, Englisch kommt dazu.
 *
 * Die Texte liegen bewusst im Kotlin-Code (kein `values-xx/strings.xml`):
 * So gelten dieselben Schlüssel für Android und später für die iOS-Oberfläche,
 * die den geteilten Kern mitbenutzt.
 */
enum class AppLanguage(val code: String, val label: String) {
    GERMAN("de", "Deutsch"),
    ENGLISH("en", "English"),
    ;

    companion object {
        const val SYSTEM = "SYSTEM"

        fun fromCode(code: String?): AppLanguage? =
            entries.firstOrNull { it.code.equals(code, ignoreCase = true) }

        /** Einstellung „SYSTEM“ auflösen: Deutsch, sofern das Gerät deutsch ist, sonst Englisch. */
        fun resolve(setting: String, device: Locale = Locale.getDefault()): AppLanguage =
            fromCode(setting) ?: if (device.language.equals("de", ignoreCase = true)) GERMAN else ENGLISH
    }
}

/** Schlüssel → Text je Sprache. Fehlt ein Eintrag im Englischen, wird der deutsche benutzt. */
private val translations: Map<String, Pair<String, String>> = buildMap {
    fun t(key: String, de: String, en: String) {
        put(key, de to en)
    }

    // --- Navigation / Titel -------------------------------------------------
    t("tab.leaderboard", "Rangliste", "Leaderboard")
    t("tab.news", "News", "News")
    t("tab.votes", "Voten", "Vote")
    t("tab.status", "Status", "Status")
    t("tab.settings", "Einstellungen", "Settings")
    t("menu.settings", "Einstellungen", "Settings")
    t("menu.about", "Über die App", "About the app")
    t("subtitle.loading", "wird geladen …", "loading …")
    t("subtitle.players", "%d Spieler", "%d players")
    t("subtitle.postsLoading", "Beiträge laden …", "loading posts …")
    t("subtitle.posts", "%d Beiträge", "%d posts")
    t("subtitle.sitesLoading", "Seiten laden …", "loading sites …")
    t("subtitle.sites", "%d Vote-Seiten", "%d vote sites")
    t("subtitle.online", "%d online", "%d online")
    t("subtitle.live", "live", "live")
    t("subtitle.version", "Version %s", "Version %s")
    t("cd.playerProfile", "Spielerprofil", "Player profile")
    t("cd.refresh", "Aktualisieren", "Refresh")
    t("cd.searchClear", "Suche leeren", "Clear search")

    // --- Allgemein ----------------------------------------------------------
    t("common.player", "Spieler", "Players")
    t("common.copy", " Kopieren", " Copy")
    t("common.copied", " Kopiert", " Copied")
    t("common.retry", "Erneut versuchen", "Try again")
    t("common.close", "Schließen", "Close")
    t("common.live", "LIVE", "LIVE")
    t("common.online", "online", "online")
    t("common.error", "Fehler: %s", "Error: %s")
    t("common.waves", "Wellen", "Waves")
    t("common.kills", "Kills", "Kills")
    t("common.games", "Spiele", "Games")
    t("common.coins", "Coins", "Coins")
    t("common.points", "Punkte", "points")

    // --- Rangliste ----------------------------------------------------------
    t("leaderboard.empty.title", "Rangliste ist leer", "Leaderboard is empty")
    t("leaderboard.empty.answered", "Antwort kam an (%d Einträge).", "Response arrived (%d entries).")
    t("leaderboard.empty.notAsked", "Noch keine Anfrage gelaufen – tippe auf „Jetzt testen“.", "No request yet – tap “Test now”.")
    t("leaderboard.test", "Jetzt testen", "Test now")
    t("leaderboard.lastCheck", "Letzte Prüfung: %s", "Last check: %s")
    t("leaderboard.search", "Spieler suchen", "Search players")
    t("leaderboard.notFound", "Niemanden gefunden", "Nobody found")
    t("leaderboard.notFound.text", "Kein Spieler passt zu „%s“. Prüfe die Schreibweise.", "No player matches “%s”. Check the spelling.")
    t("leaderboard.placements", "Platzierungen · Wellen (Nacht der Untoten)", "Placements · Waves (Nacht der Untoten)")
    t("leaderboard.place", "Platz %s wartet", "Place %s is waiting")
    t("leaderboard.leads", "%s führt mit %s Punkten – %s Spieler im Rennen.", "%s leads with %s points – %s players in the race.")
    t("leaderboard.noPlacements", "Noch keine Platzierungen verfügbar.", "No placements available yet.")
    t("leaderboard.youLead", "Du führst die Rangliste an 👑", "You lead the leaderboard 👑")
    t("leaderboard.pointsToFirst", "Noch %s Punkte bis Platz 1", "%s points to first place")
    t("leaderboard.tied", "Gleichauf mit der Spitze", "Tied with the top")
    t("leaderboard.bestRank", "Bester Rang", "Best rank")
    t("leaderboard.showInList", "In der Liste zeigen →", "Show in list →")
    t("leaderboard.notInList", "Noch nicht in der Rangliste – ein Vote pro Tag bringt dich rein.", "Not on the leaderboard yet – one vote a day gets you in.")
    t("leaderboard.you", "DU", "YOU")

    // --- Voten --------------------------------------------------------------
    t("vote.name", "Spielername", "Player name")
    t("vote.check", "Prüfen & freischalten", "Check & unlock")
    t("vote.ready", "%d / %d bereit", "%d / %d ready")
    t("vote.title", "Dein Minecraft-Name", "Your Minecraft name")
    t("vote.found", "Spieler gefunden", "Player found")
    t("vote.notFound", "Spieler nicht gefunden – prüfe die Schreibweise.", "Player not found – check the spelling.")
    t("vote.enterName", "Gib deinen Namen ein, um voten zu können. Er wird gespeichert.", "Enter your name to be able to vote. It is saved.")
    t("vote.sites", "Vote-Seiten", "Vote sites")
    t("vote.progress", "Vote-Fortschritt", "Vote progress")
    t("vote.progressCount", "%d von %d bereit", "%d of %d ready")
    t("vote.forPlayer", "Voten für %s – jede Seite bringt Punkte.", "Vote for %s – every site earns points.")
    t("vote.unlock", "Spieler prüfen, um alle Seiten freizuschalten.", "Check your player to unlock all sites.")
    t("vote.reward.default", "1 Vote-Schlüssel + 100 Coins", "1 vote key + 100 coins")
    t("vote.readyAgain", "Bereit – du kannst wieder voten", "Ready – you can vote again")
    t("vote.againIn", "Wieder möglich in %s", "Available again in %s")
    t("vote.action.vote", "Voten", "Vote")
    t("vote.action.again", "Nochmal", "Again")
    t("vote.lastVoted", "Zuletzt gevotet – jetzt wieder möglich", "Last voted – available again now")
    t("vote.needName", "Gib oben deinen Minecraft-Namen ein, dann öffnen sich die Vote-Seiten.", "Enter your Minecraft name above to open the vote sites.")
    t("vote.footer.rewards", "Für jede Abstimmung erhältst du Belohnungen wie Vote-Schlüssel und Coins. ", "Every vote gives you rewards such as vote keys and coins. ")
    t("vote.footer.browser", "Vote-Links öffnen sich im Browser. Umstellen unter Einstellungen → Vote-Links.", "Vote links open in the browser. Change this under Settings → Vote links.")
    t("vote.footer.inApp", "Vote-Links öffnen sich im eingebauten Browser – mit „Zurück“ bist du wieder hier. Umstellen unter Einstellungen → Vote-Links.", "Vote links open in the built-in browser – “Back” returns you here. Change this under Settings → Vote links.")
    t("vote.empty.title", "Keine Vote-Seiten", "No vote sites")
    t("vote.empty.text", "Ziehe zum Aktualisieren nach unten – die Seiten werden beim ersten Start geladen.", "Pull down to refresh – the sites load on first start.")

    // --- News ---------------------------------------------------------------
    t("news.empty", "Noch keine Beiträge.", "No posts yet.")
    t("news.reload", "Antippen zum Neu laden", "Tap to reload")
    t("news.loadError", "News konnten nicht geladen werden.", "News could not be loaded.")

    // --- Spielerprofil ------------------------------------------------------
    t("player.load", "Profil laden", "Load profile")
    t("player.none", "Kein Profil", "No profile")
    t("player.empty", "Kein Profil für „%s“ gefunden.", "No profile found for “%s”.")
    t("player.enterName", "Bitte einen Namen eingeben.", "Please enter a name.")
    t("player.hint", "Gib einen Namen ein – du bekommst Wellen, Kills, Ränge und Votes aus „Nacht der Untoten“.", "Enter a name – you get waves, kills, ranks and votes from “Nacht der Untoten”.")
    t("player.stat.waves", "Wellen", "Waves")
    t("player.stat.kills", "Kills", "Kills")
    t("player.stat.games", "Spiele", "Games")
    t("player.stat.coins", "Coins", "Coins")
    t("player.votes", "%d Votes · %d diesen Monat · Serie %d", "%d votes · %d this month · streak %d")
    t("player.lastPlayed", "Zuletzt gespielt: %s", "Last played: %s")

    // --- Status -------------------------------------------------------------
    t("status.empty.title", "Noch keine Statusdaten", "No status data yet")
    t("status.empty.text", "Ziehe nach unten, um den Live-Status von mcaffen.de zu laden.", "Pull down to load the live status of mcaffen.de.")
    t("status.network", "Netzwerk-Status", "Network status")
    t("status.playersOnline", "  Spieler online", "  players online")
    t("status.reachable", "%d von %d Servern erreichbar · Rekord heute: %s", "%d of %d servers reachable · peak today: %s")
    t("status.servers", "Server im Netzwerk", "Servers in the network")
    t("status.modes", "Spielmodi", "Game modes")
    t("status.messages", "Meldungen", "Messages")
    t("status.messages.empty.title", "Alles ruhig", "All quiet")
    t("status.messages.empty.text", "Aktuell gibt es keine Meldungen vom Netzwerk.", "There are no messages from the network right now.")
    t("status.javaIp", "Java-Edition · Server-IP", "Java Edition · Server IP")
    t("status.bedrockIp", "Bedrock-Edition · Handy, PC, Konsole", "Bedrock Edition · phone, PC, console")
    t("status.share", "  Server teilen", "  Share server")
    t("status.shareText", "Schau auf dem McAffen-Netzwerk vorbei! 🐵\nServer-IP: mcaffen.de", "Check out the McAffen network! 🐵\nServer IP: mcaffen.de")
    t("status.shareChooser", "Server teilen", "Share server")
    t("status.community", "Community", "Community")
    t("status.lan.title", "LAN-Welt für Konsolen", "LAN world for consoles")
    t("status.lan.onWifi", "Im WLAN", "On Wi-Fi")
    t("status.lan.noWifi", "Kein WLAN", "No Wi-Fi")
    t("status.lan.intro", "PS5, Switch und Xbox finden Welten nur im eigenen Netz. Mit einer Weiterleitung im WLAN erscheint bedrock.mcaffen.de dort als LAN-Welt.", "PS5, Switch and Xbox only find worlds on the local network. With a forward inside your Wi-Fi, bedrock.mcaffen.de shows up there as a LAN world.")
    t("status.lan.target", "Ziel", "Target")
    t("status.lan.thisDevice", "Dieses Gerät", "This device")
    t("status.lan.notInWifi", "nicht im WLAN", "not on Wi-Fi")
    t("status.lan.start", "  LAN-Welt starten", "  Start LAN world")
    t("status.lan.stop", "  LAN-Welt stoppen", "  Stop LAN world")
    t("status.lan.errorWifi", "LAN-Welt starten funktioniert nur mit einer aktiven WLAN-Verbindung.", "Starting the LAN world only works with an active Wi-Fi connection.")
    t("status.lan.errorStart", "LAN-Welt konnte nicht gestartet werden: %s", "The LAN world could not be started: %s")
    t("status.lan.portBusy", "Port %d ist belegt.", "Port %d is in use.")
    t("status.lan.footnote", "Die App bleibt geöffnet, solange die LAN-Welt aktiv ist. Handy und Konsole müssen im selben WLAN sein.", "The app stays open while the LAN world is active. Phone and console must be in the same Wi-Fi.")

    // --- Einstellungen ------------------------------------------------------
    t("settings.noName", "Noch kein Spielername", "No player name yet")
    t("settings.profile", "Profil", "Profile")
    t("settings.name", "Dein Minecraft-Name", "Your Minecraft name")
    t("settings.name.hint", "Wird beim Voten vorausgefüllt und hebt dich in der Rangliste hervor.", "Prefilled when voting and highlights you on the leaderboard.")
    t("settings.appearance", "Darstellung", "Appearance")
    t("settings.darkMode", "Dark-Mode", "Dark mode")
    t("settings.colorScheme", "Farbschema", "Color scheme")
    t("settings.effects", "Effekte & Komfort", "Effects & comfort")
    t("settings.animations", "Animationen", "Animations")
    t("settings.animations.hint", "Übergänge, Pulse und Effekte in der App.", "Transitions, pulses and effects in the app.")
    t("settings.haptics", "Haptisches Feedback", "Haptic feedback")
    t("settings.haptics.hint", "Sanftes Vibrieren bei Aktionen.", "Gentle vibration on actions.")
    t("settings.compact", "Kompakte Ansicht", "Compact view")
    t("settings.compact.hint", "Weniger Abstände und kleinere Karten.", "Less spacing and smaller cards.")
    t("settings.updates", "Updates", "Updates")
    t("settings.updates.auto", "Automatisch nach Updates suchen", "Check for updates automatically")
    t("settings.updates.auto.hint", "Beim Start, beim Zurückkehren und ca. alle 12 Stunden im Hintergrund.", "On start, on return and about every 12 hours in the background.")
    t("settings.updates.lastCheck", "Zuletzt geprüft: %s", "Last checked: %s")
    t("settings.updates.checkNow", "Jetzt prüfen", "Check now")
    t("settings.updates.searchInstall", "Nach Update suchen & installieren", "Find and install update")
    t("settings.voteBrowser", "Vote-Links öffnen", "Open vote links")
    t("settings.voteBrowser.hint", "Der eingebaute Browser bleibt in der App – mit „Zurück“ landest du wieder in der Vote-Liste. Der normale Browser öffnet einen eigenen Tab.", "The built-in browser stays in the app – “Back” returns you to the vote list. The normal browser opens its own tab.")
    t("settings.voteBrowser.inApp", "In der App", "In the app")
    t("settings.voteBrowser.external", "Normaler Browser", "Normal browser")
    t("settings.actions", "Aktionen", "Actions")
    t("settings.actions.refresh", "  Daten aktualisieren", "  Refresh data")
    t("settings.actions.replayIntro", "  Einführung noch einmal ansehen", "  Watch the intro again")
    t("settings.actions.about", "  Über die App", "  About the app")
    t("settings.actions.github", "  Auf GitHub öffnen", "  Open on GitHub")
    t("settings.legal", "Rechtliches", "Legal")
    t("settings.legal.hint", "Rechtstexte werden automatisch mit mcaffen.de synchronisiert.", "Legal texts are synced automatically with mcaffen.de.")
    t("settings.legal.lastSync", " Zuletzt: %s Uhr.", " Last sync: %s.")
    t("settings.legal.offline", " Aktuell werden die integrierten Texte angezeigt.", " The built-in texts are shown right now.")
    t("settings.legal.imprint", "Impressum", "Imprint")
    t("settings.legal.privacy", "Datenschutz", "Privacy")
    t("settings.legal.terms", "AGB", "Terms")
    t("settings.info", "App-Info", "App info")
    t("settings.info.version", "Version", "Version")
    t("settings.info.server", "Server", "Server")
    t("settings.info.disclaimer", "Inoffizielle App für den Minecraft-Server mcaffen.de. Nicht mit Mojang oder Microsoft verbunden.", "Unofficial app for the Minecraft server mcaffen.de. Not affiliated with Mojang or Microsoft.")
    t("settings.language", "Sprache", "Language")
    t("settings.language.hint", "Gilt für die ganze App. „System“ folgt der Gerätesprache.", "Applies to the whole app. “System” follows the device language.")
    t("settings.language.system", "System", "System")

    // --- „Über“-Dialog ------------------------------------------------------
    t("about.title", "Über McAffen-App", "About McAffen App")
    t("about.text", "Inoffizielle App für den Minecraft-Server mcaffen.de – Rangliste, Voten und Live-Status in einer Oberfläche.", "Unofficial app for the Minecraft server mcaffen.de – leaderboard, voting and live status in one interface.")
    t("about.badge", "Keine Zugangsdaten · kein Tracking", "No credentials · no tracking")
    t("about.legalFooter", "Vollständige und aktuelle Version: %s", "Full and current version: %s")
    t("about.openWeb", "Im Web öffnen", "Open on the web")

    // --- Willkommen ---------------------------------------------------------
    t("welcome.eyebrow", "Willkommen", "Welcome")
    t("welcome.title", "Willkommen in der McAffen-App", "Welcome to the McAffen app")
    t("welcome.text", "Rangliste, Voten und Live-Status des Minecraft-Netzwerks mcaffen.de – schnell, schön und ohne Umweg über den Browser.", "Leaderboard, voting and live status of the Minecraft network mcaffen.de – fast, beautiful and without a detour through the browser.")
    t("welcome.2.eyebrow", "Rangliste", "Leaderboard")
    t("welcome.2.title", "Sieh, wo du stehst", "See where you stand")
    t("welcome.2.text", "Podium, dein eigener Rang und der Abstand zu Platz 1 – live, mit Trend-Pfeilen für jeden Punkt, den du sammelst.", "Podium, your own rank and the gap to first place – live, with trend arrows for every point you collect.")
    t("welcome.3.eyebrow", "Voten", "Vote")
    t("welcome.3.title", "Ein Tipp, viele Belohnungen", "One tap, many rewards")
    t("welcome.3.text", "Alle Vote-Seiten an einem Ort, mit Countdown bis zum nächsten Vote und automatisch eingesetztem Spielernamen.", "All vote sites in one place, with a countdown to the next vote and your player name filled in automatically.")
    t("welcome.4.eyebrow", "Live-Status", "Live status")
    t("welcome.4.title", "Immer up to date", "Always up to date")
    t("welcome.4.text", "Spielerzahlen, Server-Übersicht und Meldungen – dazu Farbwelten, Dark-Mode und eigene Effekte.", "Player counts, server overview and messages – plus color themes, dark mode and your own effects.")
    t("welcome.nameTitle", "Wie heißt du in Minecraft?", "What is your Minecraft name?")
    t("welcome.nameHint", "Damit heben wir dich in der Rangliste hervor und füllen deinen Namen beim Voten automatisch ein.", "We use it to highlight you on the leaderboard and fill in your name when voting.")
    t("welcome.namePlaceholder", "z. B. AffenBoss", "e.g. AffenBoss")
    t("welcome.start", "Los geht's", "Let's go")
    t("welcome.next", "Weiter", "Next")
    t("welcome.later", "Später einrichten", "Set up later")
    t("welcome.skip", "Überspringen", "Skip")

    // --- Update-Dialog ------------------------------------------------------
    t("update.required", "Update erforderlich ⚠️", "Update required ⚠️")
    t("update.blocked", "Update blockiert", "Update blocked")
    t("update.failed", "Update fehlgeschlagen", "Update failed")
    t("update.available", "Update verfügbar 🚀", "Update available 🚀")
    t("update.current", "Aktuell", "Current")
    t("update.new", "Neu", "New")
    t("update.critical", "Dies ist ein kritisches Update und muss installiert werden.", "This is a critical update and must be installed.")
    t("update.explanation", "Das Update wird direkt in der App heruntergeladen und über der installierten Version installiert – deine Daten und Einstellungen bleiben erhalten.", "The update is downloaded inside the app and installed over the existing version – your data and settings are kept.")
    t("update.warning", "Hinweis: Android könnte beim Installieren warnen – das passiert bei Apps, die nicht aus dem Play Store kommen, häufig. Tippe bei der Warnung auf „Details“ und wähle „Trotzdem installieren“.", "Note: Android may warn during installation – this is common for apps not from the Play Store. Tap “Details” and choose “Install anyway”.")
    t("update.size", "Downloadgröße: %s", "Download size: %s")
    t("update.cellular", "Du nutzt gerade mobile Daten. Der Update-Download kann Datenvolumen verbrauchen.", "You are on mobile data. The update download may use your data plan.")
    t("update.downloading", "Lädt herunter …", "Downloading …")
    t("update.downloadingPercent", "Lädt herunter … %d%%", "Downloading … %d%%")
    t("update.error", "Fehler: %s\nDu kannst es erneut versuchen.", "Error: %s\nYou can try again.")
    t("update.corrupt", "Die heruntergeladene Datei ist unvollständig oder beschädigt. Bitte erneut herunterladen.", "The downloaded file is incomplete or damaged. Please download again.")
    t("update.incompatible", "Diese APK passt nicht als Update über die installierte Version (Paket-ID oder Version). Bitte erneut prüfen.", "This APK does not fit over the installed version (package ID or version). Please check again.")
    t("update.otherKey", "Die APK ist mit einem anderen Schlüssel signiert als die installierte App. Android erlaubt die Überinstallation dann nicht. Hier hilft nur eine Neuinstallation der App.", "The APK is signed with a different key than the installed app. Android will not install over it. Only a fresh install helps here.")
    t("update.allow", "Einmalig erforderlich: Erlaube der McAffen-App, „Apps aus unbekannten Quellen“ zu installieren. Danach aktualisiert sich die App direkt selbst.", "Required once: allow the McAffen app to install “apps from unknown sources”. After that the app updates itself.")
    t("update.loading", "Lädt …", "Loading …")
    t("update.retry", "Erneut versuchen", "Try again")
    t("update.download", "Jetzt herunterladen", "Download now")
    t("update.allowInstalls", "Installation erlauben", "Allow installation")
    t("update.confirmCellular", "Über mobile Daten bestätigen", "Confirm mobile data")
    t("update.installNow", "Jetzt aktualisieren", "Update now")
    t("update.github", "Notfall: Release auf GitHub öffnen", "Emergency: open release on GitHub")
    t("update.later", "Später", "Later")
    t("update.cd.updatedTo", "aktualisiert auf", "updated to")
    t("update.notice.searching", "Suche nach Updates …", "Looking for updates …")
    t("update.notice.failed", "Update-Prüfung fehlgeschlagen – bitte Internetverbindung prüfen.", "Update check failed – please check your connection.")
    t("update.notice.upToDate", "Du hast bereits die neueste Version (%s).", "You already have the latest version (%s).")
    t("update.notice.offline", "Keine Internetverbindung – Update-Prüfung nicht möglich.", "No internet connection – update check not possible.")
    t("update.notice.checkFailed", "Update-Prüfung fehlgeschlagen: %s", "Update check failed: %s")
    t("update.notice.downloadOffline", "Keine Internetverbindung – Download wurde abgebrochen.", "No internet connection – download cancelled.")
    t("update.notice.downloadFailed", "Download fehlgeschlagen", "Download failed")
    t("update.notice.boardOk", "Rangliste OK: %d Spieler, oben %s", "Leaderboard OK: %d players, top %s")
    t("update.notice.boardFailed", "Rangliste FEHLER: %s: %s", "Leaderboard ERROR: %s: %s")
    t("update.notice.boardLoadFailed", "Rangliste „%s“ konnte nicht geladen werden.", "Leaderboard “%s” could not be loaded.")
    t("update.notice.refreshFailed", "Daten konnten nicht geladen werden. Cache wird angezeigt.", "Data could not be loaded. Showing the cache.")

    // --- Zeitangaben --------------------------------------------------------
    t("time.never", "Noch nie", "Never")
    t("time.justNow", "gerade eben", "just now")
    t("time.minute", "vor 1 Minute", "1 minute ago")
    t("time.minutes", "vor %d Minuten", "%d minutes ago")
    t("time.hour", "vor 1 Stunde", "1 hour ago")
    t("time.hours", "vor %d Stunden", "%d hours ago")
    t("time.day", "vor 1 Tag", "1 day ago")
    t("time.days", "vor %d Tagen", "%d days ago")
    t("time.clock", "%s Uhr", "%s")

    // --- LAN-Schritte -------------------------------------------------------
    t("lan.step1.title", "Gerät im selben WLAN", "Device on the same Wi-Fi")
    t("lan.step1.detail", "Das Handy muss im selben WLAN wie die Konsole sein. Mobilfunk funktioniert nicht.", "The phone must be on the same Wi-Fi as the console. Mobile data does not work.")
    t("lan.step2.title", "LAN-Welt starten", "Start the LAN world")
    t("lan.step2.detail", "In der App auf „LAN-Welt starten“ drücken. Die App öffnet Port %d im lokalen WLAN und leitet Bedrock-Pakete automatisch an %s weiter.", "Tap “Start LAN world” in the app. The app opens port %d on the local network and forwards Bedrock packets to %s automatically.")
    t("lan.step3.title", "LAN-Ankündigung senden", "Send the LAN announcement")
    t("lan.step3.detail", "Die App sendet eine Bedrock-LAN-Ankündigung. Die Konsole muss im gleichen WLAN sein; Mobilfunk und Gast-WLAN mit Client-Isolation funktionieren nicht.", "The app sends a Bedrock LAN announcement. The console must be on the same Wi-Fi; mobile data and guest Wi-Fi with client isolation do not work.")
    t("lan.step4.title", "In der Konsole suchen", "Search on the console")
    t("lan.step4.detail", "Im Spiel unter „Freunde“ bzw. „LAN-Welten“ nach Welten suchen. Die Welt erscheint als LAN-Welt und verbindet sich über %s.", "In the game, search for worlds under “Friends” or “LAN worlds”. The world appears as a LAN world and connects via %s.")

    // --- Rangliste: zweites Spiel (Jump'n'Run) --------------------------------
    t("common.score", "Punkte", "Score")
    t("common.runs", "Runs", "Runs")
    t("common.blocks", "Blöcke", "Blocks")
    t("board.game.ndu", "Nacht der Untoten", "Night of the Dead")
    t("board.game.jump", "Jump'n'Run", "Jump'n'Run")

    // --- Sprachwahl im Willkommens-Flow ---------------------------------------
    t("welcome.lang.eyebrow", "Sprache", "Language")
    t("welcome.lang.title", "Wähle deine Sprache", "Choose your language")
    t("welcome.lang.text", "Die App spricht Deutsch und Englisch. Du kannst die Sprache jederzeit in den Einstellungen ändern.", "The app speaks German and English. You can change the language at any time in Settings.")

    // --- Weitere Texte, die vorher fest im Code standen -----------------------
    t("boot.loading", "McAffen wird geladen", "Loading McAffen")
    t("cd.logo", "McAffen-Logo", "McAffen logo")
    t("about.whatsNew", "Was ist neu?", "What's new?")
    t("about.openSource", "Open Source auf GitHub – Feedback und Pull Requests sind willkommen.", "Open source on GitHub – feedback and pull requests are welcome.")
    t("legal.translationNote", "Übersetzung zur besseren Verständlichkeit. Rechtlich maßgeblich ist die deutsche Fassung.", "Convenience translation. Only the German version is legally binding.")
    t("status.fallback.playersOnline", "%s: %d Spieler online", "%s: %d players online")
    t("lan.thisDeviceIp", "IP dieses Geräts", "this device's IP")

    // --- Update-Benachrichtigungen ---------------------------------------------
    t("notif.headline", "Version %s ist da", "Version %s is here")
    t("notif.criticalSuffix", " · Pflicht-Update", " · Required update")
    t("notif.defaultName", "Neue Version der McAffen-App", "New version of the McAffen app")
    t("notif.titleCritical", "Pflicht-Update %s", "Required update %s")
    t("notif.titleAvailable", "Update verfügbar: %s", "Update available: %s")
    t("notif.versions", "Installiert: %s · Neu: %s", "Installed: %s · New: %s")
    t("notif.install", "Jetzt installieren", "Install now")
    t("notif.later", "Später", "Later")
    t("notif.downloading", "Update wird geladen", "Downloading update")
    t("notif.progress", "%d % – bitte in der App bleiben", "%d % – please stay in the app")
    t("notif.ready", "Update bereit", "Update ready")
    t("notif.readyText", "Tippe hier, um die Installation abzuschließen.", "Tap here to finish the installation.")
    t("notif.channel", "McAffen-Updates", "McAffen updates")
    t("notif.channelDesc", "Informiert über neue McAffen-App-Versionen", "Notifies you about new McAffen app versions")
}

/** Aktive Sprache als CompositionLocal – jede Composable holt die Texte über [strings]. */
val LocalAppLanguage = staticCompositionLocalOf { AppLanguage.GERMAN }

/** Setzt die Texte für alle Kinder. */
val LocalStrings = compositionLocalOf<Strings> { Strings.of(AppLanguage.GERMAN) }

/**
 * Übersetzte Texte. `strings["tab.news"]` bzw. `strings("subtitle.players", 42)`
 * liefert den Text in der gerade gewählten Sprache.
 */
class Strings(val language: AppLanguage) {
    operator fun get(key: String): String {
        val entry = translations[key] ?: return key
        return if (language == AppLanguage.ENGLISH) entry.second else entry.first
    }

    /** Text mit Platzhaltern (`%s`, `%d`) in Reihenfolge der Argumente. */
    fun format(key: String, vararg args: Any?): String {
        var index = 0
        return get(key).replace(Regex("%[sd]")) { match ->
            val value = args.getOrNull(index++)
            if (match.value == "%d") (value as? Number)?.toInt()?.toString() ?: value.toString()
            else value.toString()
        }
    }

    companion object {
        private val cache = mutableMapOf<AppLanguage, Strings>()

        fun of(language: AppLanguage): Strings = cache.getOrPut(language) { Strings(language) }

        /**
         * Texte in der gespeicherten App-Sprache – für Stellen ohne Compose
         * (Benachrichtigungen, Hintergrund-Worker).
         */
        fun forContext(context: Context): Strings {
            val setting = context
                .getSharedPreferences(SettingsStore.PREFS_NAME, Context.MODE_PRIVATE)
                .getString(SettingsStore.KEY_LANGUAGE, null) ?: AppLanguage.SYSTEM
            return of(AppLanguage.resolve(setting))
        }
    }
}

/**
 * Kurzschreibweise in Composables:
 * `strings["tab.news"]` für einen Text, `strings.format("subtitle.players", 5)` mit Werten.
 */
@Composable
@ReadOnlyComposable
fun strings(): Strings = LocalStrings.current

/**
 * Übersetzter Text mit Werten – für Stellen, die kein [Strings]-Objekt haben
 * (z. B. private Composables). Nutzt die Sprache aus [LocalStrings].
 */
@Composable
@ReadOnlyComposable
fun formatString(key: String, vararg args: Any?): String =
    LocalStrings.current.format(key, *args)
