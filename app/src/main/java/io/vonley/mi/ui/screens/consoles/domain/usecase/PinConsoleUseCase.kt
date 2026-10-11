package io.vonley.mi.ui.screens.consoles.domain.usecase

import io.vonley.mi.ui.screens.consoles.domain.repository.ConsoleRepository
import javax.inject.Inject

class PinConsoleUseCase @Inject constructor(
    private val repo: ConsoleRepository
) {
    suspend operator fun invoke(ip: String, pinned: Boolean) = repo.setPin(ip, pinned)
}
