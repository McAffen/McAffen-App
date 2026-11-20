package de.mcaffen.app.data

import de.mcaffen.app.ui.AppLanguage
import de.mcaffen.app.ui.Strings

import android.content.Context
import android.net.wifi.WifiManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.Inet4Address
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.SocketTimeoutException
import java.util.Collections
import java.util.concurrent.atomic.AtomicReference

/**
 * Lokaler Bedrock-LAN-Proxy: nimmt die lokalen RakNet-Pakete entgegen,
 * leitet sie an den öffentlichen Server weiter und beantwortet die LAN-Pings
 * direkt. Port 19132 liegt über dem privilegierten Bereich und kann von einer
 * normalen Android-App verwendet werden.
 */
object LanBridge {

    const val HOST: String = "bedrock.mcaffen.de"
    const val PORT: Int = 19132
    private const val BUFFER_SIZE = 2048
    private const val RAKNET_MAGIC_SIZE = 16
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val clientAddress = AtomicReference<InetSocketAddress?>()
    private var proxyJob: Job? = null
    private var socket: DatagramSocket? = null
    private var multicastLock: WifiManager.MulticastLock? = null

    @Synchronized
    fun start(context: Context): Result<Unit> {
        if (proxyJob?.isActive == true) return Result.success(Unit)
        return runCatching {
            val wifi = context.applicationContext
                .getSystemService(Context.WIFI_SERVICE) as? WifiManager
            wifi?.createMulticastLock("mcaffen-bedrock-lan")?.also {
                it.setReferenceCounted(false)
                it.acquire()
                multicastLock = it
            }
            val localSocket = DatagramSocket(null).apply {
                reuseAddress = true
                broadcast = true
                soTimeout = 250
                bind(InetSocketAddress(InetAddress.getByName("0.0.0.0"), PORT))
            }
            socket = localSocket
            proxyJob = scope.launch {
                runProxy(localSocket)
            }
        }.onFailure {
            stop()
        }.map { Unit }
    }

    @Synchronized
    fun stop() {
        proxyJob?.cancel()
        proxyJob = null
        socket?.close()
        socket = null
        clientAddress.set(null)
        multicastLock?.let { if (it.isHeld) it.release() }
        multicastLock = null
    }

    fun isRunning(): Boolean = proxyJob?.isActive == true

    private suspend fun runProxy(localSocket: DatagramSocket) {
        val upstream = InetAddress.getByName(HOST)
        val buffer = ByteArray(BUFFER_SIZE)
        try {
            while (scope.isActive && !localSocket.isClosed) {
                val packet = DatagramPacket(buffer, buffer.size)
                try {
                    localSocket.receive(packet)
                } catch (_: SocketTimeoutException) {
                    continue
                }
                val data = packet.data.copyOfRange(packet.offset, packet.offset + packet.length)
                if (packet.address == upstream) {
                    clientAddress.get()?.let { target ->
                        localSocket.send(DatagramPacket(data, data.size, target))
                    }
                } else if (isUnconnectedPing(data)) {
                    val target = InetSocketAddress(packet.address, packet.port)
                    clientAddress.set(target)
                    sendLanPong(localSocket, target, data)
                } else {
                    clientAddress.set(InetSocketAddress(packet.address, packet.port))
                    localSocket.send(DatagramPacket(data, data.size, upstream, PORT))
                }
            }
        } finally {
            if (!localSocket.isClosed) localSocket.close()
        }
    }

    private fun isUnconnectedPing(data: ByteArray): Boolean =
        data.size >= 1 + 8 + RAKNET_MAGIC_SIZE && data[0].toInt() and 0xff == 0x01

