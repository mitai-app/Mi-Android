package io.vonley.mi.ui.main.console.domain.usecase

import androidx.lifecycle.asFlow
import io.vonley.mi.ui.main.console.domain.model.Console
import io.vonley.mi.ui.main.console.domain.repository.ConsoleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class GetConsoleUseCase @Inject constructor(
    private val repo: ConsoleRepository
) {

    operator fun invoke(): Flow<List<Console>> = repo.getMyConsoles().asFlow()


}