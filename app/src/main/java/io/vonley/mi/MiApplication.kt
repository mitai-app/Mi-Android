package io.vonley.mi

import android.app.Application
import android.app.PendingIntent
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.nsd.NsdManager
import android.net.nsd.NsdManager.RegistrationListener
import android.net.nsd.NsdManager.ResolveListener
import android.net.nsd.NsdServiceInfo
import android.os.Build
import android.util.Log
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp
import io.vonley.mi.extensions.e
import io.vonley.mi.intents.PSXService
import io.vonley.mi.intents.PSXServiceJob

@HiltAndroidApp
class MiApplication : Application() {

    private var jobScheduler: JobScheduler? = null

    override fun onCreate() {
        super.onCreate()
        jobScheduler = getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler
        FirebaseApp.initializeApp(this)
        val intent = Intent(this, PSXService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //DO Job Scheduler
            val name = ComponentName(this, PSXServiceJob::class.java)
            val info = JobInfo.Builder(101, name)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setRequiresCharging(false)
                .setPersisted(true)
                .build()
            if (jobScheduler?.schedule(info) == JobScheduler.RESULT_SUCCESS) {
                Log.e("MiApplication", "JOB SERVICE RUNNING")
            } else {
                Log.e("MiApplication", "JOB SERVICE NOT RUNNING")
            }

        } else {
            startService(intent)
        }
        Log.i("Autostart", "started")
    }

    companion object {
        const val TAG = "MiApplication"
    }

}