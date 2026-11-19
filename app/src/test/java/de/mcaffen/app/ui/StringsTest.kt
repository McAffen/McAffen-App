package de.mcaffen.app.ui

import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Prüft die Übersetzungstabelle: Jeder Schlüssel muss in beiden Sprachen
 * vorhanden sein, Platzhalter müssen übereinstimmen und die Platzhalter-Ersetzung
 * muss die Werte in der richtigen Reihenfolge einsetzen.
 */
class StringsTest {

    private val german = Strings.of(AppLanguage.GERMAN)
    private val english = Strings.of(AppLanguage.ENGLISH)

    /** Alle Schlüssel des deutschen Wörterbuchs, über einen bekannten Eintrag erreichbar. */
    private fun keys(): List<String> {
        // Die Tabelle ist privat; für den Test reicht es, jeden benutzten Schlüssel
        // aus einer Liste zu prüfen, die aus dem Quelltext gepflegt wird.
        return TRANSLATION_KEYS
    }

    @Test
    fun `jeder Schluessel existiert in beiden Sprachen`() {
        keys().forEach { key ->
            val de = german[key]
            val en = english[key]
            assertTrue(de != key, "Deutscher Text fehlt für '$key'")
            assertTrue(en != key, "Englischer Text fehlt für '$key'")
            assertTrue(de.isNotBlank(), "Deutscher Text ist leer für '$key'")
            assertTrue(en.isNotBlank(), "Englischer Text ist leer für '$key'")
        }
    }

    @Test
    fun `Platzhalter stimmen zwischen den Sprachen ueberein`() {
        keys().forEach { key ->
            assertEquals(
                placeholders(german[key]),
                placeholders(english[key]),
                "Platzhalter weichen ab für '$key'",
            )
        }
    }

    @Test
    fun `format setzt Werte in der Reihenfolge der Argumente ein`() {
        assertEquals(
            "42 Spieler",
            german.format("subtitle.players", 42),
        )
        assertEquals(
            "42 players",
            english.format("subtitle.players", 42),
        )
        assertEquals(
            "12 / 30 bereit",
            german.format("vote.ready", 12, 30),
        )
        assertEquals(
            "12 / 30 ready",
            english.format("vote.ready", 12, 30),
        )
    }

    @Test
    fun `unbekannter Schluessel liefert den Schluessel zurueck`() {
        assertEquals("gibt.es.nicht", german["gibt.es.nicht"])
    }

    @Test
    fun `Spracheinstellung wird aufgeloest`() {
        assertEquals(AppLanguage.GERMAN, AppLanguage.resolve("de"))
        assertEquals(AppLanguage.ENGLISH, AppLanguage.resolve("en"))
        assertEquals(AppLanguage.GERMAN, AppLanguage.resolve(AppLanguage.SYSTEM, Locale.GERMANY))
        assertEquals(AppLanguage.ENGLISH, AppLanguage.resolve(AppLanguage.SYSTEM, Locale.US))
    }

    private fun placeholders(text: String): List<String> =
        Regex("%[sd]").findAll(text).map { it.value }.toList()

