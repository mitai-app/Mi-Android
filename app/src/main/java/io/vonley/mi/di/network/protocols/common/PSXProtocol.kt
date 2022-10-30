package io.vonley.mi.di.network.protocols.common

import io.vonley.mi.common.base.BaseClient
import io.vonley.mi.di.network.PSXService
import io.vonley.mi.di.network.protocols.common.cmds.Boot
import io.vonley.mi.di.network.protocols.common.cmds.Buzzer
import io.vonley.mi.extensions.e
import io.vonley.mi.models.enums.Feature
import okhttp3.Callback
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.RequestBody
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
interface PSXProtocol : BaseClient {

    override val http: OkHttpClient get() = service.http
    override fun post(url: String, body: RequestBody, headers: Headers) =
        service.post(url, body, headers)

    override fun post(url: String, body: RequestBody, headers: Headers, response: Callback) =
        service.post(url, body, headers, response)

    override fun getRequest(url: String, response: Callback) = service.getRequest(url, response)
    override fun getRequest(url: String) = service.getRequest(url)

    override val TAG: String
        get() = PSXProtocol::class.qualifiedName ?: PSXProtocol::javaClass.name

    val service: PSXService
    val feature: Feature
    val socket: Socket
    val pw: OutputStream
    val br: BufferedReader

    suspend fun sendAndRecv(data: String?): String? {
        return try {
            send(data)
            recv()
        } catch (e: SocketException) {
            //pipe is broken
            if(socket.isConnected) {
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
        "sending: ${data.toString()}".e(TAG)
        pw.write(data?.toByteArray())
        pw.flush()
        "sent: ${data.toString()}".e(TAG)
    }

    suspend fun recv(): String? {
        "recv'ing".e(TAG)
        var text = br.readLine()
        "recv'd: $text".e(TAG)
        return text
    }

    suspend fun recvAll(): ByteArray {
        val readAllBytes = br.readText().toByteArray()
        "recv: ${readAllBytes}".e(TAG)
        return readAllBytes
    }

    @Throws(IOException::class)
    fun close() {
        if (socket.isConnected) {
            socket.close()
        }
    }

}