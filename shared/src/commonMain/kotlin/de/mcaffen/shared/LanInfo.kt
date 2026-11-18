package de.mcaffen.shared

/**
 * Werte für den LAN-Trick auf Konsolen (PS5, Switch, Xbox): Adresse und Port,
 * die die Konsole im eigenen WLAN anspricht. Das Weiterleiten selbst kann keine
 * App übernehmen – dafür ist ein Gerät im Netz mit offenem UDP-Port nötig.
 */
object LanInfo {
    const val HOST = "bedrock.mcaffen.de"
    const val PORT = 19132
    const val BEDROCK_ADDRESS = "$HOST:$PORT"

    fun steps(localIp: String?): List<String> = listOf(
        "Konsole und Handy müssen im selben WLAN sein.",
        "Auf der Konsole Minecraft öffnen → Spiele → Freunde → „Netzwerkspiel hinzufügen“.",
        "Serveradresse eingeben: $BEDROCK_ADDRESS, Port: $PORT.",
        localIp?.let { "Alternativ im LAN: $it:$PORT (dafür muss ein Gerät im Netz weiterleiten)." }
            ?: "Für eine echte LAN-Welt braucht ein Gerät im Netz die Weiterleitung auf Port $PORT.",
    ).filterNotNull()
}
