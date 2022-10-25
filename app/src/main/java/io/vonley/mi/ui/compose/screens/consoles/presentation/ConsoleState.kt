package io.vonley.mi.ui.compose.screens.consoles.presentation

import io.vonley.mi.ui.compose.screens.consoles.domain.model.Console

sealed class ConsoleState {
    data class Success(val repos: List<Console> = emptyList()): ConsoleState()
    data class Error(val error: String): ConsoleState()
    object Loading: ConsoleState()
}