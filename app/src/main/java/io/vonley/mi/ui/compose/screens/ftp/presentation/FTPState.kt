package io.vonley.mi.ui.compose.screens.ftp.presentation

import io.vonley.mi.ui.compose.screens.packages.data.remote.dto.Repo

sealed class FTPState {
    data class Success(val repos: List<Repo> = emptyList()): FTPState()
    data class Error(val error: String): FTPState()
    object Loading: FTPState()
}