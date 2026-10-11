package io.vonley.mi.ui.screens.consoles.data.local

import androidx.room.Dao
import androidx.room.Query
import io.vonley.mi.di.persistence.IDao
import io.vonley.mi.models.enums.Feature
import io.vonley.mi.models.enums.PlatformType
import io.vonley.mi.ui.screens.consoles.domain.model.Console
import kotlinx.coroutines.flow.Flow

@Dao
interface ConsoleDao : IDao<Console, String> {

    @Query("SELECT * FROM Console WHERE LENGTH(features) >= 3 AND ((wifi = :wifi_) OR (name IS NOT ip)) ORDER BY pinned DESC, lastKnownReachable DESC, LENGTH(features) DESC, ip ASC")
    fun get(wifi_: String): Flow<List<Console>>

    @Query("SELECT * FROM Console WHERE ip = :ip_ LIMIT 1")
    suspend fun getByIp(ip_: String): Console?

    @Query("SELECT * FROM Console WHERE LENGTH(features) >= 3 ORDER BY pinned DESC, lastKnownReachable DESC, LENGTH(features) DESC, ip ASC ")
    fun getAll(): Flow<List<Console>>

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

    /** Drops a host that answered the scan but exposes no console services, unless the user pinned it. */
    @Query("DELETE FROM Console WHERE ip = :ip_ AND pinned = 0")
    suspend fun deleteUnpinned(ip_: String)

    /** Forgets unpinned consoles on this network that the latest scan did not find. */
    @Query("DELETE FROM Console WHERE pinned = 0 AND wifi = :wifi_ AND ip NOT IN (:ips_)")
    suspend fun deleteMissing(wifi_: String, ips_: List<String>)

    /** Pinned consoles survive a scan miss but should show as offline. */
    @Query("UPDATE Console SET lastKnownReachable = 0 WHERE wifi = :wifi_ AND ip NOT IN (:ips_)")
    suspend fun markUnreachable(wifi_: String, ips_: List<String>)
}
