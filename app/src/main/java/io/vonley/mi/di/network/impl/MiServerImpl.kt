package io.vonley.mi.di.network.impl

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.webkit.MimeTypeMap
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import fi.iki.elonen.NanoHTTPD
import io.vonley.mi.Mi
import io.vonley.mi.Mi.MiEvent.JB_CONTINUE
import io.vonley.mi.Mi.MiEvent.JB_FAILURE
import io.vonley.mi.Mi.MiEvent.JB_INITIATED
import io.vonley.mi.Mi.MiEvent.JB_PAYLOAD
import io.vonley.mi.Mi.MiEvent.JB_PAYLOAD_REQUEST
import io.vonley.mi.Mi.MiEvent.JB_STARTED
import io.vonley.mi.Mi.MiEvent.JB_SUCCESS
import io.vonley.mi.R
import io.vonley.mi.common.coroutines.supervisedIoContext
import io.vonley.mi.di.network.JbEvent
import io.vonley.mi.di.network.JbLog
import io.vonley.mi.di.network.MiServer
import io.vonley.mi.di.network.PSXService
import io.vonley.mi.extensions.e
import io.vonley.mi.extensions.fromJson
import io.vonley.mi.models.Device
import io.vonley.mi.models.MIEnumCMD
import io.vonley.mi.models.MiCMDResponse
import io.vonley.mi.models.jbPath
import io.vonley.mi.models.supported
import io.vonley.mi.ui.MainActivity
import io.vonley.mi.utils.SharedPreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.atomic.AtomicLong
import kotlin.coroutines.CoroutineContext

