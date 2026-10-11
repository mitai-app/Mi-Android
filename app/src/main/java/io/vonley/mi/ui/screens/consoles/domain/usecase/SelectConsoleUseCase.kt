package io.vonley.mi.ui.screens.consoles.domain.usecase

import io.vonley.mi.ui.screens.consoles.domain.model.Client
import io.vonley.mi.ui.screens.consoles.domain.repository.ConsoleRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class SelectConsoleUseCase @Inject constructor(
    private val repo: ConsoleRepository
) {
    val target: StateFlow<Client?> get() = repo.target

    operator fun invoke(console: Client) = repo.selectConsole(console)
}
