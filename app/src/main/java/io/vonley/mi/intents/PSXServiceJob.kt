package io.vonley.mi.intents

import android.app.Service
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Intent
import android.os.IBinder
import com.google.gson.GsonBuilder
import dagger.hilt.android.AndroidEntryPoint
import io.vonley.mi.BuildConfig
import io.vonley.mi.Mi
import io.vonley.mi.common.base.BaseClient
import io.vonley.mi.di.annotations.SharedPreferenceStorage
import io.vonley.mi.utils.Semver
import io.vonley.mi.utils.SharedPreferenceManager
import kotlinx.coroutines.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@AndroidEntryPoint
class PSXServiceJob : JobService(), BaseClient {

    override fun onStartJob(params: JobParameters?): Boolean {
        if (manager.jbService) {
            Mi.log(Mi.MiEvent.MI_SERVICE_START, Pair("Service", PSXService::class.java.name))
            binder.jb.startService()
        }
        binder.sync.getClients(true)
        checkforUpdates()
        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {

        return true
    }

    override val TAG: String = PSXService::class.simpleName?:"PSXService"

    @Inject
    lateinit var binder: PSXServiceBinder


    override val http: OkHttpClient get() = binder.jb.service.http

    @Inject
    @SharedPreferenceStorage
    lateinit var manager: SharedPreferenceManager


    private var check = true
    private var meta: PSXService.Meta? = null
    private val seconds = 60


    private var update: Job? = null
    private fun checkforUpdates() {
        val block: suspend CoroutineScope.() -> Unit = {
            do {
                if (meta != null) {
                    manager.update = meta
                    continue
                }
                getRequest(
                    "https://raw.githubusercontent.com/mitai-app/versioning/main/android.json",
                    object : Callback {
                        override fun onFailure(call: Call, e: IOException) {

                        }

                        override fun onResponse(call: Call, response: Response) {
                            response.body.let { body ->
                                val json = JSONObject(body.string())
                                val version = json.getString("version")
                                val ver = version.ver
                                val buildVer = BuildConfig.VERSION_NAME.ver
                                val compare = ver > buildVer
                                if (compare) {
                                    val build = if(json.has("build")){
                                        json.getString("build")
                                    } else ""
                                    val change = if(json.has("changes")) {
                                        val changesList = json.getJSONArray("changes")
                                        val changes = StringBuilder()
                                        for (i in 0 until changesList.length()) {
                                            changes.append(changesList[i].toString() + "\n")
                                        }
                                        changes.toString()
                                    }else ""
                                    this@PSXServiceJob.meta =
                                        PSXService.Meta(version, change, build)
                                }
                            }
                        }

                    })
                delay((seconds * 1000L))
            } while (check)
        }
        when {
            update == null -> {
                update = launch(block = block)
            }
            update?.isCancelled == true -> {
                update = launch(block = block)
            }
            update?.isCompleted == true -> {
                update = launch(block = block)
            }
            update?.isActive == true -> {
                //Do nothing
            }
        }
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        return START_STICKY
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        binder.sync.stop()
        binder.jb.stopService()
        Mi.log(Mi.MiEvent.MI_SERVICE_END, Pair("Service", PSXService::class.java.name))
        update?.cancel()
        check = false
        super.onDestroy()
    }

    val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job


}