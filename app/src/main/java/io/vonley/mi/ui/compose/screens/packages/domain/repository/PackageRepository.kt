package io.vonley.mi.ui.compose.screens.packages.domain.repository

import io.vonley.mi.common.Resource
import io.vonley.mi.ui.compose.screens.packages.data.remote.dto.Repo
import kotlinx.coroutines.flow.Flow

interface PackageRepository {
    suspend fun getRepository(link: String): Flow<Resource<List<Repo>>>
    suspend fun getRepositories(): Flow<Resource<List<Repo>>>

    suspend fun exists(link: String): Boolean
    suspend fun delete(link: String)
}