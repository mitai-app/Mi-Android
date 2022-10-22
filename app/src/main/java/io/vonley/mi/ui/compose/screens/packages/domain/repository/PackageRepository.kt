package io.vonley.mi.ui.compose.screens.packages.domain.repository

import androidx.lifecycle.LiveData
import io.vonley.mi.common.Resource
import io.vonley.mi.ui.compose.screens.packages.data.remote.dto.Repo
import kotlinx.coroutines.flow.Flow

interface PackageRepository {
    suspend fun getRepository(link: String): Resource<List<Repo>>
    suspend fun getRepositories(): Resource<List<Repo>>

    suspend fun exists(link: String): Boolean
    suspend fun delete(link: String)
    suspend fun count(): Int
    suspend fun getDefaultRepo(): Boolean
}