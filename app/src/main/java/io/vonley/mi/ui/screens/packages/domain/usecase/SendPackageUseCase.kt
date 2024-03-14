package io.vonley.mi.ui.screens.packages.domain.usecase

import io.vonley.mi.common.Resource
import io.vonley.mi.di.network.MiServer
import io.vonley.mi.di.network.PSXService
import io.vonley.mi.di.network.callbacks.PayloadCallback
import io.vonley.mi.models.Payload
import io.vonley.mi.models.enums.PlatformType
import io.vonley.mi.ui.screens.consoles.domain.model.Console
import io.vonley.mi.ui.screens.packages.data.local.entity.Package
import io.vonley.mi.ui.screens.packages.data.local.entity.PackageType
import io.vonley.mi.ui.screens.packages.domain.repository.PackageRepository
import io.vonley.mi.utils.SharedPreferenceManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class SendPackageUseCase @Inject constructor(
    val repo: PackageRepository,
    val service: PSXService,
    val miServer: MiServer,
    val manager: SharedPreferenceManager
) {
    operator fun invoke(pkg: Package, onPayloadCallback: PayloadCallback): Flow<Resource<String>> = flow {
        when (pkg.type) {
            PackageType.APP -> {

            }

            PackageType.TOOL -> {


            }

            PackageType.PLUGIN -> {
                val console = (service.sync.target as? Console)

                if (console == null) {
                    this@flow.emit(
                        Resource.Error(
                            "An Error Occurred",
                            "Something went wrong with sending the payload"
                        )
                    )
                    return@flow
                }

                if (console.type != PlatformType.PS4) {
                    this@flow.emit(
                        Resource.Error(
                            "Unsupported Console",
                            "Console is not supported"
                        )
                    )
                    return@flow
                }

                val version = manager.targetVersion?.replace(".", "")
                if(version.isNullOrEmpty()) {
                    this@flow.emit(
                        Resource.Error(
                            "Target is not connected",
                            "Unknown console version... usually we get the target by connecting to the phone server"
                        )
                    )
                    return@flow
                }

                if(!pkg.dl.containsKey(version)) {
                    this@flow.emit(
                        Resource.Error(
                            "Package not found",
                            "There are no legitimate versions for your console associated with this package."
                        )
                    )
                    return@flow
                }

                val payload: Payload? = repo.fromPackage(pkg, version)

                if(payload == null) {
                    this@flow.emit(
                        Resource.Error(
                            "Download Failed",
                            "Could not download the file"
                        )
                    )
                    return@flow
                }

                service.uploadBin(miServer, arrayListOf(payload), onPayloadCallback)
            }
        }

    }
}
