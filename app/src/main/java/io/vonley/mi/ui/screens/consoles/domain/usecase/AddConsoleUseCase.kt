package io.vonley.mi.ui.screens.consoles.domain.usecase

import io.vonley.mi.ui.screens.consoles.domain.model.Console
import io.vonley.mi.ui.screens.consoles.domain.repository.ConsoleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class AddConsoleUseCase @Inject constructor(
    private val repo: ConsoleRepository
) {
    operator fun invoke(input: String): Flow<Console> = flow {
        if (repo.exists(input)) {
            repo.updateNickName(input, "Playstation 4")
        } else {
            repo.insert(input)
        }
        val consoles = repo.getConsole(input)
        emit(consoles)
    }


    suspend fun pin(ip: String, pin: Boolean) {
        repo.setPin(ip, pin)
    }
}