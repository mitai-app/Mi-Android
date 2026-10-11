package io.vonley.mi.ui.screens.packages.data.remote

import io.vonley.mi.common.coroutines.supervisedIoContext
import io.vonley.mi.di.network.impl.Method
import io.vonley.mi.extensions.e
import io.vonley.mi.models.Payload
import io.vonley.mi.ui.screens.consoles.domain.remote.SyncService
import io.vonley.mi.ui.screens.packages.data.remote.dto.RPIRequest
import io.vonley.mi.ui.screens.packages.domain.remote.RemotePackageInstaller
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.BufferedInputStream
import java.io.InputStream
import java.io.OutputStream
import java.io.RandomAccessFile
import java.net.ServerSocket
import java.net.Socket
import java.net.URLEncoder
import java.util.concurrent.ConcurrentHashMap

/**
 * Minimal HTTP/1.1 file server for the console's package installer, which
 * downloads large packages using byte-range requests.
 */
class RemotePackageInstallerImpl(
    private val service: SyncService,
    private val port: Int = DEFAULT_PORT
) : RemotePackageInstaller {

    private val scope = CoroutineScope(supervisedIoContext(TAG))
    private val payloads = ConcurrentHashMap<String, Payload>()
    private var server: ServerSocket? = null

    @Synchronized
    private fun ensureStarted() {
        if (server?.isClosed == false) return
        val socket = ServerSocket(port)
        server = socket
        scope.launch {
            while (isActive && !socket.isClosed) {
                val client = try {
                    socket.accept()
                } catch (e: Throwable) {
                    "RPI server stopped: ${e.message}".e(TAG)
                    break
                }
                launch { handleClient(client) }
            }
        }
    }

    override fun hostPackage(payload: Payload): String {
        ensureStarted()
        val path = "/pkg/" + URLEncoder.encode(payload.name, "UTF-8").replace("+", "%20")
        payloads[path] = payload
        return "http://${service.localDeviceIp}:$port$path"
    }

    private fun handleClient(client: Socket) {
        client.use { socket ->
            try {
                val input = BufferedInputStream(socket.getInputStream())
                val request = RPIRequest.parse(readHead(input)) ?: return
                val out = socket.getOutputStream()
                val payload = payloads[request.path]
                if (payload == null || request.method !in listOf(Method.GET, Method.HEAD)) {
                    writeHead(out, "404 Not Found", listOf("Content-Length: 0"))
                    return
                }
                serve(out, request, payload)
            } catch (e: Throwable) {
                "RPI request failed: ${e.message}".e(TAG, e)
            }
        }
    }

    private fun serve(out: OutputStream, request: RPIRequest, payload: Payload) {
        val size = payload.size
        val range = RPIRequest.parseRange(request.headers["range"], size)
        val first = range?.first ?: 0L
        val length = range?.let { it.last - it.first + 1 } ?: size
        val headers = mutableListOf(
            "Content-Type: application/octet-stream",
            "Content-Disposition: attachment; filename=\"${payload.name}\"",
            "Accept-Ranges: bytes",
            "Content-Length: $length",
        )
        if (range != null) headers += "Content-Range: bytes ${range.first}-${range.last}/$size"
        writeHead(out, if (range != null) "206 Partial Content" else "200 OK", headers)
        if (request.method == Method.HEAD) return

        RandomAccessFile(payload.file, "r").use { file ->
            file.seek(first)
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE * 8)
            var remaining = length
            while (remaining > 0) {
                val read = file.read(buffer, 0, minOf(buffer.size.toLong(), remaining).toInt())
                if (read < 0) break
                out.write(buffer, 0, read)
                remaining -= read
            }
        }
        out.flush()
    }

    private fun writeHead(out: OutputStream, status: String, headers: List<String>) {
        val head = buildString {
            append("HTTP/1.1 ").append(status).append("\r\n")
            append("Server: Mi\r\n")
            append("Connection: close\r\n")
            headers.forEach { append(it).append("\r\n") }
            append("\r\n")
        }
        out.write(head.toByteArray())
    }

    /** Reads the request line and headers, stopping at the blank line. */
    private fun readHead(input: InputStream): List<String> {
        val lines = mutableListOf<String>()
        val line = StringBuilder()
        while (lines.size < MAX_HEADER_LINES) {
            val b = input.read()
            if (b < 0) break
            if (b == '\n'.code) {
                val text = line.toString().trimEnd('\r')
                if (text.isEmpty()) break
                lines += text
                line.clear()
            } else {
                line.append(b.toChar())
            }
        }
        return lines
    }

    companion object {
        const val TAG = "RemotePackageInstaller"
        const val DEFAULT_PORT = 12800
        private const val MAX_HEADER_LINES = 100
    }
}
