package io.vonley.mi.di.network.protocols.common

import io.vonley.mi.common.base.BaseClient
import io.vonley.mi.di.network.PSXService
import io.vonley.mi.di.network.protocols.common.cmds.Boot
import io.vonley.mi.di.network.protocols.common.cmds.Buzzer
import io.vonley.mi.extensions.e
import io.vonley.mi.models.enums.Feature
import io.vonley.mi.ui.screens.consoles.domain.remote.get
import okhttp3.OkHttpClient
import java.io.BufferedReader
import java.io.IOException
import java.io.OutputStream
import java.net.Socket
import java.net.SocketException

interface PSXNotifier {
    suspend fun notify(message: String)
    suspend fun boot(ps3boot: Boot)
    suspend fun buzzer(buzz: Buzzer)
}

typealias OnPipeReconnected = suspend () -> String?
typealias OnReAuth = suspend () -> String?

/** A service exposed by the selected console. */
interface PSXProtocol : BaseClient {

    val service: PSXService
    val feature: Feature

    override val http: OkHttpClient get() = service.http

    override val TAG: String
        get() = PSXProtocol::class.qualifiedName ?: PSXProtocol::javaClass.name
}

/** A console service spoken over a raw, line-based TCP socket. */
interface SocketProtocol : PSXProtocol {

    val socketOrNull: Socket? get() = service.sync[service.target, feature]

    val socket: Socket
        get() = socketOrNull ?: throw IOException("Not connected to ${feature.title}")

    val pw: OutputStream
    val br: BufferedReader

    suspend fun sendAndRecv(data: String?): String? {
        return try {
            send(data)
            recv()
        } catch (e: SocketException) {
            // pipe is broken
            if (socketOrNull?.isConnected == true) {
                return reconnectPipes {
                    return@reconnectPipes sendAndRecv(data)
                }
            }
            null
        }
    }

    suspend fun reconnectPipes(callback: OnPipeReconnected): String? {
        return null
    }

    suspend fun send(data: String?) {
        "sending: $data".e(TAG)
        pw.write(data?.toByteArray() ?: return)
        pw.flush()
    }

    suspend fun recv(): String? {
        val text = br.readLine()
        "recv'd: $text".e(TAG)
        return text
    }

    @Throws(IOException::class)
    fun close() {
        socketOrNull?.takeIf { it.isConnected }?.close()
    }
}
