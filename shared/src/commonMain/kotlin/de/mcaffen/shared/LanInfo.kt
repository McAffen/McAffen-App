package de.mcaffen.shared

/** Werte für die lokale Bedrock-LAN-Brücke auf Konsolen. */
object LanInfo {
    const val HOST = "bedrock.mcaffen.de"
    const val PORT = 19132
    const val BEDROCK_ADDRESS = "$HOST:$PORT"

    fun steps(localIp: String?): List<String> = listOf(
        "Konsole und Handy müssen im selben WLAN sein.",
        "In der App „LAN-Welt starten“ drücken.",
        "Auf der Konsole Minecraft öffnen → Spiele → Freunde → LAN-Welten.",
        localIp?.let { "Die lokale LAN-Adresse ist $it:$PORT; Zielserver: $BEDROCK_ADDRESS." }
            ?: "Die App verwendet lokal Port $PORT und leitet an $BEDROCK_ADDRESS weiter.",
    ).filterNotNull()
}
