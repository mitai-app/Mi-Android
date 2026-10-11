package io.vonley.mi.intents

import android.app.Service
import android.content.Intent
import android.os.IBinder
import dagger.hilt.android.AndroidEntryPoint
import io.vonley.mi.BuildConfig
import io.vonley.mi.Mi
import io.vonley.mi.common.coroutines.supervisedIoContext
import io.vonley.mi.di.annotations.GuestInterceptorOkHttpClient
import io.vonley.mi.di.annotations.SharedPreferenceStorage
import io.vonley.mi.di.network.MiServer
import io.vonley.mi.extensions.e
import io.vonley.mi.models.AppUpdate
import io.vonley.mi.models.ver
import io.vonley.mi.ui.screens.consoles.domain.remote.SyncService
import io.vonley.mi.utils.SharedPreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import javax.inject.Inject

/**
 * Keeps the jailbreak server and the console scan running while the app is in use.
 */
@AndroidEntryPoint
class MiService : Service() {

    @Inject
    lateinit var server: MiServer

    @Inject
    lateinit var sync: SyncService

    @Inject
    @SharedPreferenceStorage
    lateinit var manager: SharedPreferenceManager

    @Inject
    @GuestInterceptorOkHttpClient
    lateinit var http: OkHttpClient

    private val scope = CoroutineScope(supervisedIoContext(TAG))
    private var updateJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (manager.jbService) {
            Mi.log(Mi.MiEvent.MI_SERVICE_START, "Service" to TAG)
            server.startService()
        }
        sync.getClients(loop = true)
        checkForUpdates()
        return START_STICKY
    }

    override fun onDestroy() {
        sync.stop()
        server.stopService()
        Mi.log(Mi.MiEvent.MI_SERVICE_END, "Service" to TAG)
        scope.cancel()
        super.onDestroy()
    }

    private fun checkForUpdates() {
        if (updateJob?.isActive == true) return
        updateJob = scope.launch {
            while (isActive) {
                val update = runCatching { fetchLatest() }
                    .onFailure { "Update check failed: ${it.message}".e(TAG) }
                    .getOrNull()
                if (update != null) {
                    manager.update = update
                    return@launch
                }
                delay(UPDATE_INTERVAL_MS)
            }
        }
    }

    /** Returns the published release when it is newer than this build. */
    private fun fetchLatest(): AppUpdate? {
        val request = Request.Builder().url(VERSION_URL).get().build()
        val body = http.newCall(request).execute().use { it.body.string() }
        val json = JSONObject(body)
        val version = json.getString("version")
        if (version.ver <= BuildConfig.VERSION_NAME.ver) return null
        val build = json.optString("build")
        val changes = json.optJSONArray("changes")
            ?.let { list -> (0 until list.length()).joinToString("\n") { list[it].toString() } }
            .orEmpty()
        return AppUpdate(version, changes, build)
    }

    companion object {
        const val TAG = "MiService"
        private const val VERSION_URL = "https://raw.githubusercontent.com/mitai-app/versioning/main/android.json"
        private const val UPDATE_INTERVAL_MS = 60 * 60 * 1000L
    }
}
