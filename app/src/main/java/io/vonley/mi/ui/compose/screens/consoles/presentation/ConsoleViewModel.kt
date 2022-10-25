package io.vonley.mi.ui.compose.screens.consoles.presentation

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.vonley.mi.di.annotations.SharedPreferenceStorage
import io.vonley.mi.extensions.e
import io.vonley.mi.ui.compose.screens.consoles.domain.model.Client
import io.vonley.mi.ui.compose.screens.consoles.domain.model.Console
import io.vonley.mi.ui.compose.screens.consoles.domain.usecase.AddConsoleUseCase
import io.vonley.mi.ui.compose.screens.consoles.domain.usecase.GetConsoleUseCase
import io.vonley.mi.ui.compose.screens.consoles.domain.usecase.SelectConsoleUseCase
import io.vonley.mi.utils.SharedPreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class ConsoleViewModel @Inject constructor(
    private val consoleUseCase: GetConsoleUseCase,
    private val addConsoleUseCase: AddConsoleUseCase,
    private val selectConsoleUseCase: SelectConsoleUseCase,
    @SharedPreferenceStorage val manager: SharedPreferenceManager
) : ViewModel() {

    private var _consoles: MutableState<ConsoleState> = mutableStateOf(ConsoleState.Loading)

    val consoles: State<ConsoleState> get() = _consoles

    init {
        getConsoles()
    }

    private fun getConsoles() {
        consoleUseCase()
            .flowOn(Dispatchers.IO)
            .onEach { consoles ->
                _consoles.value = ConsoleState.Success(consoles)
            }.onCompletion { error ->
                if (error != null) {
                    "Error ${error.message}".e("ERROR", error)
                    _consoles.value = ConsoleState.Error("Unable to fetch consoles: ${error.message}")
                }
            }.catch { error ->
                "Error ${error.message}".e("ERROR", error)
                _consoles.value = ConsoleState.Error("Unable to fetch consoles: ${error.message}")
            }.launchIn(viewModelScope)
    }

    fun addConsole(input: String) {
        addConsoleUseCase(input).onEach { console ->

        }.launchIn(viewModelScope)
    }

    fun pin(client: Client) {
        suspend {
            addConsoleUseCase.pin(client.ip, true)
        }.asFlow().launchIn(viewModelScope)
    }

    fun unpin(client: Client) {
        suspend {
            addConsoleUseCase.pin(client.ip, false)
        }.asFlow().launchIn(viewModelScope)
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