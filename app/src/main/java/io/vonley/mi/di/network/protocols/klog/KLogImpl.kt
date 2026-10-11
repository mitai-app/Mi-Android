package io.vonley.mi.di.network.protocols.klog

import io.vonley.mi.common.coroutines.supervisedIoContext
import io.vonley.mi.di.network.PSXService
import io.vonley.mi.extensions.e
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.OutputStream

class KLogImpl(override val service: PSXService) : KLog {

    private val scope = CoroutineScope(supervisedIoContext(TAG))
    private var reader: Job? = null
    private var _br: BufferedReader? = null

    private val _logs = MutableSharedFlow<String>(
        replay = REPLAY,
        extraBufferCapacity = REPLAY,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override val logs: SharedFlow<String> = _logs.asSharedFlow()

    private val _isStreaming = MutableStateFlow(false)
    override val isStreaming: StateFlow<Boolean> = _isStreaming.asStateFlow()

    override val pw: OutputStream get() = socket.getOutputStream()
    override val br: BufferedReader get() = _br ?: throw IllegalStateException("KLog is not connected")

    override fun connect() {
        if (reader?.isActive == true) return
        reader = scope.launch {
            val socket = socketOrNull ?: service.sync.createSocket(service.target, feature)
            if (socket == null) {
                "Unable to open klog socket".e(TAG)
                return@launch
            }
            _br = socket.getInputStream().bufferedReader()
            _isStreaming.value = true
            try {
                while (isActive) {
                    // readLine() returns null once the console closes the stream
                    val line = recv() ?: break
                    _logs.emit(line)
                }
            } catch (e: Throwable) {
                "klog stream ended: ${e.message}".e(TAG)
            } finally {
                _isStreaming.value = false
                _br = null
                // A remote close leaves isConnected == true; drop it so the next connect reopens.
                runCatching { socket.close() }
            }
        }
    }

    override fun disconnect() {
        reader?.cancel()
        reader = null
        // readLine() ignores cancellation; closing the socket unblocks it.
        runCatching { socketOrNull?.close() }
    }

    override val TAG: String
        get() = KLogImpl::class.simpleName ?: "KLogImpl"

    private companion object {
        const val REPLAY = 500
    }
}
