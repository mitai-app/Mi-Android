package io.vonley.mi.ui.screens.packages.domain.remote

import io.vonley.mi.models.Payload
import kotlinx.coroutines.Job

interface RemotePackageInstaller {
    fun hostPackage(vararg payload: Payload): Array<String>
    fun start(port: Int = 12800): Job
}