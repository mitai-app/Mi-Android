package io.vonley.mi.ui.screens.consoles.domain.usecase

import io.vonley.mi.ui.screens.consoles.domain.model.Console
import io.vonley.mi.ui.screens.consoles.domain.repository.ConsoleRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetConsolesUseCase @Inject constructor(
    private val repo: ConsoleRepository
) {
    operator fun invoke(): Flow<List<Console>> = repo.getMyConsoles()
}
