package io.vonley.mi.ui.compose.screens.ftp.presentation

import io.vonley.mi.ui.compose.screens.ftp.domain.repository.FTPEvent
import org.apache.commons.net.ftp.FTPFile

sealed class FTPState {
    data class Success(
        val files: List<FTPFile> = emptyList(),
        val event: FTPEvent = FTPEvent.None
    ): FTPState()
    data class Error(val files: List<FTPFile>, val error: FTPEvent): FTPState()
    data class Loading(val files: List<FTPFile>, val event: FTPEvent): FTPState()
}