package io.vonley.mi.ui.screens.settings.data

import io.vonley.mi.BuildConfig
import io.vonley.mi.di.annotations.SharedPreferenceStorage
import io.vonley.mi.di.network.MiServer
import io.vonley.mi.di.persistence.AppDatabase
import io.vonley.mi.models.enums.Feature
import io.vonley.mi.ui.screens.consoles.domain.remote.SyncService
import io.vonley.mi.ui.screens.settings.domain.Settings
import io.vonley.mi.ui.screens.settings.domain.SettingsRepository
import io.vonley.mi.utils.SharedPreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    @SharedPreferenceStorage private val manager: SharedPreferenceManager,
    private val sync: SyncService,
    private val server: MiServer,
    private val database: AppDatabase,
) : SettingsRepository {

    override fun current() = Settings(
        wifiConnected = sync.isConnected,
        wifiSsid = sync.wifiSsid,
        deviceIp = sync.localDeviceIp,
        targetName = manager.targetName,
        targetVersion = manager.targetVersion,
        ftpUser = manager.ftpUser,
        ftpPasswordSet = manager.ftpPass.isNotEmpty(),
        payloadFeature = manager.featurePort,
        jbPort = manager.jbPort,
        scanInterval = manager.scanInterval,
        jbServiceEnabled = manager.jbService,
        cacheEnabled = manager.cached,
        appVersion = BuildConfig.VERSION_NAME,
    )

    override fun setFtpUser(user: String) {
        manager.ftpUser = user
    }

    override fun setFtpPassword(password: String) {
        manager.ftpPass = password
    }

    override fun setPayloadFeature(feature: Feature) {
        manager.featurePort = feature
    }

    override fun setJbPort(port: Int) {
        manager.jbPort = port
        if (manager.jbService) server.restartService()
    }

    override fun setScanInterval(seconds: Int) {
        manager.scanInterval = seconds
    }

    override fun setJbServiceEnabled(enabled: Boolean) {
        manager.jbService = enabled
        if (enabled) server.startService() else server.stopService()
    }

    override fun setCacheEnabled(enabled: Boolean) {
        manager.cached = enabled
    }

    override suspend fun clearAll() = withContext(Dispatchers.IO) {
        manager.clear()
        database.clearAllTables()
    }
}
