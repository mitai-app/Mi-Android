package io.vonley.mi.di.network.protocols.klog

import android.text.Spannable
import io.vonley.mi.di.network.protocols.common.PSXProtocol
import io.vonley.mi.models.enums.Feature
import kotlinx.coroutines.Job
import java.net.Socket

interface KLog : PSXProtocol {

    override val feature: Feature get() = Feature.KLOG
    val _socket: Socket?
    override val socket: Socket get() = _socket!!

    override val TAG: String
        get() = KLog::class.qualifiedName ?: KLog::javaClass.name

    interface KLogger {
        fun onLog(string: Spannable)
    }

    val loggers: HashMap<Class<*>, KLogger>

    var onGoing: Job?

    fun connect()

    fun attach(logger: KLogger)

    fun detach(logger: KLogger)
}