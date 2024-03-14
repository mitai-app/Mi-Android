package io.vonley.mi.di.network.protocols.goldenhen

import io.vonley.mi.di.network.callbacks.PayloadCallback
import io.vonley.mi.ui.screens.packages.domain.remote.RemotePackageInstaller
import io.vonley.mi.di.network.protocols.common.PSXProtocol
import io.vonley.mi.models.Payload
import io.vonley.mi.models.enums.Feature
import io.vonley.mi.ui.screens.consoles.data.remote.get
import java.net.Socket

typealias GoldhenCallback = PayloadCallback
interface Goldhen: PSXProtocol {

    override val feature: Feature get() = Feature.GOLDENHEN
    private val _socket: Socket? get() = service[service.target, feature]
    override val socket: Socket get() = _socket!!
    val rpi: RemotePackageInstaller

    override val TAG: String
        get() = Goldhen::class.qualifiedName ?: Goldhen::javaClass.name

    suspend fun sendPayloads(
        callback: GoldhenCallback,
        vararg payloads: Payload
    ): Map<String, Payload>

}