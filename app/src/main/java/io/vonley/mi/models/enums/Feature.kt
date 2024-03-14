package io.vonley.mi.models.enums

import android.content.Context
import android.os.Parcelable
import androidx.annotation.StringRes
import androidx.room.Entity
import io.vonley.mi.R
import io.vonley.mi.ui.screens.consoles.domain.remote.SyncService
import io.vonley.mi.ui.screens.consoles.domain.model.Client
import kotlinx.parcelize.Parcelize
import okhttp3.Request

/**
 * We wont scan for netcat, this port will be ignored since its a
 * oneshot jb exploit. Our MiJbServer class will handle the netcat
 * payload
 */
@Entity
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

    fun validate(client: Client, service: SyncService): Boolean {
        fun req(url: String): String {
            val req = Request.Builder()
                .url(url)
                .get()
                .build()
            val execute = service.http.newCall(req)
            val response = execute.execute()
            return response.body.string()
        }

        return when (this) {
            WEBMAN -> req("http://${client.ip}:${ports.first()}/index.ps3").let { s ->
                s.lowercase().contains("ps3mapi") || s.lowercase()
                    .contains("webman") || s.lowercase().contains("dex") ||
                        s.lowercase().contains("d-rex") || s.lowercase()
                    .contains("cex") || s.lowercase()
                    .contains("rebug") ||
                        s.lowercase().contains("rsx")
            }
            CCAPI -> req("http://${client.ip}:${ports.first()}/ccapi").let {
                true
            }
            else -> true
        }

    }

    companion object {
        fun find(context: Context, id: String): Feature? {
            return values().firstOrNull { p -> context.getString(p.id) == id }
        }

        /**
         * These fields are for client
         * Due to GoldenHen & NetCat being the payload sender,
         * we want to be extract careful what we send there.
         * Goldenhen Bin Uploader stops working after a while
         * NetCat
         */
        //arrayOf(ORBISAPI, RPI, PS3MAPI, CCAPI, WEBMAN, FTP, KLOG)
        val stableFeatures: Array<Feature> =
            values().filterNot { p -> p in arrayOf(NONE, NETCAT, GOLDENHEN) }.toTypedArray()

        /**
         * These are stable sockets that are allowed to be opened for however long
         */
        val allowedToOpen: Array<Feature> = arrayOf(PS3MAPI, CCAPI, WEBMAN, ORBISAPI, KLOG)
    }
}