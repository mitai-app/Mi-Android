package io.vonley.mi.ui.screens.consoles.domain.remote

import io.vonley.mi.models.enums.Feature
import io.vonley.mi.ui.screens.consoles.domain.model.Client
import kotlinx.coroutines.flow.StateFlow
import okhttp3.OkHttpClient
import java.net.Socket

/**
 * Scans the local network for consoles and owns the long-lived sockets
 * opened to the selected target.
 * @see io.vonley.mi.ui.screens.consoles.data.remote.SyncServiceImpl
 */
interface SyncService {

    val targetFlow: StateFlow<Client?>
    val target: Client? get() = targetFlow.value

    /** SSID of the connected Wi-Fi network, or empty when unknown. */
    val wifiSsid: String
    val localDeviceIp: String
    val isConnected: Boolean
    val http: OkHttpClient

    fun getClients(loop: Boolean = false)
    fun stop()
    fun setTarget(client: Client)
    fun cleanup()
    fun createSocket(client: Client?, feature: Feature): Socket?
    fun getSocket(client: Client?, feature: Feature): Socket?
}

operator fun SyncService.set(client: Client?, feature: Feature) {
    createSocket(client, feature)
}

operator fun SyncService.get(client: Client?, feature: Feature): Socket? {
    return getSocket(client, feature)
}
