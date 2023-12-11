package io.vonley.mi

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.nsd.NsdManager
import android.net.nsd.NsdManager.RegistrationListener
import android.net.nsd.NsdManager.ResolveListener
import android.net.nsd.NsdServiceInfo
import android.util.Log
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp
import io.vonley.mi.extensions.e
import io.vonley.mi.intents.PSXService

@HiltAndroidApp
class MiApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        startService(Intent(this, PSXService::class.java))
    }

    companion object {
        const val TAG = "MiApplication"
    }

}