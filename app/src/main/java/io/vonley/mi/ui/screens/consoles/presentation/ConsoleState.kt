package io.vonley.mi.ui.screens.consoles.presentation

import io.vonley.mi.ui.screens.consoles.domain.model.Console

sealed class ConsoleState {
    data class Success(val repos: List<Console> = emptyList()): ConsoleState()
    data class Error(val error: String): ConsoleState()
    object Loading: ConsoleState()
}