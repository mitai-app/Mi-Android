package io.vonley.mi.ui.screens.consoles.data.remote

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.text.format.Formatter
import io.vonley.mi.common.coroutines.supervisedIoContext
import io.vonley.mi.extensions.e
import io.vonley.mi.extensions.i
import io.vonley.mi.extensions.v
import io.vonley.mi.models.enums.Feature
import io.vonley.mi.models.enums.PlatformType
import io.vonley.mi.models.enums.Protocol
import io.vonley.mi.ui.screens.consoles.data.local.ConsoleDao
import io.vonley.mi.ui.screens.consoles.domain.model.Client
import io.vonley.mi.ui.screens.consoles.domain.model.Console
import io.vonley.mi.ui.screens.consoles.domain.model.NetworkClient
import io.vonley.mi.ui.screens.consoles.domain.remote.SyncService
import io.vonley.mi.utils.SharedPreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import okhttp3.OkHttpClient
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext

/**
 * Scans the subnet for reachable hosts, probes them for console services and
 * persists the results to [ConsoleDao]. Runs at most one scan job at a time.
 */
class SyncServiceImpl(
    context: Context,
    private val dao: ConsoleDao,
    private val manager: SharedPreferenceManager,
    override val http: OkHttpClient
) : SyncService, CoroutineScope {

    override val coroutineContext: CoroutineContext = supervisedIoContext(TAG)

    private val appContext = context.applicationContext
    private val cm = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val wm = appContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    private val cachedTargets = ConcurrentHashMap<String, ConcurrentHashMap<Feature, Socket>>()

    private var activeJob: Job? = null

    private val _target = MutableStateFlow<Client?>(null)
    override val targetFlow: StateFlow<Client?> = _target.asStateFlow()

    @Suppress("DEPRECATION")
    override val wifiSsid: String
        get() = wm.connectionInfo?.ssid?.removeSurrounding("\"").orEmpty()

    @Suppress("DEPRECATION")
    override val localDeviceIp: String
        get() = Formatter.formatIpAddress(wm.connectionInfo?.ipAddress ?: 0)

    override val isConnected: Boolean
        get() {
            val caps = cm.getNetworkCapabilities(cm.activeNetwork ?: return false) ?: return false
            return caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        }

    override fun getSocket(client: Client?, feature: Feature): Socket? {
        if (client == null) return null
        return cachedTargets[client.ip]?.get(feature)?.takeIf { it.isUsable }
    }

    override fun createSocket(client: Client?, feature: Feature): Socket? {
        if (client == null) return null
        val sockets = cachedTargets.getOrPut(client.ip) { ConcurrentHashMap() }
        synchronized(sockets) {
            sockets[feature]?.let { existing ->
                if (existing.isUsable) return existing
                sockets.remove(feature)
                existing.closeQuietly()
            }
            for (port in feature.ports) {
                val socket = Socket()
                try {
                    socket.connect(InetSocketAddress(client.ip, port), CONNECT_TIMEOUT_MS)
                    sockets[feature] = socket
                    return socket
                } catch (e: Throwable) {
                    socket.closeQuietly()
                }
            }
            return null
        }
    }

    override fun getClients(loop: Boolean) {
        if (activeJob?.isActive == true) return
        activeJob = launch {
            do {
                scan()
                if (!loop) break
                "[fetching again in ${manager.scanInterval} seconds]".v(TAG)
                delay(manager.scanInterval * 1000L)
            } while (isActive)
        }
    }

    override fun stop() {
        activeJob?.cancel()
        activeJob = null
    }

    override fun setTarget(client: Client) {
        manager.targetName = client.ip
        _target.value = client
    }

    override fun cleanup() {
        cachedTargets.values.forEach { sockets -> sockets.values.forEach { it.closeQuietly() } }
        cachedTargets.clear()
    }

    private suspend fun scan() {
        if (!isConnected) {
            "[Scan] skipped, not on wifi".i(TAG)
            return
        }
        val wifi = wifiSsid
        val clients = fetchClients(wifi)
        val consoles = fetchConsoles(clients)
        persist(wifi, consoles)
    }

    /**
     * Pings every IPv4 address on the /24 the device is on.
     * TODO: Accommodate for IPV6 Clients
     */
    private suspend fun fetchClients(wifi: String): List<Client> = coroutineScope {
        val ownIp = localDeviceIp
        if (ownIp == "0.0.0.0") return@coroutineScope emptyList()
        val prefix = ownIp.substringBeforeLast(".") + "."
        val permits = Semaphore(SCAN_PARALLELISM)
        (1 until 255)
            .map { "$prefix$it" }
            .filter { it != ownIp }
            .map { ip ->
                async {
                    permits.withPermit {
                        try {
                            if (InetAddress.getByName(ip).isReachable(PING_TIMEOUT_MS)) {
                                "[Client IP] ($ip) is reachable".v(TAG)
                                NetworkClient(ip = ip, wifi = wifi)
                            } else null
                        } catch (e: Throwable) {
                            null
                        }
                    }
                }
            }
            .awaitAll()
            .filterNotNull()
            .also { "[FetchClients::End] reachable: ${it.size}".v(TAG) }
    }

    private suspend fun fetchConsoles(clients: List<Client>): List<Console> = coroutineScope {
        val permits = Semaphore(SCAN_PARALLELISM)
        clients.map { client ->
            async {
                permits.withPermit {
                    val console = probe(client)
                    if (console == null) dao.deleteUnpinned(client.ip)
                    console
                }
            }
        }.awaitAll().filterNotNull()
    }

    private suspend fun persist(wifi: String, consoles: List<Console>) {
        try {
            consoles.forEach { console ->
                if (dao.exists(console.ip)) {
                    dao.update(console.ip, console.type, console.features, true, console.wifi)
                } else {
                    dao.insert(console)
                }
            }
            val found = consoles.map { it.ip }
            dao.deleteMissing(wifi, found)
            dao.markUnreachable(wifi, found)
            "[FetchConsoles::End] consoles: ${consoles.size}".v(TAG)
        } catch (t: Throwable) {
            "[FetchConsoles::End] failed to persist scan: ${t.message}".e(TAG, t)
        }
    }

    /**
     * Checks for active ports while avoiding unstable ones. Long-lived services
     * keep their socket open in [cachedTargets] so later commands reuse it.
     */
    private fun probe(client: Client): Console? {
        val features = Feature.stableFeatures.filter { feature ->
            try {
                when {
                    feature in Feature.allowedToOpen && feature.protocol == Protocol.SOCKET ->
                        createSocket(client, feature) != null

                    feature == Feature.WEBMAN ->
                        isPortOpen(client.ip, feature.ports.first()) && feature.validate(client, http)

                    else -> feature.ports.any { port -> isPortOpen(client.ip, port) }
                }
            } catch (e: Throwable) {
                false
            }
        }
        if (features.isEmpty()) return null
        val type = when {
            features.any { it in PlatformType.PS4.features } -> PlatformType.PS4
            features.any { it in PlatformType.PS3.features } -> PlatformType.PS3
            else -> PlatformType.UNKNOWN
        }
        return Console(
            ip = client.ip,
            name = client.ip,
            type = type,
            features = features,
            lastKnownReachable = true,
            wifi = client.wifi
        )
    }

    private fun isPortOpen(ip: String, port: Int): Boolean = Socket().use { socket ->
        try {
            socket.connect(InetSocketAddress(ip, port), PORT_PROBE_TIMEOUT_MS)
            true
        } catch (e: Throwable) {
            false
        }
    }

    private val Socket.isUsable: Boolean
        get() = isConnected && !isClosed && !isOutputShutdown && !isInputShutdown

    private fun Socket.closeQuietly() {
        try {
            close()
        } catch (_: Throwable) {
        }
    }

    companion object {
        const val TAG = "SyncService"
        private const val SCAN_PARALLELISM = 32
        private const val PING_TIMEOUT_MS = 300
        private const val PORT_PROBE_TIMEOUT_MS = 1000
        private const val CONNECT_TIMEOUT_MS = 2000
    }
}
