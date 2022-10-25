package io.vonley.mi.ui.compose.screens.consoles.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import io.vonley.mi.ui.compose.screens.consoles.domain.model.Console
import io.vonley.mi.models.enums.Feature
import io.vonley.mi.models.enums.PlatformType
import io.vonley.mi.persistence.IDao

@Dao
interface ConsoleDao : IDao<Console, String> {

    @Query("SELECT * FROM Console WHERE LENGTH(features) >= 3 AND ((wifi = :wifi_) OR (name IS NOT ip)) ORDER BY pinned DESC, lastKnownReachable DESC, LENGTH(features) DESC, ip ASC")
    fun get(wifi_: String): LiveData<List<Console>>

    @Query("SELECT * FROM Console WHERE  ip = :ip_ AND (LENGTH(features) >= 3 AND ((wifi = :wifi_) OR (name IS NOT ip))) ORDER BY pinned DESC, lastKnownReachable DESC, LENGTH(features) DESC, ip ASC LIMIT 1")
    suspend fun get(ip_: String, wifi_: String): Console

    @Query("SELECT * FROM Console WHERE LENGTH(features) >= 3 ORDER BY pinned DESC, lastKnownReachable DESC, LENGTH(features) DESC, ip ASC ")
    fun getAll(): LiveData<List<Console>>

    @Query("SELECT EXISTS(SELECT * FROM Console WHERE ip = :ip_)")
    suspend fun exists(ip_: String): Boolean

    @Query("UPDATE Console SET name = :name_ WHERE ip = :ip_")
    suspend fun updateNickName(ip_: String, name_: String)


    @Query("UPDATE Console SET type = :type_, features = :features_, lastKnownReachable = :lastKnown_, wifi = :wifi_ WHERE ip = :ip_")
    suspend fun update(
        ip_: String,
        type_: PlatformType,
        features_: List<Feature>,
        lastKnown_: Boolean,
        wifi_: String
    )

    @Query("UPDATE Console SET pinned = :pinned_ WHERE ip = :ip_")
    suspend fun setPin(ip_: String, pinned_: Boolean)

    @Query("DELETE FROM Console WHERE ip = :ip_ AND wifi = :wifi_")
    suspend fun delete(ip_: String, wifi_: String)

    @Query("DELETE FROM Console WHERE pinned = 0 AND ip not in (:ips_)")
    suspend fun delete(ips_: Array<String>)
}