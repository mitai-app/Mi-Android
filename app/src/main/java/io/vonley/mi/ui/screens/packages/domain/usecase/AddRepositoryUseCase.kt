package io.vonley.mi.ui.screens.packages.domain.usecase

import io.vonley.mi.common.Resource
import io.vonley.mi.ui.screens.packages.data.local.entity.Repo
import io.vonley.mi.ui.screens.packages.domain.repository.PackageRepository
import javax.inject.Inject

class AddRepositoryUseCase @Inject constructor(
    private val repository: PackageRepository
) {
    suspend operator fun invoke(repoLink: String): Resource<Repo> =
        runCatching { repository.addRepository(repoLink.trim()) }
            .getOrElse { Resource.Error("Repo not found!") }
}
