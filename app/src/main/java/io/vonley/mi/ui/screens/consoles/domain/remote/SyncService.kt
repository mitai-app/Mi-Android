package io.vonley.mi.ui.screens.consoles.domain.remote

import android.net.Network
import android.net.NetworkInfo
import android.net.wifi.WifiInfo
import android.text.format.Formatter
import androidx.lifecycle.LiveData
import io.vonley.mi.di.network.handlers.ClientHandler
import io.vonley.mi.di.network.listeners.OnConsoleListener
import io.vonley.mi.models.enums.Feature
import io.vonley.mi.ui.screens.consoles.domain.model.Client
import kotlinx.coroutines.CoroutineScope
import okhttp3.OkHttpClient
import java.net.Socket

/**
 * Implementations are in this class
 * @see SyncServiceImpl
 */
interface SyncService : CoroutineScope {

    val liveTarget: LiveData<Client>
    val target: Client? get() = liveTarget.value
    val wifiInfo: WifiInfo
    val activeNetworkInfo: NetworkInfo?
    val activeNetwork: Network?
    val localDeviceIpInt: Int get() = wifiInfo.ipAddress
    val localDeviceIp: String get() = Formatter.formatIpAddress(localDeviceIpInt)
    val isConnected: Boolean
    val handlers: HashMap<Class<*>, ClientHandler>
    val TAG: String
    val http: OkHttpClient

    fun cleanup()
    fun initialize()
    fun isNetworkAvailable(): Boolean
    fun isWifiAvailable(): Boolean
    fun getClients(loop: Boolean = false)
    fun setTarget(client: Client)
    fun addConsoleListener(console: OnConsoleListener)
    fun stop()
    fun removeConsoleListener(console: OnConsoleListener)
    fun createSocket(client: Client?, feature: Feature): Socket?
    fun getSocket(client: Client?, feature: Feature): Socket?
}
