package io.vonley.mi.ui.screens.settings.domain

import io.vonley.mi.models.enums.Feature

data class Settings(
    val wifiConnected: Boolean,
    val wifiSsid: String,
    val deviceIp: String,
    val targetName: String?,
    val targetVersion: String?,
    val ftpUser: String,
    val ftpPasswordSet: Boolean,
    val payloadFeature: Feature,
    val jbPort: Int,
    val scanInterval: Int,
    val jbServiceEnabled: Boolean,
    val cacheEnabled: Boolean,
    val appVersion: String,
)

interface SettingsRepository {
    fun current(): Settings
    fun setFtpUser(user: String)
    fun setFtpPassword(password: String)
    fun setPayloadFeature(feature: Feature)

    /** Persists the port and restarts the jailbreak server on it. */
    fun setJbPort(port: Int)
    fun setScanInterval(seconds: Int)

    /** Starts or stops the jailbreak server. */
    fun setJbServiceEnabled(enabled: Boolean)
    fun setCacheEnabled(enabled: Boolean)

    /** Wipes preferences and the database (consoles, repositories). */
    suspend fun clearAll()
}
