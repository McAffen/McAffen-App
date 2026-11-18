package de.mcaffen.app.data

import android.content.Context
import android.net.wifi.WifiManager
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.Collections

/**
 * Hilft beim LAN-Trick für Konsolen (PS5, Switch, Xbox, Smart-TV):
 *
 * Ein Bedrock-Server im Internet (bedrock.mcaffen.de) wird von Konsolen nicht
 * gefunden – die suchen nur im eigenen Netz. Wenn ein Gerät im selben WLAN
 * (z. B. dieses Handy) den Server auf Port 19132 weiterleitet und sich als
 * "Minecraft-Broadcast" meldet, erscheint er auf der Konsole als LAN-Welt.
 *
 * Wichtig und ehrlich: Die App kann das nicht selbst tun. Android erlaubt einer
 * normalen App keinen offenen Port unter 1024 und kein UDP-Broadcast-Listener
 * ohne Systemrechte. Deshalb liefert diese Klasse nur die echten Werte
 * (eigene IP, Ziel, Port) und die Anleitung; das Weiterleiten übernimmt eine
 * Hilfs-App oder ein PC im selben Netz.
 */
object LanBridge {

    const val HOST: String = "bedrock.mcaffen.de"
    const val PORT: Int = 19132

    /** Eigene IPv4-Adresse im WLAN – das ist die Adresse, die die Konsole anspricht. */
    fun localAddress(context: Context): String? {
        runCatching {
            @Suppress("DEPRECATION")
            val wifi = context.applicationContext
                .getSystemService(Context.WIFI_SERVICE) as? WifiManager
            @Suppress("DEPRECATION")
            val ip = wifi?.connectionInfo?.ipAddress ?: 0
            if (ip != 0) {
                @Suppress("DEPRECATION")
                val formatted = String.format(
                    "%d.%d.%d.%d",
                    ip and 0xff,
                    ip shr 8 and 0xff,
                    ip shr 16 and 0xff,
                    ip shr 24 and 0xff,
                )
                if (formatted != "0.0.0.0") return formatted
            }
        }
        // Fallback: Netzwerk-Schnittstellen durchsuchen (funktioniert auch ohne WLAN-Rechte).
        return runCatching {
            Collections.list(NetworkInterface.getNetworkInterfaces())
                .asSequence()
                .filter { it.isUp && !it.isLoopback }
                .flatMap { Collections.list(it.inetAddresses).asSequence() }
                .filterIsInstance<Inet4Address>()
                .firstOrNull { it.isSiteLocalAddress }
                ?.hostAddress
        }.getOrNull()
    }

    /** Ist das Gerät im WLAN (nicht mobil) unterwegs? */
    fun isOnWifi(context: Context): Boolean {
        return runCatching {
            @Suppress("DEPRECATION")
            val wifi = context.applicationContext
                .getSystemService(Context.WIFI_SERVICE) as? WifiManager
            @Suppress("DEPRECATION")
            wifi?.isWifiEnabled == true && localAddress(context) != null
        }.getOrDefault(false)
    }

    /**
     * Schritte für die Einrichtung – bewusst als Klartext, weil sie auf einem
     * zweiten Gerät (PC oder Hilfs-App) ausgeführt werden müssen.
     */
    fun steps(localIp: String?): List<LanStep> {
        val address = localIp ?: "IP dieses Geräts"
        return listOf(
            LanStep(
                title = "Gerät im selben WLAN",
                detail = "Handy, PC oder Tablet, das weiterleitet, muss im selben WLAN wie " +
                    "die Konsole sein. Mobilfunk funktioniert nicht.",
            ),
            LanStep(
                title = "UDP-Weiterleitung einrichten",
                detail = "Auf dem Gerät Port $PORT/UDP an $HOST:$PORT weiterleiten " +
                    "– mit einer Port-Weiterleitungs-App aus dem Store oder einem " +
                    "kleinen Skript auf dem PC.",
            ),
            LanStep(
                title = "LAN-Ankündigung senden",
                detail = "Die Konsole findet Welten nur über eine Ankündigung im Netz. " +
                    "Eine LAN-Proxy-App übernimmt Weiterleitung und Ankündigung zusammen " +
                    "– dort als Ziel $HOST:$PORT eintragen.",
            ),
            LanStep(
                title = "In der Konsole suchen",
                detail = "Im Spiel unter „Freunde“ bzw. „LAN-Welten“ nach Welten suchen. " +
                    "Die Welt erscheint als LAN-Welt; als Adresse meldet sich $address.",
            ),
        )
    }
}

data class LanStep(val title: String, val detail: String)
