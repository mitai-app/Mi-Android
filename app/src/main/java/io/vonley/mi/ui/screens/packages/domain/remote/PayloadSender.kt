package io.vonley.mi.ui.screens.packages.domain.remote

import io.vonley.mi.models.Payload
import kotlinx.coroutines.flow.Flow

sealed interface PayloadProgress {
    val payload: Payload

    data class Writing(override val payload: Payload) : PayloadProgress
    data class Sent(override val payload: Payload) : PayloadProgress
    data class Failed(override val payload: Payload, val reason: String) : PayloadProgress
}

/**
 * Sends `.bin` payloads to the configured loader port and installs `.pkg`
 * files through the console's Remote Package Installer.
 */
interface PayloadSender {
    fun send(payloads: List<Payload>): Flow<PayloadProgress>
}
