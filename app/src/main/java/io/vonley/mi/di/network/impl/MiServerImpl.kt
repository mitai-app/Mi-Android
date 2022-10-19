package io.vonley.mi.di.network.impl

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import fi.iki.elonen.NanoHTTPD
import io.vonley.mi.BuildConfig
import io.vonley.mi.Mi
import io.vonley.mi.Mi.MiEvent.*
import io.vonley.mi.R
import io.vonley.mi.di.annotations.SharedPreferenceStorage
import io.vonley.mi.di.network.MiServer
import io.vonley.mi.di.network.PSXService
import io.vonley.mi.extensions.e
import io.vonley.mi.extensions.fromJson
import io.vonley.mi.models.*
import io.vonley.mi.ui.main.MainActivity
import io.vonley.mi.utils.SharedPreferenceManager
import kotlinx.coroutines.*
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import kotlin.coroutines.CoroutineContext

class MiServerImpl constructor(
    @ApplicationContext val context: Context,
    @SharedPreferenceStorage val manager: SharedPreferenceManager,
    override val service: PSXService
) : MiServer, CoroutineScope {

    interface MiJbServerListener {
        fun onDeviceConnected(device: Device)
        fun onLog(string: String)
        fun onJailbreakSucceeded(message: String)
        fun onJailbreakFailed(message: String)
        fun onPayloadSent(msg: String? = null)
        fun onUnsupported(s: String)
        fun onCommand(mi: MiResponse<MiResponse.Cmd>)
        fun onSendPayloadAttempt(attempt: Int)
        fun onSendPkgSuccess(payload: Payload) {

        }

        fun onSendPkgFail(payload: Payload) {

        }
    }

    override var server: NanoHTTPD? = null
    private val callbacks: HashMap<Class<*>, MiJbServerListener> = hashMapOf()
    private val callback: MiJbServerListener = object : MiJbServerListener {

        override fun onSendPkgFail(payload: Payload) {
            val name = "onSendPkgFail"
            launch {
                withContext(Dispatchers.Main) {
                    callbacks.onEach { a ->
                        if (BuildConfig.DEBUG) {
                            Log.e(TAG, "Calling ${a.key.name}::${name}(string) / ${payload.name}")
                        }
                        a.value.onSendPkgFail(payload)
                    }
                }
            }
        }

        override fun onSendPkgSuccess(payload: Payload) {
            val name = "onSendPkgSuccess"
            launch {
                withContext(Dispatchers.Main) {
                    callbacks.onEach { a ->
                        if (BuildConfig.DEBUG) {
                            Log.e(TAG, "Calling ${a.key.name}::${name}(string) / ${payload.name}")
                        }
                        a.value.onSendPkgSuccess(payload)
                    }
                }
            }
        }

        override fun onDeviceConnected(device: Device) {
            val name = "onDeviceConnected"
            launch {
                withContext(Dispatchers.Main) {
                    callbacks.onEach { a ->
                        if (BuildConfig.DEBUG) {
                            Log.e(TAG, "Calling ${a.key.name}::${name}(string) / ${device.device}")
                        }
                        a.value.onDeviceConnected(device)
                    }
                }
            }
        }

        override fun onLog(string: String) {
            val name = "onLog"
            launch {
                withContext(Dispatchers.Main) {
                    callbacks.onEach { a ->
                        if (BuildConfig.DEBUG) {
                            Log.e(TAG, "Calling ${a.key.name}::${name}(string) / $string")
                        }
                        a.value.onLog(string)
                    }
                }
            }
        }

        override fun onJailbreakSucceeded(message: String) {
            val name = "onJailbreakSucceeded"
            launch {
                withContext(Dispatchers.Main) {
                    callbacks.onEach { a ->
                        if (BuildConfig.DEBUG) {
                            Log.e(TAG, "Calling ${a.key.name}::${name}(message)")
                        }
                        a.value.onJailbreakSucceeded(message)
                    }
                }
            }
        }

        override fun onJailbreakFailed(message: String) {
            val name = "onJailbreakFailed"
            launch {
                withContext(Dispatchers.Main) {
                    callbacks.onEach { a ->
                        if (BuildConfig.DEBUG) {
                            Log.e(TAG, "Calling ${a.key.name}::${name}(message)")
                        }
                        a.value.onJailbreakFailed(message)
                    }
                }
            }
        }

        override fun onPayloadSent(msg: String?) {
            val name = "onPayloadSent"
            launch {
                withContext(Dispatchers.Main) {
                    callbacks.onEach { a ->
                        if (BuildConfig.DEBUG) {
                            Log.e(TAG, "Calling ${a.key.name}::${name}($msg)")
                        }
                        a.value.onPayloadSent(msg)
                    }
                }
            }
        }

        override fun onUnsupported(s: String) {
            val name = "onUnsupported"
            launch {
                withContext(Dispatchers.Main) {
                    callbacks.onEach { a ->
                        if (BuildConfig.DEBUG) {
                            Log.e(TAG, "Calling ${a.key.name}::${name}(string)")
                        }
                        a.value.onUnsupported(s)
                    }
                }
            }
        }

        override fun onCommand(mi: MiResponse<MiResponse.Cmd>) {
            val name = "onCommand"
            launch {
                withContext(Dispatchers.Main) {
                    callbacks.onEach { a ->
                        if (BuildConfig.DEBUG) {
                            Log.e(TAG, "Calling ${a.key.name}::${name}(Mi<Mi.Cmd>())")
                        }
                        a.value.onCommand(mi)
                    }
                }
            }
        }

        override fun onSendPayloadAttempt(attempt: Int) {
            val name = "onSendPayloadAttempt"
            launch {
                withContext(Dispatchers.Main) {
                    callbacks.onEach { a ->
                        if (BuildConfig.DEBUG) {
                            Log.e(TAG, "Calling ${a.key.name}::${name}($attempt)")
                        }
                        a.value.onSendPayloadAttempt(attempt)
                    }
                }
            }
        }
    }
    private val miJs: ByteArray = context.assets.open("jb/mi.js").readBytes()
    private val miJsCached: ByteArray = context.assets.open("jb/mi-cached.js").readBytes()
    private val failHtml: ByteArray = context.assets.open("pages/fail.html").readBytes()
    private val routes: HashMap<String, ByteArray> = hashMapOf()
    private val payloads: HashMap<String, ByteArray> by lazy {
        return@lazy hashMapOf(
            Pair("6.72", context.assets.open("payloads/goldhen/672.bin").readBytes()),
            Pair("7.02", context.assets.open("payloads/goldhen/702.bin").readBytes()),
            Pair("7.50", context.assets.open("payloads/goldhen/750.bin").readBytes()),
            Pair("7.51", context.assets.open("payloads/goldhen/751.bin").readBytes()),
            Pair("7.55", context.assets.open("payloads/goldhen/755.bin").readBytes()),
            Pair("9.00", context.assets.open("payloads/goldhen/900.bin").readBytes())
        )
    }

    private val root: String get() = console!!.jbPath
    private var ready: Boolean = false
    private var console: Device? = null
    private var attempts = 3

    fun create(
        title: String = "ミ (Mi) - PS4 Management Tool",
        content: String = "Visit http://${service.localDeviceIp}:${activePort} on your ps4!",
        summary: String = "Jailbreak server is running in the background"
    ): Notification {
        val channel_id = "MI"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "MI"
            val desc = "Mi Server"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channel_id, name, importance).apply {
                description = desc
            }
            val man = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            man.createNotificationChannel(channel)
        }
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val texts = "Visit http://${service.localDeviceIp}:${activePort} on your ps4!"
        val titles = "ミ (Mi)"
        val summaries = "Jailbreak server is running in the background"

        val style = NotificationCompat.BigTextStyle()
            .bigText(content)
            .setBigContentTitle(title)
            .setSummaryText(summary)

        val server = NotificationCompat.Builder(context, channel_id)
            .setSmallIcon(R.mipmap.orb)
            .setStyle(style)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setAutoCancel(false)
            .build()
        return server
    }

    private fun init(): NanoHTTPD {
        return object : NanoHTTPD(manager.jbPort), CoroutineScope {

            val job = Job()

            var jobThread: Job? = null

            override fun start() {
                jobThread = launch {
                    try {
                        ready = true
                        super.start()
                        delay(3000)
                        withContext(Dispatchers.Main) {
                            NotificationManagerCompat.from(context).notify(0, create())
                        }
                    } catch (e: Throwable) {
                        ready = false
                        if (BuildConfig.DEBUG) {
                            Log.e(TAG, "failed to start service ${e.message}")
                        }
                    }
                }
            }

            override fun stop() {
                launch {
                    try {
                        jobThread?.cancelAndJoin()
                    } catch (t: Throwable) {
                        if (BuildConfig.DEBUG) {
                            Log.e(TAG, "jobThread.cancelAndJoin() failed ${t.message}")
                        }
                    }
                    try {
                        super.stop()
                        ready = false
                    } catch (e: Throwable) {
                        if (BuildConfig.DEBUG) {
                            Log.e(TAG, "failed to stop service ${e.message}")
                        }
                    }

                    delay(3000)
                    withContext(Dispatchers.Main) {
                        NotificationManagerCompat.from(context).cancel(0)
                    }
                }
            }

            override fun serve(s: IHTTPSession?): Response {
                s?.let { session ->
                    val uri = session.uri.toString()
                    val console = parse(session)
                    fun installPayload(): Response {
                        val bytes = payloads[uri]!!
                        "PAYLOAD DOES CONTAIN URI: $uri, size: ${bytes.size}".e("BOO YEAH")
                        return newChunkedResponse(
                            Response.Status.OK,
                            "application/octet-stream",
                            ByteArrayInputStream(bytes)
                        )
                    }
                    console?.let {
                        manager.ftpPass
                        callback.onDeviceConnected(it)
                        manager.targetVersion = it.version
                        manager.targetName = it.ip
                        val mime = getMimeType(uri) ?: "text/*"
                        when {
                            it.supported -> {
                                if (this@MiServerImpl.console?.ip != it.ip) this@MiServerImpl.console = it
                                when (uri) {
                                    "/" -> {
                                        val read = read("index.html")
                                            .replace("{{BODY}}", "Mi")
                                            .replace("{{TITLE}}", "Mi JB Host")
                                        return newFixedLengthResponse(
                                            Response.Status.OK,
                                            "text/html",
                                            read
                                        )
                                    }
                                    "/mi.js" -> {
                                        val mi = if (manager.cached) {
                                            miJsCached.decodeToString()
                                        } else {
                                            miJs.decodeToString()
                                        }
                                        return newFixedLengthResponse(
                                            Response.Status.OK,
                                            "text/javascript",
                                            mi
                                        )
                                    }
                                    "/jb/cmd" -> {
                                        Log.e(TAG, "Method: ${session.method.name}")
                                        val map = HashMap<String, String>()
                                        session.parseBody(map)
                                        val body = map["postData"] ?: run {
                                            "NO DATA".e(TAG)
                                            return newFixedLengthResponse(
                                                Response.Status.INTERNAL_ERROR,
                                                "text/*",
                                                "no data"
                                            )
                                        }
                                        "DATA: $body".e(TAG)
                                        val mi: MiResponse<MiResponse.Cmd> = body.fromJson() ?: run {
                                            "COULD NOT PARSE".e(TAG)
                                            return newFixedLengthResponse(
                                                Response.Status.INTERNAL_ERROR,
                                                "text/*",
                                                "unable to parse body"
                                            )
                                        }
                                        callback.onCommand(mi)
                                        val cmd = mi.data.cmd
                                        val message = mi.response
                                        when (cmd) {
                                            Minum.initiated -> {
                                                callback.onLog("[JB:Initiated] $message")
                                                val params = arrayOf(
                                                    Pair("console_device", it.device),
                                                    Pair("console_version", it.version),
                                                    Pair("console_supported",it.supported),
                                                    Pair("from", MiServer::class.java.name)
                                                )
                                                Mi.log(JB_INITIATED, *params)}
                                            Minum.continuee -> {
                                                callback.onLog("[JB:Continue] $message")
                                                val params = arrayOf(
                                                    Pair("console_device", it.device),
                                                    Pair("console_version", it.version),
                                                    Pair("console_supported",it.supported),
                                                    Pair("from", MiServer::class.java.name)
                                                )
                                                Mi.log(JB_CONTINUE, *params)}
                                            Minum.payloadReq -> {
                                                callback.onLog("[JB:Payload:Request] $message")
                                                val params = arrayOf(
                                                    Pair("console_device", it.device),
                                                    Pair("console_version", it.version),
                                                    Pair("console_supported",it.supported),
                                                    Pair("from", MiServer::class.java.name)
                                                )
                                                Mi.log(JB_PAYLOAD_REQUEST, *params)
                                            }
                                            Minum.started -> {
                                                callback.onLog("[JB:Started] $message")
                                                val params = arrayOf(
                                                    Pair("console_device", it.device),
                                                    Pair("console_version", it.version),
                                                    Pair("console_supported",it.supported),
                                                    Pair("from", MiServer::class.java.name)
                                                )
                                                Mi.log(JB_STARTED, *params)
                                            }
                                            Minum.success -> {
                                                callback.onLog("[Status] Enjoy!")
                                                callback.onJailbreakSucceeded(message)
                                                val params = arrayOf(
                                                    Pair("console_device", it.device),
                                                    Pair("console_version", it.version),
                                                    Pair("console_supported",it.supported),
                                                    Pair("from", MiServer::class.java.name)
                                                )
                                                Mi.log(JB_SUCCESS, *params)
                                            }
                                            Minum.failed -> {
                                                callback.onLog("[JB:Failed] Somethings not right...")
                                                callback.onJailbreakFailed(message)
                                                val params = arrayOf(
                                                    Pair("console_device", it.device),
                                                    Pair("console_version", it.version),
                                                    Pair("console_supported",it.supported),
                                                    Pair("from", MiServer::class.java.name)
                                                )
                                                Mi.log(JB_FAILURE, *params)
                                            }
                                            Minum.payload -> {
                                                callback.onLog("[Send:Payload] Initializing Payload sender (PS4 about to get blessed.)")
                                                sendPayload(it, if(console.version == "9.00") 9090 else 9021)
                                            }
                                            Minum.pending -> {
                                                callback.onLog("[Send:Pending] Initializing Payload sender (PS4 about to get blessed.)")
                                                sendPayload(it, if(console.version == "9.00") 9020 else 9021)
                                            }
                                            else -> return newFixedLengthResponse(
                                                Response.Status.NOT_FOUND,
                                                "text/*",
                                                "invalid cmd"
                                            )
                                        }
                                        return newFixedLengthResponse(
                                            Response.Status.OK,
                                            "text/*",
                                            "received"
                                        )
                                    }
                                    "/gh.bin" -> {
                                        val path = uri.drop(1)
                                        val readBytes = readBytes(path)
                                        val byteArrayInputStream = ByteArrayInputStream(readBytes)
                                        return newFixedLengthResponse(
                                            Response.Status.OK,
                                            "*/*",
                                            byteArrayInputStream,
                                            readBytes.size.toLong()
                                        )
                                    }
                                    else -> {
                                        val path = uri.drop(1)
                                        if (uri.contains(".manifest")) {
                                            if (!manager.cached) {
                                                return newFixedLengthResponse(
                                                    Response.Status.OK,
                                                    "*/*",
                                                    ""
                                                )
                                            }
                                        }
                                        Log.e(TAG, "URI: $uri")

                                        var read = read(path)
                                        if (path.contains("index.html")) {
                                            read = read
                                                .replace("{{BODY}}", "Mi")
                                                .replace("{{TITLE}}", "Mi JB Host")
                                        }
                                        return newFixedLengthResponse(
                                            Response.Status.OK,
                                            mime,
                                            read
                                        )
                                    }
                                }
                            }
                            payloads.containsKey(uri) -> {
                                return installPayload()
                            }
                            else -> {
                                return newFixedLengthResponse(
                                    Response.Status.OK,
                                    "text/html",
                                    failHtml.decodeToString()
                                )
                            }
                        }
                    } ?: run {

                        if (payloads.containsKey(uri)) {
                            return installPayload()
                        }
                        callback.onUnsupported("This device is not supported!")
                        return newFixedLengthResponse(
                            Response.Status.OK, "text/html", failHtml.decodeToString()
                        )
                    }
                }
                return super.serve(s)
            }

            override val coroutineContext: CoroutineContext
                get() = Dispatchers.IO + job
        }
    }

    fun extract(open: InputStream) {
        ZipInputStream(open).use { zip ->
            with(zip) {
                var stub: ZipEntry? = null
                while (nextEntry.also { stub = it } != null) {
                    val ze: ZipEntry = stub!!
                    Log.d(TAG, "Decompressing + ${ze.name}")
                    if (ze.isDirectory) {
                        createDir(ze.name)
                    } else {
                        val name = ze.name
                        if (name.contains("__MACOSX")) continue
                        //val file = File(path, name)
                        Log.e(TAG, "Extracting: $name")
                        try {
                            routes[name] = readBytes()
                            closeEntry()
                            /*
                            file.outputStream().use { fos ->
                                with(fos) {
                                    write(readBytes())
                                }
                                closeEntry()
                            }*/
                        } catch (e: Throwable) {
                            Log.e(TAG, e.message ?: "Unable to extract")
                            closeEntry()
                        }
                    }
                }
            }
        }
    }

    override fun add(jb: MiJbServerListener) {
        callbacks[jb.javaClass] = jb
    }

    override fun remove(jb: MiJbServerListener) {
        if (callbacks.containsKey(jb.javaClass)) {
            callbacks.remove(jb.javaClass)
        }
    }

    override fun hostPackage(vararg payloads: Payload): Array<String> {
        val urls = payloads.map { payload ->
            val path = "/pkg/${payload.name}"
            //this.payloads[path] = payload
            //TODO: Fix
            "http://${service.localDeviceIp}:${manager.jbPort}$path"
        }
        return urls.toTypedArray()
    }

    override fun startService() {
        try {
            if (server == null) {
                server = init()
            }
            if (server?.wasStarted() == false) {
                server?.start()
            }
        } catch (e: Throwable) {
            Log.e(TAG, e.message ?: "Hmmm unable to start service")
        }
    }

    override val activePort: Int
        get() = server?.listeningPort ?: manager.jbPort

    override fun stopService() {
        try {
            if (server?.wasStarted() == true) {
                server?.stop()
            }
            server = null
        } catch (e: Throwable) {
            Log.e(TAG, e.message ?: "Hmmm idk")
        }
    }

    private fun read(path: String): String {
        return readBytes(path).decodeToString()
    }

    private fun readBytes(path: String): ByteArray {
        return try {
            val s = root + path
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "Fetching: $s")
            }
            context.assets.open(s).readBytes()
        } catch (e: Throwable) {
            ByteArray(0)
        }
    }

    private fun createDir(name: String?) {
        val file = File(name)
        if (!file.isDirectory) {
            file.mkdirs()
        }
    }

    private fun parse(session: NanoHTTPD.IHTTPSession): Device? {
        val string = session.headers["user-agent"].toString()
        val extractPlaystation = """\(([^()]*)\)""".toRegex()
        val extractVersion = "([0-9]+(?:\\.[0-9]+)?)".toRegex()
        val matchEntire = extractPlaystation.findAll(string).flatMap { it.groupValues }.distinct()
            .filter { it.contains("playstation", true) }.toList().firstOrNull();
        if (matchEntire != null) {
            println(matchEntire)
            val extracted =
                extractVersion.findAll(matchEntire).flatMap { it.groupValues }.distinct().toList()
            val version = extracted.last()
            return Device(matchEntire, version, session.headers["http-client-ip"] ?: "0.0.0.0")
        }
        return null
    }

    private fun getMimeType(url: String): String? {
        var type: String? = null
        val extension = MimeTypeMap.getFileExtensionFromUrl(url)
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        }
        return type;
    }

    private fun sendPayload(device: Device, port: Int = 9021) {
        payloads[device.version]?.let { payload ->
            Log.e(TAG, "Payload Size: ${payload.size}")
            if (payload.isEmpty()) return@let
            val block: suspend CoroutineScope.() -> Unit = {
                callback.onLog("[Payload:Wait] First we will wait 20 seconds for mira+netcat to do its thing.")
                delay(20000)
                callback.onLog("[Payload:Init] Now its time send our payload")
                attempts = 0
                while (attempts < ATTEMPT_LIMIT) {
                    val check = InetAddress.getByName(device.ip).isReachable(3000) ?: break
                    try {
                        callback.onLog("[Payload:Connecting] attempting to connect to ${device.ip}:9021")

                        val outSock = Socket()
                        val inetSocketAddress = InetSocketAddress(device.ip, 9021)
                        outSock.connect(inetSocketAddress, 10000)
                        val outputStream = outSock.getOutputStream()
                        val string = "[Payload:Connected] Sending GoldHen 2.1 to PS4: ${device.version}"
                        callback.onLog(string)
                        delay(2000)
                        outputStream.write(payload)
                        outputStream.flush()
                        outputStream.close()
                        callback.onPayloadSent(string)
                        outSock.close()
                        val params = arrayOf(
                            Pair("console_device", device.device),
                            Pair("console_version", device.version),
                            Pair("console_supported",device.supported),
                            Pair("from", MiServer::class.java.name)
                        )
                        Mi.log(JB_PAYLOAD, *params)
                        break
                    } catch (ex: Exception) {
                        attempts++
                        callback.onLog("[Payload:Retrying:$attempts/3] Attempt #$attempts: unable to connect to PS4...")
                        callback.onSendPayloadAttempt(attempts)
                        delay(10000)
                    }
                }
                attempts = 0
            }
            if (!this::port9021.isInitialized) {
                this.port9021 = launch(block = block)
            } else if ((!this.port9021.isActive || this.port9021.isCompleted) && !this.port9021.start()) {
                this.port9021 = launch(block = block)
            }
        } ?: run {
            "So the payloads aren't event loaded wtf".e(TAG)
        }
    }

    private val job = Job()
    private lateinit var port9021: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    companion object {
        const val ATTEMPT_LIMIT = 3
        const val TAG = "MiJbServer"
    }

}