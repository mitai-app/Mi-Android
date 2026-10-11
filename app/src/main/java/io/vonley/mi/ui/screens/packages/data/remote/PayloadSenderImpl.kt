package io.vonley.mi.ui.screens.packages.data.remote

import io.vonley.mi.di.network.PSXService
import io.vonley.mi.extensions.e
import io.vonley.mi.extensions.fromJson
import io.vonley.mi.extensions.i
import io.vonley.mi.extensions.toJson
import io.vonley.mi.models.Payload
import io.vonley.mi.models.enums.Feature
import io.vonley.mi.ui.screens.packages.domain.remote.PayloadProgress
import io.vonley.mi.ui.screens.packages.domain.remote.PayloadSender
import io.vonley.mi.ui.screens.packages.domain.remote.RemotePackageInstaller
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class PayloadSenderImpl @Inject constructor(
    private val service: PSXService,
    private val rpi: RemotePackageInstaller,
) : PayloadSender {

    /** Package installs can take minutes before the console answers. */
    private val installHttp by lazy {
        service.http.newBuilder().readTimeout(0, TimeUnit.SECONDS).build()
    }

    private data class InstallRequest(val type: String, val packages: List<String>)

    override fun send(payloads: List<Payload>): Flow<PayloadProgress> = flow {
        val ip = service.targetIp
        if (ip == null) {
            payloads.forEach { emit(PayloadProgress.Failed(it, "No console selected")) }
            return@flow
        }
        payloads.forEach { payload ->
            when {
                payload.isBin -> sendBin(ip, payload)
                payload.isPkg -> installPkg(ip, payload)
                else -> emit(PayloadProgress.Failed(payload, "Only .bin and .pkg files can be sent"))
            }
        }
    }.flowOn(Dispatchers.IO)

    private suspend fun FlowCollector<PayloadProgress>.sendBin(ip: String, payload: Payload) {
        val port = service.manager.featurePort.ports.first()
        try {
            emit(PayloadProgress.Writing(payload))
            Socket().use { socket ->
                socket.connect(InetSocketAddress(ip, port), CONNECT_TIMEOUT_MS)
                socket.getOutputStream().use { out ->
                    payload.file.inputStream().use { it.copyTo(out) }
                    out.flush()
                }
            }
            "Payload '${payload.name}' sent to $ip:$port".i(TAG)
            emit(PayloadProgress.Sent(payload))
            // GoldHEN's loader needs a moment before it accepts the next connection.
            delay(BIN_COOLDOWN_MS)
        } catch (e: Throwable) {
            "Failed to send payload '${payload.name}': ${e.message}".e(TAG, e)
            emit(PayloadProgress.Failed(payload, e.message ?: "Unable to reach $ip:$port"))
        }
    }

    private suspend fun FlowCollector<PayloadProgress>.installPkg(ip: String, payload: Payload) {
        if (service.target?.features?.contains(Feature.RPI) != true) {
            emit(PayloadProgress.Failed(payload, "Console does not expose the Remote Package Installer"))
            return
        }
        try {
            emit(PayloadProgress.Writing(payload))
            val url = rpi.hostPackage(payload)
            val body = InstallRequest("direct", listOf(url)).toJson()
                .toRequestBody("application/json".toMediaType())
            val request = Request.Builder().url("http://$ip:${Feature.RPI.ports.first()}/api/install").post(body).build()
            val response = installHttp.newCall(request).execute().use { it.body.string() }
            val result = response.fromJson<Map<String, Any?>>()
            val status = result?.get("status")
            if (status != null && status != "fail") {
                emit(PayloadProgress.Sent(payload))
            } else {
                val reason = result?.get("error")?.toString() ?: "Install was rejected"
                emit(PayloadProgress.Failed(payload, reason))
            }
        } catch (e: Throwable) {
            "Failed to install '${payload.name}': ${e.message}".e(TAG, e)
            emit(PayloadProgress.Failed(payload, e.message ?: "Install failed"))
        }
    }

    private companion object {
        const val TAG = "PayloadSender"
        const val CONNECT_TIMEOUT_MS = 3000
        const val BIN_COOLDOWN_MS = 3000L
    }
}
