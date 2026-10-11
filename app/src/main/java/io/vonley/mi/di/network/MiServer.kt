package io.vonley.mi.di.network

import io.vonley.mi.models.Device
import io.vonley.mi.models.MIEnumCMD
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/** One line of the jailbreak session log; [cmd] is set for mi.js status updates. */
data class JbLog(
    val id: Long,
    val message: String,
    val cmd: MIEnumCMD? = null,
    val device: Device? = null,
)

/** One-shot notices raised while a PS4 runs the jailbreak. Not replayed. */
sealed interface JbEvent {
    data class JailbreakSucceeded(val message: String) : JbEvent
    data class JailbreakFailed(val message: String) : JbEvent
    data class PayloadSent(val message: String) : JbEvent
    data class SendPayloadFailed(val attempts: Int) : JbEvent
}

/** The local web server a PS4 browser visits to run the jailbreak. */
interface MiServer {
    val activePort: Int

    /** URL to type into the PS4 browser, e.g. http://192.168.1.20:8080 */
    val hostAddress: String

    val isRunning: StateFlow<Boolean>

    /** The PS4 that most recently loaded the host. */
    val device: StateFlow<Device?>

    /** Session log, newest last, bounded in size. */
    val logs: StateFlow<List<JbLog>>

    val events: SharedFlow<JbEvent>

    fun startService()
    fun stopService()
    fun restartService()
}
