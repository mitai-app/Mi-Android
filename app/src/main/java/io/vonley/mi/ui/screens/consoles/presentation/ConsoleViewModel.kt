package io.vonley.mi.ui.screens.consoles.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.vonley.mi.ui.screens.consoles.domain.model.Client
import io.vonley.mi.ui.screens.consoles.domain.model.Console
import io.vonley.mi.ui.screens.consoles.domain.model.protocolFeatures
import io.vonley.mi.ui.screens.consoles.domain.usecase.AddConsoleUseCase
import io.vonley.mi.ui.screens.consoles.domain.usecase.GetConsolesUseCase
import io.vonley.mi.ui.screens.consoles.domain.usecase.PinConsoleUseCase
import io.vonley.mi.ui.screens.consoles.domain.usecase.RefreshConsolesUseCase
import io.vonley.mi.ui.screens.consoles.domain.usecase.SelectConsoleUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ConsolesUiState(
    val consoles: List<Console> = emptyList(),
    val loading: Boolean = true,
    val error: String? = null,
    val targetIp: String? = null,
    /** Console whose protocol controls are open in the bottom sheet. */
    val sheetTarget: Client? = null,
)

@HiltViewModel
class ConsoleViewModel @Inject constructor(
    getConsoles: GetConsolesUseCase,
    private val addConsole: AddConsoleUseCase,
    private val pinConsole: PinConsoleUseCase,
    private val selectConsole: SelectConsoleUseCase,
    private val refreshConsoles: RefreshConsolesUseCase,
) : ViewModel() {

    private val sheetTarget = MutableStateFlow<Client?>(null)
    private val messageChannel = Channel<String>(Channel.BUFFERED)
    val messages: Flow<String> = messageChannel.receiveAsFlow()

    private val consoles = getConsoles()
        .map { ConsolesUiState(consoles = it, loading = false) }
        .onStart { emit(ConsolesUiState(loading = true)) }
        .catch { emit(ConsolesUiState(loading = false, error = "Unable to fetch consoles: ${it.message}")) }

    val state: StateFlow<ConsolesUiState> = combine(consoles, selectConsole.target, sheetTarget) { list, target, sheet ->
        list.copy(targetIp = target?.ip, sheetTarget = sheet)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ConsolesUiState())

    fun select(console: Console) {
        selectConsole(console)
        messageChannel.trySend("Target set to ${console.name}")
        if (console.protocolFeatures.isNotEmpty()) sheetTarget.value = console
    }

    fun openControls(console: Console) {
        selectConsole(console)
        sheetTarget.value = console
    }

    fun dismissControls() {
        sheetTarget.value = null
    }

    fun togglePin(console: Console) = viewModelScope.launch {
        pinConsole(console.ip, !console.pinned)
    }

    fun add(ip: String) = viewModelScope.launch {
        addConsole(ip)
            .onSuccess { messageChannel.send("Console ${it.ip} added") }
            .onFailure { messageChannel.send(it.message ?: "Unable to add console") }
    }

    fun refresh() {
        refreshConsoles()
        messageChannel.trySend("Scanning the network…")
    }
}
