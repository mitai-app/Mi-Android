package io.vonley.mi.ui.screens.packages.domain.remote

import io.vonley.mi.models.Payload

/**
 * Hosts packages on the phone so the console's Remote Package Installer can
 * download them.
 */
interface RemotePackageInstaller {
    /** Serves [payload] and returns the URL the console should fetch it from. */
    fun hostPackage(payload: Payload): String
}
