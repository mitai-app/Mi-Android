package io.vonley.mi.ui.compose.screens.consoles.domain.usecase

import io.vonley.mi.ui.compose.screens.consoles.domain.model.Console
import io.vonley.mi.ui.compose.screens.consoles.domain.repository.ConsoleRepository
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class SelectConsoleUseCase @Inject constructor(
    private val repo: ConsoleRepository
) {

    operator fun invoke(console: Console) =  flow {
        repo.selectConsole(console)
        emit(console)
    }

}