    private companion object {
        /**
         * Die Schlüssel, die die App benutzt. Wird ein Schlüssel umbenannt,
         * muss er hier mit angepasst werden – der Test hält beide Sprachen zusammen.
         */
        val TRANSLATION_KEYS = listOf(
            "tab.leaderboard", "tab.news", "tab.votes", "tab.status", "tab.settings",
            "menu.settings", "menu.about",
            "subtitle.loading", "subtitle.players", "subtitle.postsLoading", "subtitle.posts",
            "subtitle.sitesLoading", "subtitle.sites", "subtitle.online", "subtitle.live", "subtitle.version",
            "cd.playerProfile", "cd.refresh", "cd.searchClear",
            "common.player", "common.copy", "common.copied", "common.retry", "common.close",
            "common.live", "common.online", "common.error", "common.waves", "common.kills",
            "common.games", "common.coins", "common.points",
            "leaderboard.empty.title", "leaderboard.empty.answered", "leaderboard.empty.notAsked",
            "leaderboard.test", "leaderboard.lastCheck", "leaderboard.search",
            "leaderboard.notFound", "leaderboard.notFound.text", "leaderboard.placements",
            "leaderboard.place", "leaderboard.leads", "leaderboard.noPlacements",
            "leaderboard.youLead", "leaderboard.pointsToFirst", "leaderboard.tied",
            "leaderboard.bestRank", "leaderboard.showInList", "leaderboard.notInList", "leaderboard.you",
            "vote.name", "vote.check", "vote.ready", "vote.title", "vote.found", "vote.notFound",
            "vote.enterName", "vote.sites", "vote.progress", "vote.progressCount", "vote.forPlayer",
            "vote.unlock", "vote.reward.default", "vote.readyAgain", "vote.againIn",
            "vote.action.vote", "vote.action.again", "vote.lastVoted", "vote.needName",
            "vote.footer.rewards", "vote.footer.browser", "vote.footer.inApp",
            "vote.empty.title", "vote.empty.text",
            "news.empty", "news.reload", "news.loadError",
            "player.load", "player.none", "player.empty", "player.enterName", "player.hint",
            "player.stat.waves", "player.stat.kills", "player.stat.games", "player.stat.coins",
            "player.votes", "player.lastPlayed",
            "status.empty.title", "status.empty.text", "status.network", "status.playersOnline",
            "status.reachable", "status.servers", "status.modes", "status.messages",
            "status.messages.empty.title", "status.messages.empty.text", "status.javaIp",
            "status.bedrockIp", "status.share", "status.shareText", "status.shareChooser",
            "status.community", "status.lan.title", "status.lan.onWifi", "status.lan.noWifi",
            "status.lan.intro", "status.lan.target", "status.lan.thisDevice", "status.lan.notInWifi",
            "status.lan.start", "status.lan.stop", "status.lan.errorWifi", "status.lan.errorStart",
            "status.lan.portBusy", "status.lan.footnote",
            "settings.noName", "settings.profile", "settings.name", "settings.name.hint",
            "settings.appearance", "settings.darkMode", "settings.colorScheme",
            "settings.effects", "settings.animations", "settings.animations.hint",
            "settings.haptics", "settings.haptics.hint", "settings.compact", "settings.compact.hint",
            "settings.updates", "settings.updates.auto", "settings.updates.auto.hint",
            "settings.updates.lastCheck", "settings.updates.checkNow", "settings.updates.searchInstall",
            "settings.voteBrowser", "settings.voteBrowser.hint", "settings.voteBrowser.inApp",
            "settings.voteBrowser.external", "settings.actions", "settings.actions.refresh",
            "settings.actions.replayIntro", "settings.actions.about", "settings.actions.github",
            "settings.legal", "settings.legal.hint", "settings.legal.lastSync", "settings.legal.offline",
            "settings.legal.imprint", "settings.legal.privacy", "settings.legal.terms",
            "settings.info", "settings.info.version", "settings.info.server", "settings.info.disclaimer",
            "settings.language", "settings.language.hint", "settings.language.system",
            "about.title", "about.text", "about.badge", "about.legalFooter", "about.openWeb",
            "welcome.eyebrow", "welcome.title", "welcome.text",
            "welcome.2.eyebrow", "welcome.2.title", "welcome.2.text",
            "welcome.3.eyebrow", "welcome.3.title", "welcome.3.text",
            "welcome.4.eyebrow", "welcome.4.title", "welcome.4.text",
            "welcome.nameTitle", "welcome.nameHint", "welcome.namePlaceholder",
            "welcome.start", "welcome.next", "welcome.later", "welcome.skip",
            "update.required", "update.blocked", "update.failed", "update.available",
            "update.current", "update.new", "update.critical", "update.explanation",
            "update.warning", "update.size", "update.cellular", "update.downloading",
            "update.downloadingPercent", "update.error", "update.corrupt", "update.incompatible",
            "update.otherKey", "update.allow", "update.loading", "update.retry", "update.download",
            "update.allowInstalls", "update.confirmCellular", "update.installNow", "update.github",
            "update.later", "update.cd.updatedTo", "update.notice.searching", "update.notice.failed",
            "update.notice.upToDate", "update.notice.offline", "update.notice.checkFailed",
            "update.notice.downloadOffline", "update.notice.downloadFailed", "update.notice.boardOk",
            "update.notice.boardFailed", "update.notice.boardLoadFailed", "update.notice.refreshFailed",
            "time.never", "time.justNow", "time.minute", "time.minutes", "time.hour", "time.hours",
            "time.day", "time.days", "time.clock",
            "lan.step1.title", "lan.step1.detail", "lan.step2.title", "lan.step2.detail",
            "lan.step3.title", "lan.step3.detail", "lan.step4.title", "lan.step4.detail",
        )
    }
}
