package de.mcaffen.app.data

import de.mcaffen.app.model.VoteSite

/**
 * Die Vote-Seiten des Netzwerks. Die Website liefert sie nicht über die API,
 * sondern bettet sie als JavaScript-Liste ein (`/assets/pages/voten.js`).
 * Diese Liste ist daraus übernommen und entspricht dem Stand der Website.
 */
object DefaultVoteSites {
    private const val REWARD = "1 Vote-Schlüssel + 100 Coins"

    val ALL: List<VoteSite> = listOf(
        VoteSite(
            site = "minecraft-server.eu",
            url = "https://minecraft-server.eu/vote/index/237E8",
            hours = 24,
            reward = REWARD,
        ),
        VoteSite(
            site = "minecraft-serverlist.net",
            url = "https://www.minecraft-serverlist.net/vote/60155/{user}",
            hours = 24,
            reward = REWARD,
        ),
        VoteSite(
            site = "serverliste.net",
            url = "https://serverliste.net/vote/5706",
            hours = 24,
            reward = REWARD,
        ),
        VoteSite(
            site = "mc-liste.de",
            url = "https://www.mc-liste.de/server/358/vote#serverVotePanel",
            hours = 24,
            reward = REWARD,
        ),
        VoteSite(
            site = "mcserv.org",
            url = "https://mcserv.org/de/vote/mcaffen",
            hours = 24,
            reward = REWARD,
        ),
    )
}
