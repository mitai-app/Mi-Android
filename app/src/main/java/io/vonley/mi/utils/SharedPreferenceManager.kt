package io.vonley.mi.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.StringRes
import androidx.core.content.edit
import io.vonley.mi.R
import io.vonley.mi.extensions.fromJson
import io.vonley.mi.extensions.toJson
import io.vonley.mi.models.AppUpdate
import io.vonley.mi.models.enums.Feature

/**
 * Typed access to the app's preferences. Values keep the storage types the
 * legacy PreferenceFragment wrote (ports/intervals as strings) so existing
 * installs keep their settings.
 */
interface SharedPreferenceManager {

    val context: Context

    val sharedPreferences: SharedPreferences

    fun clear() = sharedPreferences.edit { clear() }

    var cached: Boolean
        get() = sharedPreferences.getBoolean(this[CACHED], false)
        set(value) = sharedPreferences.edit { putBoolean(this@SharedPreferenceManager[CACHED], value) }

    var jbService: Boolean
        get() = sharedPreferences.getBoolean(this[MIJBSERVICE], true)
        set(value) = sharedPreferences.edit { putBoolean(this@SharedPreferenceManager[MIJBSERVICE], value) }

    var featurePort: Feature
        get() {
            val stored = stringOrNull(MIJBFEATUREPORT)
            return PAYLOAD_FEATURES.find { this[it.id] == stored } ?: Feature.GOLDENHEN
        }
        set(port) = sharedPreferences.edit { putString(this@SharedPreferenceManager[MIJBFEATUREPORT], this@SharedPreferenceManager[port.id]) }

    var scanInterval: Int
        get() = stringOrNull(MISCANINTERVAL)?.toIntOrNull() ?: DEFAULT_SCAN_INTERVAL
        set(interval) = sharedPreferences.edit { putString(this@SharedPreferenceManager[MISCANINTERVAL], interval.toString()) }

    var update: AppUpdate?
        get() = sharedPreferences.getString(this[UPDATE], null)?.fromJson()
        set(value) = sharedPreferences.edit { putString(this@SharedPreferenceManager[UPDATE], value?.toJson()) }

    var jbPort: Int
        get() = stringOrNull(MIJBSERVERPORT)?.toIntOrNull() ?: DEFAULT_JB_PORT
        set(port) = sharedPreferences.edit { putString(this@SharedPreferenceManager[MIJBSERVERPORT], port.toString()) }

    var ftpPath: String
        get() = sharedPreferences.getString(this[FTPPATH], null) ?: "/"
        set(path) = sharedPreferences.edit { putString(this@SharedPreferenceManager[FTPPATH], path) }

    var ftpUser: String
        get() = sharedPreferences.getString(this[FTPUSER], null).orEmpty()
        set(user) = sharedPreferences.edit { putString(this@SharedPreferenceManager[FTPUSER], user) }

    var ftpPass: String
        get() = sharedPreferences.getString(this[FTPPASS], null).orEmpty()
        set(pass) = sharedPreferences.edit { putString(this@SharedPreferenceManager[FTPPASS], pass) }

    var targetName: String?
        get() = sharedPreferences.getString(this[TARGETNAME], null)
        set(name) = sharedPreferences.edit { putString(this@SharedPreferenceManager[TARGETNAME], name) }

    var targetVersion: String?
        get() = sharedPreferences.getString(this[TARGETVER], null)
        set(version) = sharedPreferences.edit { putString(this@SharedPreferenceManager[TARGETVER], version) }

    companion object {
        const val DEFAULT_JB_PORT = 8080
        const val DEFAULT_SCAN_INTERVAL = 15

        /** Ports a payload can be pushed to from the payload screen. */
        val PAYLOAD_FEATURES = listOf(Feature.GOLDENHEN, Feature.ORBISAPI, Feature.RPI)

        val SCAN_INTERVALS = listOf(15, 30, 60, 300, 600, 900)

        val CACHED: Int get() = R.string.preference_jb_cache
        val FTPPATH: Int get() = R.string.preference_ftp_loc
        val FTPUSER: Int get() = R.string.preference_ftp_user
        val FTPPASS: Int get() = R.string.preference_ftp_pass
        val TARGETNAME: Int get() = R.string.preference_target_name
        val TARGETVER: Int get() = R.string.preference_target_version
        val MIJBSERVERPORT: Int get() = R.string.preference_jb_port
        val MISCANINTERVAL: Int get() = R.string.preference_jb_scan
        val MIJBFEATUREPORT: Int get() = R.string.preference_jb_feature
        val MIJBSERVICE: Int get() = R.string.preference_jb_service
        val UPDATE: Int get() = R.string.preference_app_update
    }
}

operator fun SharedPreferenceManager.get(@StringRes string: Int): String {
    return context.getString(string)
}

/**
 * Older builds wrote some of these keys with the wrong type (an Int where a
 * String is expected), which makes getString throw ClassCastException.
 */
private fun SharedPreferenceManager.stringOrNull(@StringRes key: Int): String? =
    when (val value = sharedPreferences.all[this[key]]) {
        null -> null
        is String -> value
        else -> value.toString()
    }
