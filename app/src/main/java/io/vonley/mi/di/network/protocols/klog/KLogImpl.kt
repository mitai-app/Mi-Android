package io.vonley.mi.di.network.protocols.klog

import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import io.vonley.mi.di.network.PSXService
import io.vonley.mi.extensions.d
import io.vonley.mi.extensions.e
import io.vonley.mi.ui.compose.screens.consoles.data.remote.get
import io.vonley.mi.ui.compose.screens.consoles.data.remote.set
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.OutputStream
import java.io.PrintWriter
import java.net.Socket
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class KLogImpl @Inject constructor(override val service: PSXService) : KLog, KLog.KLogger {

    private val job = Job()

    override val loggers: HashMap<Class<*>, KLog.KLogger> = hashMapOf(Pair(this.javaClass, this))
    override val _socket: Socket? get() = service[service.target, feature]
    private var _br: BufferedReader? = null
    private var _pw: OutputStream? = null

    override var onGoing: Job? = null
    override fun connect() {
        if (onGoing?.isActive == true) return
        if (onGoing?.isCompleted == true || onGoing?.isCancelled == true || onGoing == null) {
            when (_socket?.isConnected) {
                true -> {
                    _br = socket.getInputStream().bufferedReader()
                    onGoing = launch {
                        try {
                            var stub: String? = null
                            while (socket.isConnected) {
                                while (recv()?.let { stub = it } != null) {
                                    withContext(Dispatchers.Main) {
                                        stub?.let {
                                            loggers.onEach { entry ->
                                                val className = entry.key.simpleName
                                                val logger = entry.value
                                                "Calling $className.onLog(log)".e(TAG)
                                                logger.onLog(parse(it))
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (e: Throwable) {
                            e.message.e(TAG)
                        }
                    }
                }
                else ->
                    launch {
                        try {
                            if (_socket == null) {
                                service[service.target] = feature
                                if (_socket != null) {
                                    withContext(Dispatchers.Main) {
                                        connect()
                                    }
                                }
                            }
                        } catch (e: Throwable) {
                            e.message.e(TAG)
                        }
                    }

            }
        }
    }

    override fun attach(logger: KLog.KLogger) {
        loggers[logger.javaClass] = logger
    }

    override fun detach(logger: KLog.KLogger) {
        loggers.remove(logger.javaClass)
    }

    override val pw: OutputStream
        get() = _pw!!
    override val br: BufferedReader
        get() = _br!!

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    override fun onLog(string: Spannable) {
        string.d(TAG)
    }

    override val TAG: String
        get() = KLogImpl::class.simpleName ?: KLogImpl::class.java.simpleName


    fun parse(string: String): Spannable {
        //Do Some Spannibles
        val peach: Long = 0xFFFF917D
        val orange: Long = 0xFFFFB12F
        val purple: Long = 0xFF6956FF
        val blue: Long = 0xFF3574ff
        val slateblue: Long = 0xFF3A4E8B
        val gold: Long = 0xFF755f19
        val red: Long = 0xFFFF3531
        val brown: Long = 0x704727ff
        val transparent: Long = 0x00000000
        val parse = hashMapOf<String, Long>(
            Pair("[GoldHEN]", gold),
            Pair("[ShellUI]", red),
            Pair("[SceShellUI]", red),
            Pair("[SceShellCore]", red),
            Pair("[SceWorkaroundCtl]", orange),
            Pair("[Theme/I]", purple),
            Pair("[NpPush]", Color.BLACK.toLong()),
            Pair("[SharedWebView]", Color.MAGENTA.toLong()),
            Pair("[SceHidConfigService]", Color.CYAN.toLong()),
            Pair("[SceNetEv]", blue),
            Pair("[SceLncService]", purple),
            Pair("[ScePatchChecker]", gold),
            Pair("[BgDailyChecker]", gold),
            Pair("[SceSystemStateMgr]", slateblue),
            Pair("[AppMgr Trace]", slateblue),
            Pair("[SceRnpsAppMgr]", slateblue),
            Pair("[sceProcessStarter]", blue),
            Pair("[Scecore App]", gold),
            Pair("[Syscore App]", gold),
            Pair("[LoginMgr]", peach),
            Pair("#LOGIN MGR#", peach),
            Pair("[LoginMgr:EventQueue]", peach),
            Pair("[LoginMgr:Queue]", peach),
            Pair("[ProfileCacheManager]", brown),
            Pair("<118>", transparent),
            Pair("118>", transparent),
            Pair("<klog>", gold),
            Pair("[SceUserServiceServer]", gold),
            Pair("[SceUserService]", gold),
            Pair("[SceLncUtil]", brown),
            Pair("[Syscore Umd]", gold),
            Pair("[AppDb]", gold),
        )

        val bold = StyleSpan(Typeface.BOLD)
        val element = SpannableString(string)

        element.setSpan(bold, 0, element.length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
        parse.keys.onEach { key ->
            if (key in element) {
                val color = parse[key] ?: Color.DKGRAY
                val fore = ForegroundColorSpan(color.toInt())
                val start = element.indexOf(key)
                val end = start + key.length
                element.setSpan(fore, start, end, SpannableString.SPAN_INCLUSIVE_INCLUSIVE)
            }
        }
        return element
    }
}