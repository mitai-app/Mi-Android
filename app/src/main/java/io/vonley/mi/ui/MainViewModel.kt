package io.vonley.mi.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.vonley.mi.di.network.MiServer
import io.vonley.mi.ui.screens.consoles.domain.model.Client
import io.vonley.mi.ui.screens.consoles.domain.usecase.SelectConsoleUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    selectConsole: SelectConsoleUseCase,
    private val server: MiServer,
) : ViewModel() {

    val targetSummary: StateFlow<String> = selectConsole.target
        .map { it.summary() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), selectConsole.target.value.summary())

    /** Re-posts the server notification once the user allows notifications. */
    fun onNotificationsAllowed() {
        if (server.isRunning.value) server.restartService()
    }

    private fun Client?.summary(): String =
        this?.let { "Target: ${it.name} (${it.type.name}) · ${it.ip}" } ?: "Target: none"
}
