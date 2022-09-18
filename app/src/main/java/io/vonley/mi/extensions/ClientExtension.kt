package io.vonley.mi.extensions

import android.net.wifi.WifiInfo
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import io.vonley.mi.ui.main.console.data.remote.SyncService
import io.vonley.mi.ui.main.console.domain.model.Console
import io.vonley.mi.models.enums.Feature
import io.vonley.mi.models.enums.PlatformType
import io.vonley.mi.ui.main.console.domain.model.Client
import java.net.InetAddress
import java.net.Socket

inline fun <reified T> Gson.fromJson(json: String) =
    fromJson<T>(json, object : TypeToken<T>() {}.type)

inline fun <reified T> String.fromJson(): T? =
    GsonBuilder().create().fromJson<T>(this, object : TypeToken<T>() {}.type)

inline fun <reified T> T.toJson(): String =
    GsonBuilder().create().toJson(this)

inline fun <reified T : Client> T.getSocket(sync: SyncService, port: Feature): Socket? {
    return sync.createSocket(this, port)
}

fun InetAddress.client(wi: WifiInfo): Client {
    return object : Client {

        private var deviceName: String = canonicalHostName
        private var isReachable = false
        private var platformType: PlatformType = PlatformType.UNKNOWN
        private var wifiInfo: String = wi.ssid ?: "not connected?"
        private var feats: List<Feature> = emptyList()
        private var pin: Boolean = false


        override val ip: String
            get() = hostAddress ?: canonicalHostName

        override var name: String
            get() = deviceName
            set(value) {
                deviceName = value
            }

        override var type: PlatformType
            get() = platformType
            set(value) {
                platformType = value
            }

        override var features: List<Feature>
            get() = feats
            set(value) {
                feats = value
            }

        override var wifi: String
            get() = wifiInfo
            set(value) {
                wifiInfo = value
            }


        override var lastKnownReachable: Boolean
            get() = isReachable
            set(value) {
                isReachable = value
            }
        override var pinned: Boolean
            get() = pin
            set(value) {
                pin = value
            }

    }
}

suspend fun Client.console(service: SyncService): Console? {
    val actives = openActivePorts(service)
    if (actives.isNotEmpty()) {
        val features = actives
        features.e("FEATURES")
        // TODO: For now we only recognize rpi and orbisapi ports, when stable it should perform
        // Since goldenhen bin loader isnt that stable, we cant really keep doing requests on it
        // It will eventually lock and wont be able to connect or goldenhen will be in a hang state
        val ps4 = arrayOf(Feature.GOLDENHEN, Feature.NETCAT, Feature.RPI, Feature.ORBISAPI, Feature.KLOG)
        val ps3 = arrayOf(Feature.CCAPI, Feature.PS3MAPI, Feature.WEBMAN)
        val isPs4 = features.any { p -> p in ps4 }
        val isPs3 = features.any { p -> p in ps3 }
        val type = when {
            isPs4 -> {
                PlatformType.PS4
            }
            isPs3 -> {
                PlatformType.PS3
            }
            else -> {
                PlatformType.UNKNOWN
            }
        }
        return Console(
            ip,
            ip,
            type,
            features,
            lastKnownReachable,
            wifi
        )
    }
    return null
}