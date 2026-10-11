package io.vonley.mi.di.network

import io.vonley.mi.common.base.BaseClient
import io.vonley.mi.ui.screens.consoles.domain.model.Client
import io.vonley.mi.ui.screens.consoles.domain.remote.SyncService
import io.vonley.mi.utils.SharedPreferenceManager

/**
 * Shared entry point for the console protocols: the selected target, its
 * sockets (through [sync]) and an HTTP client.
 */
interface PSXService : BaseClient {

    val sync: SyncService

    val manager: SharedPreferenceManager

    val target: Client? get() = sync.target

    val targetIp: String? get() = target?.ip

    val localDeviceIp: String get() = sync.localDeviceIp

    /**
     * Opens the long-lived sockets for every service the target advertises so
     * protocol panels can reuse them.
     */
    suspend fun connectTarget()
}
