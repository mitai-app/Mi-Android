package io.vonley.mi.ui.screens.ftp.presentation

import io.vonley.mi.ui.screens.ftp.domain.repository.FTPEvent
import org.apache.commons.net.ftp.FTPFile

sealed class FTPState(open val files: List<FTPFile> = emptyList()) {
    data class Success(
        override val files: List<FTPFile>,
        val event: FTPEvent = FTPEvent.None
    ): FTPState(files)
    data class Error(override val files: List<FTPFile>, val error: FTPEvent, val message: String = ""): FTPState(files)
    data class Loading(override val files: List<FTPFile>, val event: FTPEvent): FTPState(files)
}