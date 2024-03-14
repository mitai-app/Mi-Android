package io.vonley.mi.ui.screens.consoles.presentation

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.vonley.mi.di.annotations.SharedPreferenceStorage
import io.vonley.mi.extensions.e
import io.vonley.mi.ui.screens.consoles.domain.model.Client
import io.vonley.mi.ui.screens.consoles.domain.model.Console
import io.vonley.mi.ui.screens.consoles.domain.usecase.AddConsoleUseCase
import io.vonley.mi.ui.screens.consoles.domain.usecase.GetConsoleUseCase
import io.vonley.mi.ui.screens.consoles.domain.usecase.SelectConsoleUseCase
import io.vonley.mi.utils.SharedPreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConsoleViewModel @Inject constructor(
    private val consoleUseCase: GetConsoleUseCase,
    private val addConsoleUseCase: AddConsoleUseCase,
    private val selectConsoleUseCase: SelectConsoleUseCase,
    @SharedPreferenceStorage val manager: SharedPreferenceManager
) : ViewModel() {

    private var _consoles: MutableStateFlow<ConsoleState> = MutableStateFlow(ConsoleState.Loading)

    val consoles: StateFlow<ConsoleState> get() = _consoles

    init {
        getConsoles()
    }

    private fun getConsoles() {
        consoleUseCase()
            .flowOn(Dispatchers.IO)
            .onEach { consoles ->
                _consoles.update {
                    ConsoleState.Success(consoles)
                }
            }.onCompletion { error ->
                if (error != null) {
                    "Error ${error.message}".e("ERROR", error)
                    _consoles.update {
                        ConsoleState.Error("Unable to fetch consoles: ${error.message}")
                    }
                }
            }.catch { error ->
                "Error ${error.message}".e("ERROR", error)
                _consoles.update {
                    ConsoleState.Error("Unable to fetch consoles: ${error.message}")
                }
            }.launchIn(viewModelScope)
    }

    fun addConsole(input: String) = viewModelScope.launch {
        addConsoleUseCase(input).collect { console ->

        }
    }


    fun pin(client: Client) = viewModelScope.launch {
        addConsoleUseCase.pin(client.ip, true)
    }

    fun unpin(client: Client) = viewModelScope.launch {
        addConsoleUseCase.pin(client.ip, false)
    }

    fun select(console: Console) {
        selectConsoleUseCase(console)
            .onEach { con ->
                "${con.ip} set as target".e(TAG)
            }
            .launchIn(viewModelScope)
    }

    val TAG: String
        get() = ConsoleViewModel::class.java.name


}