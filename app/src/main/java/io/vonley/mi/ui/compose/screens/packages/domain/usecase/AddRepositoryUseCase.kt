package io.vonley.mi.ui.compose.screens.packages.domain.usecase

import io.vonley.mi.common.Resource
import io.vonley.mi.ui.compose.screens.packages.data.remote.dto.Repo
import io.vonley.mi.ui.compose.screens.packages.domain.repository.PackageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class AddRepositoryUseCase @Inject constructor(
    private val repository: PackageRepository
) {

    operator fun invoke(repoLink: String): Flow<Resource<Repo>> = flow {
        emit(Resource.Loading())
        val repo = repository.addRepository(repoLink)
        emit(repo)
    }.catch {
        emit(Resource.Error("Repo not found!", null))
    }
}