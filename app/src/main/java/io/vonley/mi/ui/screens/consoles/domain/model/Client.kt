package io.vonley.mi.ui.screens.consoles.domain.model

import io.vonley.mi.models.enums.Feature
import io.vonley.mi.models.enums.PlatformType

/**
 * A host on the local network that may expose console services.
 * @see Console for the persisted form.
 */
interface Client {
    val ip: String
    val name: String
    val type: PlatformType
    val features: List<Feature>
    val wifi: String
    val lastKnownReachable: Boolean
    val pinned: Boolean
}

/** A reachable host discovered during a network scan, before its ports are probed. */
data class NetworkClient(
    override val ip: String,
    override val wifi: String,
    override val name: String = ip,
    override val type: PlatformType = PlatformType.UNKNOWN,
    override val features: List<Feature> = emptyList(),
    override val lastKnownReachable: Boolean = true,
    override val pinned: Boolean = false,
) : Client

val Client.activeFeatures: List<Feature>
    get() = features.filter { p -> p != Feature.NONE }

val Client.featureString: String
    get() = activeFeatures.joinToString { f -> f.title }

val List<Feature>.isPs3: Boolean
    get() = any { p -> p in PlatformType.PS3.features }

val List<Feature>.isPs4: Boolean
    get() = any { p -> p in PlatformType.PS4.features }

/** Features that have an interactive control panel in the console sheet. */
val Client.protocolFeatures: List<Feature>
    get() = activeFeatures.filter { it in Feature.controllable }
