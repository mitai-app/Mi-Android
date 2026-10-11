package io.vonley.mi.ui.screens.packages.domain.usecase

import io.vonley.mi.di.network.PSXService
import io.vonley.mi.models.enums.PlatformType
import io.vonley.mi.ui.screens.packages.data.local.entity.Package
import io.vonley.mi.ui.screens.packages.domain.remote.PayloadProgress
import io.vonley.mi.ui.screens.packages.domain.remote.PayloadSender
import io.vonley.mi.ui.screens.packages.domain.repository.PackageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/** Raised before anything is sent when the package cannot target the console. */
class SendPackageException(val title: String, message: String) : Exception(message)

class SendPackageUseCase @Inject constructor(
    private val repo: PackageRepository,
    private val service: PSXService,
    private val sender: PayloadSender,
) {
    operator fun invoke(pkg: Package): Flow<PayloadProgress> = flow {
        val console = service.target
            ?: throw SendPackageException("No target", "Select a console on the Consoles tab first.")
        if (console.type != PlatformType.PS4) {
            throw SendPackageException("Unsupported Console", "Packages can only be sent to a PS4.")
        }
        val firmware = service.manager.targetVersion
        if (firmware.isNullOrEmpty()) {
            throw SendPackageException(
                "Unknown firmware",
                "Open the jailbreak page on your PS4 once so Mi can detect its firmware."
            )
        }
        val url = pkg.downloadFor(firmware)
            ?: throw SendPackageException(
                "Package not found",
                "${pkg.name} has no download for firmware $firmware."
            )
        val payload = repo.download(pkg, url)
            ?: throw SendPackageException("Download Failed", "Could not download ${pkg.name}.")
        emitAll(sender.send(listOf(payload)))
    }
}
