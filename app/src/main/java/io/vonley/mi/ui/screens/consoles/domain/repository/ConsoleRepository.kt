package io.vonley.mi.ui.screens.consoles.domain.repository

import io.vonley.mi.ui.screens.consoles.domain.model.Client
import io.vonley.mi.ui.screens.consoles.domain.model.Console
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface ConsoleRepository {
    val target: StateFlow<Client?>
    fun getMyConsoles(): Flow<List<Console>>
    suspend fun addConsole(ip: String): Console
    suspend fun setPin(ip: String, pinned: Boolean)
    fun selectConsole(console: Client)
    fun refresh()
}
