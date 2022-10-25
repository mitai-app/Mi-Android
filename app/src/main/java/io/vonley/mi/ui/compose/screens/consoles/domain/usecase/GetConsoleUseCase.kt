package io.vonley.mi.ui.compose.screens.consoles.domain.usecase

import androidx.lifecycle.asFlow
import io.vonley.mi.ui.compose.screens.consoles.domain.model.Console
import io.vonley.mi.ui.compose.screens.consoles.domain.repository.ConsoleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class GetConsoleUseCase @Inject constructor(
    private val repo: ConsoleRepository
) {

    operator fun invoke(): Flow<List<Console>> = repo.getMyConsoles().asFlow()


}