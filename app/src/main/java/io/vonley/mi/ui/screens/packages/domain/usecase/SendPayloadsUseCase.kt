package io.vonley.mi.ui.screens.packages.domain.usecase

import android.net.Uri
import io.vonley.mi.common.files.LocalFileSource
import io.vonley.mi.models.Payload
import io.vonley.mi.ui.screens.packages.domain.remote.PayloadProgress
import io.vonley.mi.ui.screens.packages.domain.remote.PayloadSender
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Sends payloads the user picked from local storage. */
class SendPayloadsUseCase @Inject constructor(
    private val files: LocalFileSource,
    private val sender: PayloadSender,
) {
    suspend fun stage(uri: Uri): Payload = files.stage(uri)

    operator fun invoke(payloads: List<Payload>): Flow<PayloadProgress> = sender.send(payloads)
}
