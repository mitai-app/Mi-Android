package io.vonley.mi.di.network.impl

import io.vonley.mi.Mi
import io.vonley.mi.di.annotations.GuestInterceptorOkHttpClient
import io.vonley.mi.di.annotations.SharedPreferenceStorage
import io.vonley.mi.di.network.PSXService
import io.vonley.mi.models.enums.Feature
import io.vonley.mi.ui.screens.consoles.domain.remote.SyncService
import io.vonley.mi.utils.SharedPreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import javax.inject.Inject

class PSXServiceImpl @Inject constructor(
    override val sync: SyncService,
    @GuestInterceptorOkHttpClient override val http: OkHttpClient,
    @SharedPreferenceStorage override val manager: SharedPreferenceManager
) : PSXService {

    override val TAG: String = PSXService::class.java.name

    override suspend fun connectTarget() {
        val target = target ?: return
        val allowed = target.type.features
        val opened = withContext(Dispatchers.IO) {
            target.features
                .filter { it in allowed && it in Feature.allowedToOpen }
                .filter { sync.createSocket(target, it) != null }
        }
        Mi.log(
            Mi.MiEvent.CONSOLE_SERVICES,
            "console_features" to opened.joinToString { it.name },
            "console_target" to target.type.name
        )
    }
}
