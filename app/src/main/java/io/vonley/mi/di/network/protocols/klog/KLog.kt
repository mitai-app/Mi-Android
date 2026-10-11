package io.vonley.mi.di.network.protocols.klog

import io.vonley.mi.di.network.protocols.common.SocketProtocol
import io.vonley.mi.models.enums.Feature
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/** GoldHEN's kernel log stream (port 3232). */
interface KLog : SocketProtocol {

    override val feature: Feature get() = Feature.KLOG

    override val TAG: String
        get() = KLog::class.qualifiedName ?: KLog::javaClass.name

    /** Raw log lines; recent lines are replayed to new collectors. */
    val logs: SharedFlow<String>

    val isStreaming: StateFlow<Boolean>

    fun connect()

    fun disconnect()
}
