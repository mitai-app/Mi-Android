package io.vonley.mi.models.enums

import android.os.Parcelable
import androidx.annotation.StringRes
import io.vonley.mi.R
import io.vonley.mi.ui.screens.consoles.domain.model.Client
import kotlinx.parcelize.Parcelize
import okhttp3.OkHttpClient
import okhttp3.Request

/**
 * We wont scan for netcat, this port will be ignored since its a
 * oneshot jb exploit. Our MiJbServer class will handle the netcat
 * payload
 */
@Parcelize
enum class Feature(
    val title: String,
    @StringRes val id: Int,
    val protocol: Protocol,
    vararg val ports: Int
) : Parcelable {
    NONE("None", R.string.feature_none, Protocol.NONE, 0),
    NETCAT("Netcat", R.string.feature_netcat, Protocol.SOCKET, 9021, 9020),
    GOLDENHEN("Golden Hen", R.string.feature_goldhen, Protocol.SOCKET, 9090),
    ORBISAPI("Orbis API", R.string.feature_orbisapi, Protocol.SOCKET, 6023),
    RPI("Remote Package Installer", R.string.feature_rpi, Protocol.HTTP, 12800),
    PS3MAPI("PS3MAPI", R.string.feature_ps3mapi, Protocol.SOCKET, 7887),
    CCAPI("CCAPI", R.string.feature_ccapi, Protocol.HTTP, 6333),
    WEBMAN("WebMan", R.string.feature_webman, Protocol.HTTP, 80),
    KLOG("Klog", R.string.feature_klog, Protocol.SOCKET, 3232),
    FTP("FTP", R.string.feature_ftp, Protocol.FTP, 21, 2121);

    /**
     * Confirms an open port really is this service. Only HTTP services that share
     * common ports (port 80 is on most routers) need a content check.
     */
    fun validate(client: Client, http: OkHttpClient): Boolean {
        fun body(url: String): String {
            val request = Request.Builder().url(url).get().build()
            return http.newCall(request).execute().use { it.body.string() }
        }

        return when (this) {
            WEBMAN -> body("http://${client.ip}:${ports.first()}/index.ps3").lowercase().let { s ->
                WEBMAN_MARKERS.any { marker -> marker in s }
            }

            else -> true
        }
    }

    companion object {
        private val WEBMAN_MARKERS = listOf("ps3mapi", "webman", "dex", "d-rex", "cex", "rebug", "rsx")

        /**
         * These fields are for client
         * Due to GoldenHen & NetCat being the payload sender,
         * we want to be extract careful what we send there.
         * Goldenhen Bin Uploader stops working after a while
         */
        val stableFeatures: List<Feature> = entries.filterNot { p -> p in listOf(NONE, NETCAT, GOLDENHEN) }

        /**
         * These are stable sockets that are allowed to be opened for however long
         */
        val allowedToOpen: List<Feature> = listOf(PS3MAPI, CCAPI, WEBMAN, ORBISAPI, KLOG)

        /** Services with an interactive panel in the console control sheet. */
        val controllable: List<Feature> = listOf(PS3MAPI, WEBMAN, CCAPI, KLOG)
    }
}