    private fun sendLanPong(
        socket: DatagramSocket,
        target: InetSocketAddress,
        ping: ByteArray,
    ) {
        val name = "MCPE;McAffen;686;1.21.0;0;100;0;McAffen;Survival;1;19132;19133;"
            .toByteArray(Charsets.UTF_8)
        val pong = ByteArray(33 + 2 + name.size)
        pong[0] = 0x1c
        if (ping.size >= 33) {
            ping.copyInto(pong, destinationOffset = 1, startIndex = 1, endIndex = 33)
        }
        val nameLengthOffset = 33
        java.nio.ByteBuffer.wrap(pong, nameLengthOffset, 2)
            .putShort(name.size.toShort())
        name.copyInto(pong, destinationOffset = nameLengthOffset + 2)
        socket.send(DatagramPacket(pong, pong.size, target))
    }

    /** IP-Adresse des aktiven WLAN-Netzes (über ConnectivityManager, ohne Sonderrechte). */
    private fun wifiAddressFromConnectivity(context: Context): String? = runCatching {
        val cm = context.getSystemService(android.net.ConnectivityManager::class.java)
            ?: return@runCatching null
        val network = cm.activeNetwork ?: return@runCatching null
        val caps = cm.getNetworkCapabilities(network) ?: return@runCatching null
        val isWifi = caps.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI)
        if (!isWifi) return@runCatching null
        val props = cm.getLinkProperties(network) ?: return@runCatching null
        props.linkAddresses
            .asSequence()
            .map { it.address }
            .filterIsInstance<java.net.Inet4Address>()
            .firstOrNull { !it.isLoopbackAddress }
            ?.hostAddress
    }.getOrNull()

    /** Eigene IPv4-Adresse im WLAN – das ist die Adresse, die die Konsole anspricht. */
    fun localAddress(context: Context): String? {
        // 1. Weg (zuverlässig ab Android 12): aktives Netz über ConnectivityManager.
        wifiAddressFromConnectivity(context)?.let { return it }
        // 2. Weg: WifiManager (ältere Versionen).
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
        // 3. Weg: Netzwerk-Schnittstellen durchsuchen (funktioniert auch ohne WLAN-Rechte).
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

    /**
     * Ist das Gerät gerade in einem WLAN? Geprüft über das aktive Netz
     * (zuverlässig) mit Rückfall auf eine private IPv4-Adresse.
     */
    fun isOnWifi(context: Context): Boolean {
        val viaConnectivity = runCatching {
            val cm = context.getSystemService(android.net.ConnectivityManager::class.java)
            val network = cm?.activeNetwork
            val caps = network?.let { cm.getNetworkCapabilities(it) }
            caps?.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI) == true
        }.getOrDefault(false)
        if (viaConnectivity) return true
        // Kein rein privater-IP-Rückschluss: mobile Daten können ebenfalls eine
        // 10.x-Adresse erhalten. Fallback nur über den tatsächlichen WifiManager.
        return runCatching {
            @Suppress("DEPRECATION")
            val wifi = context.applicationContext
                .getSystemService(Context.WIFI_SERVICE) as? WifiManager
            @Suppress("DEPRECATION")
            wifi?.connectionInfo?.bssid != null
        }.getOrDefault(false)
    }

    /**
     * Kurze Einrichtungsschritte für die lokale Bedrock-LAN-Brücke in der
     * gewählten Sprache (Standard: Deutsch).
     */
    fun steps(localIp: String?, language: AppLanguage = AppLanguage.GERMAN): List<LanStep> {
        val strings = Strings.of(language)
        val address = localIp ?: strings["lan.thisDeviceIp"]
        return listOf(
            LanStep(
                title = strings["lan.step1.title"],
                detail = strings["lan.step1.detail"],
            ),
            LanStep(
                title = strings["lan.step2.title"],
                detail = strings.format("lan.step2.detail", PORT, "$HOST:$PORT"),
            ),
            LanStep(
                title = strings["lan.step3.title"],
                detail = strings["lan.step3.detail"],
            ),
            LanStep(
                title = strings["lan.step4.title"],
                detail = strings.format("lan.step4.detail", "$address:$PORT"),
            ),
        )
    }
}

data class LanStep(val title: String, val detail: String)
