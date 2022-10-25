package io.vonley.mi.ui.compose.screens.packages.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import io.vonley.mi.persistence.IDao
import io.vonley.mi.ui.compose.screens.packages.data.remote.dto.Repo
import kotlinx.coroutines.flow.Flow

@Dao
interface PackageRepositoryDao : IDao<Repo, String> {

    @Query("SELECT * FROM Repo WHERE link LIKE '%' || :link_ || '%'")
    suspend fun get(link_: String): List<Repo>

    @Query("SELECT * FROM Repo WHERE author LIKE '%' || :name_ || '%' or title LIKE '%' || :name_ || '%'")
    fun getByName(name_: String): Flow<List<Repo>>

    @Query("SELECT * FROM Repo WHERE 1")
    suspend fun getAll(): List<Repo>

    @Query("SELECT COUNT(*) FROM Repo")
    suspend fun count(): Int

    @Query("SELECT EXISTS(SELECT * FROM Repo WHERE link = :link_)")
    suspend fun exists(link_: String): Boolean

    @Query("DELETE FROM Repo WHERE link = :link_")
    suspend fun delete(link_: String)

}