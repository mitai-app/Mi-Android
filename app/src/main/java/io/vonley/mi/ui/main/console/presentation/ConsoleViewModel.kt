package io.vonley.mi.ui.main.console.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.vonley.mi.di.annotations.SharedPreferenceStorage
import io.vonley.mi.extensions.e
import io.vonley.mi.ui.main.console.domain.model.Client
import io.vonley.mi.ui.main.console.domain.model.Console
import io.vonley.mi.ui.main.console.domain.usecase.AddConsoleUseCase
import io.vonley.mi.ui.main.console.domain.usecase.GetConsoleUseCase
import io.vonley.mi.utils.SharedPreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class ConsoleViewModel @Inject constructor(
    private val consoleUseCase: GetConsoleUseCase,
    private val addConsoleUseCase: AddConsoleUseCase,
    @SharedPreferenceStorage val manager: SharedPreferenceManager
) : ViewModel() {

    private var _consoles: MutableLiveData<List<Console>> = MutableLiveData()

    val consoles: LiveData<List<Console>> get() = _consoles

    fun getConsoles() {
        consoleUseCase()
            .flowOn(Dispatchers.IO)
            .onEach { consoles ->
                _consoles.postValue(consoles)
            }.onCompletion { error ->
                if (error != null) {
                    "Error ${error.message}".e("ERROR", error)
                }
            }.catch { error ->
                "Error ${error.message}".e("ERROR", error)
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

    val TAG: String
        get() = ConsoleViewModel::class.java.name


}