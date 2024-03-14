package io.vonley.mi.di.network.protocols.goldenhen

import android.os.Environment
import io.vonley.mi.di.modules.NetworkModule
import io.vonley.mi.di.network.MiServer
import io.vonley.mi.di.network.PSXService
import io.vonley.mi.di.network.impl.PSXServiceImpl
import io.vonley.mi.ui.screens.packages.domain.remote.RemotePackageInstaller
import io.vonley.mi.extensions.*
import io.vonley.mi.models.Payload
import io.vonley.mi.models.enums.Feature
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.io.BufferedReader
import java.io.OutputStream
import java.net.Socket
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class GoldhenImpl @Inject constructor(
    override val service: PSXService,
    override val rpi: RemotePackageInstaller,
    val server: MiServer
) : Goldhen {

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    init {
        rpi.start()
    }

    override val http: OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(0, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .cache(Cache(Environment.getDownloadCacheDirectory(), (20 * 1024 * 1024).toLong())).apply {
            if (NetworkModule.LOG) {
                this.addInterceptor(HttpLoggingInterceptor().also {
                    it.level = HttpLoggingInterceptor.Level.HEADERS
                }).addInterceptor(HttpLoggingInterceptor().also {
                    it.level = HttpLoggingInterceptor.Level.BODY
                })
            }
        }
        .build()


    fun longPost(url: String, body: RequestBody, headers: Headers): Response? {
        val req = Request.Builder()
            .url(url)
            .headers(headers)
            .post(body)
            .build()
        val execute = http.newCall(req)
        return try {
            execute.execute()
        } catch (e: Throwable) {
            "Something went wrong: ${e.message}".e(TAG, e)
            null
        }
    }

    override suspend fun sendPayloads(
        callback: GoldhenCallback,
        vararg payloads: Payload
    ): Map<String, Payload> {
        val pairs = payloads.map { payload ->
            delay(1000)
            val pair: Pair<String, Payload>
            val fail: suspend (msg: String) -> Pair<String, Payload> = { msg ->
                payload.status = -1
                msg.e(TAG)
                withContext(Dispatchers.Main) {
                    callback.onPayloadFailed(payload)
                }
                Pair(payload.name, payload)
            }
            val success: suspend (msg: String) -> Pair<String, Payload> = { msg ->
                msg.e(TAG)
                payload.status = 1
                withContext(Dispatchers.Main) {
                    callback.onSent(payload)
                }
                Pair(payload.name, payload)
            }
            val writing: suspend (msg: String) -> Unit = { msg ->
                msg.e(TAG)
                payload.status = 0
                withContext(Dispatchers.Main) {
                    callback.onWriting(payload)
                }
            }
            pair = when {
                payload.name.endsWith(".bin") -> {
                    try {
                        suspend fun send(socket: Socket): Pair<String, Payload> {
                            socket.getOutputStream().use { out ->
                                writing("writing payload")
                            //TODO: Fix
                            //out.write(payload.data)
                                //out.flush()
                            }
                            socket.close()
                            "Payload '${payload.name}' Sent!".i(TAG)
                            return success("success")
                        }
                        if (!socket.isConnected || socket.isOutputShutdown || socket.isClosed || socket.isInputShutdown) {
                            val createSocket = service.createSocket(service.target, Feature.GOLDENHEN)
                            if (createSocket == null) {
                                fail("something went wrong")
                            } else {
                                // we know that socket is initialized now
                                send(createSocket)
                            }
                        } else {
                            send(socket)
                        }
                    } catch (e: Throwable) {
                        fail("Failed to send payload '${payload.name}': ${e.message}")
                    }
                }
                payload.name.endsWith(".pkg") -> {
                    try {
                        if (service.target?.features?.contains(Feature.RPI) == false) {
                            return@map fail("target does not have rpi")
                        }
                        "Target has Remote Package Installer".i(TAG)
                        val urls = rpi.hostPackage(payload)
                        val toJson =
                            PSXServiceImpl.RPI(PSXServiceImpl.RPI.Type.direct, urls).toJson()
                        val contentType = "application/json".toMediaType()
                        val requestBody = toJson.toRequestBody(contentType)
                        "json: $toJson".d(TAG)
                        val res = longPost(
                            "http://${service.target?.ip}:12800/api/install",
                            requestBody,
                            Headers.headersOf()
                        )
                        val body = res?.body ?: run {
                            return@map fail("No response")
                        }
                        writing("writing payload")
                        val map = body.string().fromJson<HashMap<String, String>>() ?: run {
                            return@map fail("Unable to parse response")
                        }
                        val status = map["status"] ?: run {
                            return@map fail("status not found?")
                        }
                        if (status == "fail") {
                            return@map fail(map["error"] ?: "Failed to send payload")
                        } else {
                            return@map success("Payload sent")
                        }
                    } catch (e: Throwable) {
                        return@map fail(e.message ?: "Something went wrong")
                    }
                }
                else -> fail("something isn't right")
            }
            return@map pair
        }.toTypedArray()
        return mapOf(*pairs)
    }

    private var _br: BufferedReader? = System.`in`.bufferedReader()
    private var _pw: OutputStream? = System.out

    override val pw: OutputStream
        get() = _pw!!

    override val br: BufferedReader
        get() = br!!
}
