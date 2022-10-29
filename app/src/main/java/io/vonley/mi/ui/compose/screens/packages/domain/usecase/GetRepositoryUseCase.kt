package io.vonley.mi.ui.compose.screens.packages.domain.usecase

import io.vonley.mi.common.Resource
import io.vonley.mi.ui.compose.screens.packages.data.local.entity.Repo
import io.vonley.mi.ui.compose.screens.packages.domain.repository.PackageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetRepositoryUseCase @Inject constructor(
    private val repository: PackageRepository
) {
    operator fun invoke(): Flow<Resource<List<Repo>>> = flow {
        emit(Resource.Loading())
        val repo = repository.getRepositories()
        emit(repo)
    }.catch {
        emit(Resource.Error("Unable to fetch data source", emptyList()))
    }

    operator fun invoke(search: String): Flow<Resource<List<Repo>>> = flow {
        val repo = repository.searchRelevance(search)
        emit(repo)
    }.catch {
        emit(Resource.Error("Repo not found!", emptyList()))
    }
}