package io.vonley.mi.ui.screens.consoles.domain.usecase

import io.vonley.mi.ui.screens.consoles.domain.repository.ConsoleRepository
import javax.inject.Inject

class RefreshConsolesUseCase @Inject constructor(
    private val repo: ConsoleRepository
) {
    operator fun invoke() = repo.refresh()
}
