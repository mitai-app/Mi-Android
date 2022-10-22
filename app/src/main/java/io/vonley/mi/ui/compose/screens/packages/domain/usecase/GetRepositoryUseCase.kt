package io.vonley.mi.ui.compose.screens.packages.domain.usecase

import io.vonley.mi.common.Resource
import io.vonley.mi.ui.compose.screens.packages.data.remote.dto.Repo
import io.vonley.mi.ui.compose.screens.packages.domain.repository.PackageRepository
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class GetRepositoryUseCase @Inject constructor(
    private val repository: PackageRepository
) {
    operator fun invoke(): Flow<Resource<List<Repo>>> = flow {
        emit(Resource.Loading())
        val repo = repository.getRepositories()
        repo.onEach {
            emit(it)
        }.collect()
    }

    operator fun invoke(link: String): Flow<Resource<List<Repo>>> = flow {
        emit(Resource.Loading())
        val repo = repository.getRepository(link)
        val value = repo.single()
        emit(value)
    }
}