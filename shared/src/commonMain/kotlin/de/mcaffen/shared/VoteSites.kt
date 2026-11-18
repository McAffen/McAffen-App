package de.mcaffen.shared

/**
 * Die Vote-Seiten des Netzwerks. Die Website liefert sie nicht über die API,
 * sondern bettet sie als JavaScript-Liste ein (`/assets/pages/voten.js`).
 */
object DefaultVoteSites {
    private const val REWARD = "1 Vote-Schlüssel + 100 Coins"

    val all: List<VoteSite> = listOf(
        VoteSite("minecraft-server.eu", "https://minecraft-server.eu/vote/index/237E8", 24, REWARD),
        VoteSite("minecraft-serverlist.net", "https://www.minecraft-serverlist.net/vote/60155/{user}", 24, REWARD),
        VoteSite("serverliste.net", "https://serverliste.net/vote/5706", 24, REWARD),
        VoteSite("mc-liste.de", "https://www.mc-liste.de/server/358/vote#serverVotePanel", 24, REWARD),
        VoteSite("mcserv.org", "https://mcserv.org/de/vote/mcaffen", 24, REWARD),
    )

    /** Vote-Link mit eingesetztem Spielernamen (die Website nutzt `{user}`). */
    fun urlFor(site: VoteSite, playerName: String): String =
        site.url.replace("{user}", encodeUrlComponent(playerName.trim()))

    /** Einfache URL-Kodierung für Namen (Leerzeichen, Sonderzeichen). */
    fun encodeUrlComponent(value: String): String = buildString {
        value.forEach { ch ->
            when {
                ch.isLetterOrDigit() || ch == '-' || ch == '_' || ch == '.' || ch == '~' -> append(ch)
                else -> {
                    val bytes = ch.toString().encodeToByteArray()
                    bytes.forEach { b ->
                        append('%')
                        append(HEX[(b.toInt() shr 4) and 0xF])
                        append(HEX[b.toInt() and 0xF])
                    }
                }
            }
        }
    }

    private const val HEX = "0123456789ABCDEF"
}
