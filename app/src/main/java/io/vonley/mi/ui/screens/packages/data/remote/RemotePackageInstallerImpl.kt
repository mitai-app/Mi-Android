package io.vonley.mi.ui.screens.packages.data.remote

import io.vonley.mi.di.network.impl.Method
import io.vonley.mi.ui.screens.packages.data.remote.dto.RPIRequest
import io.vonley.mi.ui.screens.packages.data.remote.dto.RPIResponse
import io.vonley.mi.extensions.e
import io.vonley.mi.models.Payload
import io.vonley.mi.ui.screens.consoles.domain.remote.SyncService
import io.vonley.mi.ui.screens.packages.domain.remote.RemotePackageInstaller
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.net.ServerSocket
import java.net.Socket
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class RemotePackageInstallerImpl @Inject constructor(val service: SyncService) : RemotePackageInstaller, CoroutineScope {

    val localDeviceIp get() = service.localDeviceIp
    private val payloads = hashMapOf<String, Payload>()
    private val debug: Boolean = true
    private lateinit var server: ServerSocket
    private var port: Int = 0

    override fun start(port: Int) = launch {
        this@RemotePackageInstallerImpl.port = port
        if (!this@RemotePackageInstallerImpl::server.isInitialized) {
            server = ServerSocket(port)
            "Initializing RPI server".e(TAG)
        } else {
            "RPI Server already started".e(TAG)
            return@launch
        }
        while (!server.isClosed) {
            val client = server.accept()
            try {
                handleClient(client)
            }catch (e: Throwable) {

                "FAILED: ${e.message}".e(TAG, e)
            }
        }
    }

    private fun handleClient(client: Socket) = launch {
        val rpi = parse(client)
        val response = handleConnection(rpi)
        try {
            generate(client, rpi, response)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        //client.close()
    }

    private fun parse(client: Socket): RPIRequest {
        val sb = StringBuilder()
        val bytes = ByteArray(DEFAULT_BUFFER_SIZE) { 0 }
        val readLines = client.getInputStream().read(bytes, 0, DEFAULT_BUFFER_SIZE)
        sb.append(bytes.decodeToString().trim())
        if (debug) {
            "DATA: $sb".e(TAG)
        }
        val split = sb.toString().split("\r\n\r\n")
        val headerString = split[0]
        val contentString = if (split.size > 1) split[1] else ""
        val headersArray = headerString.split("\r\n").toMutableList()
        val request = headersArray.removeFirst()
        val headers = mapOf(*headersArray.map {
            val pairs = it.split(": ")
            return@map Pair(pairs[0].lowercase(), pairs[1])
        }.toTypedArray())
        val keepAlive = headers.containsKey("connection") && headers["connection"]!!.contains("Keep-Alive")
        client.keepAlive = keepAlive
        return when {
            request.startsWith("GET") -> {
                var path = request.replace("GET ", "")
                    .replace(" HTTP/1.1", "")
                    .replace(" HTTP/1.0", "")
                    .replace(" HTTP/2.0", "")
                val params = if (path.contains("?")) {
                    val s = path.split("?")
                    path = s[0]
                    s[1]
                } else {
                    ""
                }
                RPIRequest(Method.GET, path, headers, contentString, params)
            }

            request.startsWith("POST") -> {
                val path = request.replace("POST ", "")
                    .replace(" HTTP/1.1", "")
                    .replace(" HTTP/1.0", "")
                    .replace(" HTTP/2.0", "")
                RPIRequest(Method.POST, path, headers, contentString, "")
            }

            request.startsWith("PUT") -> {
                val path = request.replace("PUT ", "")
                    .replace(" HTTP/1.1", "")
                    .replace(" HTTP/1.0", "")
                    .replace(" HTTP/2.0", "")
                RPIRequest(Method.PUT, path, headers, contentString, "")
            }

            request.startsWith("DELETE") -> {
                val path = request.replace("DELETE ", "")
                    .replace(" HTTP/1.1", "")
                    .replace(" HTTP/1.0", "")
                    .replace(" HTTP/2.0", "")
                RPIRequest(Method.DELETE, path, headers, contentString, "")
            }

            request.startsWith("HEAD") -> {
                val path = request.replace("HEAD ", "")
                    .replace(" HTTP/1.1", "")
                    .replace(" HTTP/1.0", "")
                    .replace(" HTTP/2.0", "")
                RPIRequest(Method.HEAD, path, headers, contentString, "")
            }

            else -> {
                request.e(TAG)
                RPIRequest(Method.HEAD, request, headers, contentString, "")
            }
        }
    }

    private fun generate(client: Socket, request: RPIRequest, response: RPIResponse) {
        fun generateHeader(filename: String, start: Int, end: Int, totalBytes: Int): ByteArray {
            val headers = arrayOf(
                "HTTP/1.1 206 Partial Content",
                "X-Powered-By: Mi",
                "Access-Control-Allow-Origin: *",
                "Access-Control-Allow-Methods: GET, POST, OPTIONS, PUT, PATCH, DELETE",
                "Content-Disposition: attachment; filename=\"${filename.replace("/pkg/","")}\"",
                "Accept-Ranges: bytes",
                "Cache-Control: public, max-age=0",
                "Last-Modified: Fri, 14 Oct 2022 16:52:07 GMT",
                "ETag: W/\"2140000-183d769035f\"",
                "Content-Type: application/octet-stream",
                "Content-Range: bytes $start-$end/$totalBytes",
                "Content-Length: ${end - start}",
                "Date: Fri, 14 Oct 2022 17:36:56 GMT",
                "Connection: keep-alive"
            ).joinToString("\r\n") + "\r\n\r\n"
            headers.e(TAG)
            return headers.toByteArray()
        }

        if (request.headers.containsKey("range")) {
            val range = request.headers["range"]!!
            val bytes = range.split("=")[1]
            val split = bytes.split("-")
            val from: Long = split[0].toLong()
            val to: Long = if (split.size > 1) {
                try {
                    split[1].toLong() + 1
                } catch (e: Throwable){
                    response.content!!.size
                }
            } else {
                response.content!!.size
            }

            try {
                val header = generateHeader(
                    request.path,
                    from.toInt(),
                    to.toInt(),
                    response.content!!.size.toInt()
                )
                client.getOutputStream().write(header)
                response.content.stream.mark(from.toInt())
                response.content.stream.reset()
                (from.toInt()..to.toInt()).forEach { position ->
                    val read = response.content.stream.read()
                    client.getOutputStream().write(read)
                }
                //val payload = response.content.copyOfRange(from, to)
                //response.content.stream.copyTo(client.getOutputStream(), response.content.size.toInt())
                client.getOutputStream().flush()
            }catch (E: Throwable) {
                E.printStackTrace()
            }
            return
        }

        try {
            val headers =
                "HTTP/1.1 $response.status OK\r\nContent-Length: ${response.content?.size ?: 0}\r\n\r\n".toByteArray()
            client.getOutputStream().write(headers)
            response.content?.let {
                client.getOutputStream().write(it.stream.readBytes())
                //it.stream.copyTo(client.getOutputStream(), it.size.toInt())
            }
            client.getOutputStream().flush()
        }catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    private fun handleConnection(rpi: RPIRequest): RPIResponse {
        if (debug) {
            "PATH: ${rpi.path}?${rpi.param}".e(TAG)
        }
        return when (rpi.method) {
            Method.HEAD -> handleHeadRequest(rpi)
            Method.GET -> handleGetRequest(rpi)
            Method.POST -> handlePostRequest(rpi)
            Method.PUT -> handlePutRequest(rpi)
            Method.DELETE -> handleDeleteRequest(rpi)
        }
    }

    private fun handleGetRequest(rpi: RPIRequest): RPIResponse {
        return when (rpi.path) {
            in payloads.keys -> {
                val payload = payloads[rpi.path]!!
                RPIResponse(200, content = payload)
            }
            else -> emptyResponse(rpi)
        }
    }
    private fun handleHeadRequest(rpi: RPIRequest): RPIResponse = emptyResponse(rpi)
    private fun handlePostRequest(rpi: RPIRequest): RPIResponse = emptyResponse(rpi)
    private fun handlePutRequest(rpi: RPIRequest): RPIResponse = emptyResponse(rpi)
    private fun handleDeleteRequest(rpi: RPIRequest): RPIResponse = emptyResponse(rpi)
    private fun emptyResponse(rpi: RPIRequest): RPIResponse {
        val content = "HTTP/1.1 200 OK\r\nContent-Length: 0\r\nContent-Type: text/html\r\n\r\n"
        return RPIResponse(200, content = Payload("", content.toByteArray().inputStream(), size=content.length.toLong()))
    }
    override fun hostPackage(vararg payloads: Payload): Array<String> {
        val urls = payloads.map { payload ->
            val path = "/pkg/${payload.name}"
            this.payloads[path] = payload
            "http://${service.localDeviceIp}:${port}$path"
        }
        return urls.toTypedArray()
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    private val job = Job()

    companion object {
        const val TAG = "RemotePackageInstaller"
    }

}