class MiServerImpl(
    private val context: Context,
    private val manager: SharedPreferenceManager,
    private val service: PSXService
) : MiServer, CoroutineScope {

    override val coroutineContext: CoroutineContext = supervisedIoContext(TAG)

    private val _events = MutableSharedFlow<JbEvent>(
        extraBufferCapacity = 16,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override val events: SharedFlow<JbEvent> = _events.asSharedFlow()

    private val _device = MutableStateFlow<Device?>(null)
    override val device: StateFlow<Device?> = _device.asStateFlow()

    private val logIds = AtomicLong()
    private val _logs = MutableStateFlow<List<JbLog>>(emptyList())
    override val logs: StateFlow<List<JbLog>> = _logs.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    override val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private var server: NanoHTTPD? = null
    private var payloadJob: Job? = null

    private val miJs: String by lazy { readAsset("jb/mi.js") }
    private val miJsCached: String by lazy { readAsset("jb/mi-cached.js") }
    private val failHtml: String by lazy { readAsset("pages/fail.html") }
    private val payloads: Map<String, String> = mapOf(
        "6.72" to "payloads/goldhen/672.bin",
        "7.02" to "payloads/goldhen/702.bin",
        "7.50" to "payloads/goldhen/750.bin",
        "7.51" to "payloads/goldhen/751.bin",
        "7.55" to "payloads/goldhen/755.bin",
        "9.00" to "payloads/goldhen/900.bin"
    )

    override val activePort: Int
        get() = server?.listeningPort?.takeIf { it > 0 } ?: manager.jbPort

    override val hostAddress: String
        get() = "http://${service.localDeviceIp}:$activePort"

    private fun emit(event: JbEvent) {
        _events.tryEmit(event)
    }

    private fun log(message: String, cmd: MIEnumCMD? = null, device: Device? = null) {
        val entry = JbLog(logIds.incrementAndGet(), message, cmd, device)
        _logs.update { (it + entry).takeLast(MAX_LOGS) }
    }

    private fun createNotification(): Notification {
        val channelId = "MI"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "MI", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Mi Server"
            }
            val man = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            man.createNotificationChannel(channel)
        }
        val intent = Intent(context, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val title = "ミ (Mi) - PS4 Management Tool"
        val content = "Visit $hostAddress on your ps4!"
        val style = NotificationCompat.BigTextStyle()
            .bigText(content)
            .setBigContentTitle(title)
            .setSummaryText("Jailbreak server is running in the background")

        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.orb)
            .setStyle(style)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setAutoCancel(false)
            .build()
    }

    private fun canPostNotifications(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED

    override fun startService() {
        if (server?.isAlive == true) return
        try {
            val http = JbHttpServer(manager.jbPort)
            http.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false)
            server = http
            _isRunning.value = true
            if (canPostNotifications()) {
                NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, createNotification())
            }
        } catch (e: Throwable) {
            _isRunning.value = false
            "Unable to start jb server on ${manager.jbPort}: ${e.message}".e(TAG, e)
        }
    }

    override fun stopService() {
        try {
            server?.stop()
        } catch (e: Throwable) {
            "Unable to stop jb server: ${e.message}".e(TAG, e)
        }
        server = null
        _isRunning.value = false
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
    }

    override fun restartService() {
        launch {
            stopService()
            startService()
        }
    }

    private inner class JbHttpServer(port: Int) : NanoHTTPD(port) {

        override fun serve(session: IHTTPSession): Response {
            val uri = session.uri.toString()
            val device = parse(session)
            if (device == null) {
                log("A device that is not a PS4 opened the jailbreak page")
                return newFixedLengthResponse(Response.Status.OK, "text/html", failHtml)
            }

            _device.value = device
            manager.targetVersion = device.version
            manager.targetName = device.ip

            if (!device.supported) {
                log("Firmware ${device.version} is not supported", device = device)
                return newFixedLengthResponse(Response.Status.OK, "text/html", failHtml)
            }

            return when (uri) {
                "/" -> newFixedLengthResponse(Response.Status.OK, "text/html", page(device, "index.html"))
                "/mi.js" -> newFixedLengthResponse(
                    Response.Status.OK,
                    "text/javascript",
                    if (manager.cached) miJsCached else miJs
                )

                "/jb/cmd" -> handleCommand(session, device)
                "/gh.bin" -> {
                    val bytes = readBytes(device, uri.drop(1))
                    newFixedLengthResponse(Response.Status.OK, "*/*", ByteArrayInputStream(bytes), bytes.size.toLong())
                }

                else -> {
                    val path = uri.drop(1)
                    if (uri.contains(".manifest") && !manager.cached) {
                        return newFixedLengthResponse(Response.Status.OK, "*/*", "")
                    }
                    val body = if (path.contains("index.html")) page(device, path) else read(device, path)
                    newFixedLengthResponse(Response.Status.OK, getMimeType(uri) ?: "text/*", body)
                }
            }
        }

        private fun handleCommand(session: IHTTPSession, device: Device): Response {
            val body = HashMap<String, String>()
                .also { session.parseBody(it) }["postData"]
                ?: return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/*", "no data")
            val parsed: MiCMDResponse = body.fromJson()
                ?: return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/*", "unable to parse body")
            // The device only comes from the request headers, never the JSON body.
            val mi = parsed.copy(device = device)
            log(mi.response, mi.data.cmd, device)
            val params = arrayOf(
                "console_device" to device.device,
                "console_version" to device.version,
                "console_supported" to device.supported,
                "from" to MiServer::class.java.name
            )
            val message = mi.response
            when (mi.data.cmd) {
                MIEnumCMD.INITIATED -> {
                    Mi.log(JB_INITIATED, *params)
                }

                MIEnumCMD.CONTINUE -> {
                    Mi.log(JB_CONTINUE, *params)
                }

                MIEnumCMD.PAYLOAD_REQUEST -> {
                    Mi.log(JB_PAYLOAD_REQUEST, *params)
                }

                MIEnumCMD.STARTED -> {
                    Mi.log(JB_STARTED, *params)
                }

                MIEnumCMD.SUCCESS -> {
                    log("[Status] Enjoy!")
                    emit(JbEvent.JailbreakSucceeded(message))
                    Mi.log(JB_SUCCESS, *params)
                }

                MIEnumCMD.FAILED -> {
                    log("[JB:Failed] Somethings not right...")
                    emit(JbEvent.JailbreakFailed(message))
                    Mi.log(JB_FAILURE, *params)
                }

                MIEnumCMD.PAYLOAD -> {
                    log("[Send:Payload] Initializing Payload sender (PS4 about to get blessed.)")
                    sendPayload(device, if (device.version == "9.00") 9090 else NETCAT_PORT)
                }

                MIEnumCMD.PENDING -> {
                    log("[Send:Pending] Initializing Payload sender (PS4 about to get blessed.)")
                    sendPayload(device, if (device.version == "9.00") 9020 else NETCAT_PORT)
                }
            }
            return newFixedLengthResponse(Response.Status.OK, "text/*", "received")
        }
    }

    private fun page(device: Device, path: String): String = read(device, path)
        .replace("{{BODY}}", "Mi")
        .replace("{{TITLE}}", "Mi JB Host")

    private fun readAsset(path: String): String =
        context.assets.open(path).use { it.readBytes().decodeToString() }

    private fun read(device: Device, path: String): String = readBytes(device, path).decodeToString()

    private fun readBytes(device: Device, path: String): ByteArray {
        return try {
            context.assets.open(device.jbPath + path).use { it.readBytes() }
        } catch (e: Throwable) {
            ByteArray(0)
        }
    }

    private fun parse(session: NanoHTTPD.IHTTPSession): Device? {
        val agent = session.headers["user-agent"].toString()
        val playstation = """\(([^()]*)\)""".toRegex().findAll(agent)
            .flatMap { it.groupValues }
            .distinct()
            .firstOrNull { it.contains("playstation", true) }
            ?: return null
        val version = "([0-9]+(?:\\.[0-9]+)?)".toRegex().findAll(playstation)
            .flatMap { it.groupValues }
            .distinct()
            .lastOrNull()
            ?: return null
        return Device(playstation, version, session.headers["http-client-ip"] ?: "0.0.0.0")
    }

    private fun getMimeType(url: String): String? {
        val extension = MimeTypeMap.getFileExtensionFromUrl(url) ?: return null
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
    }

    /**
     * Pushes GoldHEN to the console once the exploit has opened its loader.
     * Tries the port the exploit asked for first, then the netcat loader the
     * app has always used, so a wrong guess for a firmware still falls back.
     */
    private fun sendPayload(device: Device, port: Int) {
        val asset = payloads[device.version] ?: run {
            "No payload bundled for ${device.version}".e(TAG)
            return
        }
        if (payloadJob?.isActive == true) return
        payloadJob = launch {
            val payload = context.assets.open(asset).use { it.readBytes() }
            log("[Payload:Wait] First we will wait 20 seconds for mira+netcat to do its thing.")
            delay(20_000)
            log("[Payload:Init] Now its time send our payload")
            val ports = listOf(port, NETCAT_PORT).distinct()
            for (attempt in 1..ATTEMPT_LIMIT) {
                for (target in ports) {
                    try {
                        log("[Payload:Connecting] attempting to connect to ${device.ip}:$target")
                        Socket().use { socket ->
                            socket.connect(InetSocketAddress(device.ip, target), 10_000)
                            val message = "[Payload:Connected] Sending GoldHen to PS4: ${device.version}"
                            log(message)
                            delay(2000)
                            socket.getOutputStream().apply {
                                write(payload)
                                flush()
                            }
                            emit(JbEvent.PayloadSent("Sending payload... please wait"))
                        }
                        Mi.log(
                            JB_PAYLOAD,
                            "console_device" to device.device,
                            "console_version" to device.version,
                            "console_supported" to device.supported,
                            "port" to target.toLong(),
                            "from" to MiServer::class.java.name
                        )
                        return@launch
                    } catch (ex: Exception) {
                        "Payload send to ${device.ip}:$target failed: ${ex.message}".e(TAG)
                    }
                }
                log("[Payload:Retrying:$attempt/$ATTEMPT_LIMIT] Attempt #$attempt: unable to connect to PS4...")
                delay(10_000)
            }
            emit(JbEvent.SendPayloadFailed(ATTEMPT_LIMIT))
        }
    }

    companion object {
        const val ATTEMPT_LIMIT = 3
        const val TAG = "MiJbServer"
        private const val NETCAT_PORT = 9021
        private const val NOTIFICATION_ID = 0
        private const val MAX_LOGS = 200
    }
}
