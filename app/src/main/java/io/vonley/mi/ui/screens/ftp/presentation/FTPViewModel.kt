package io.vonley.mi.ui.screens.ftp.presentation

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.vonley.mi.ui.screens.consoles.domain.usecase.SelectConsoleUseCase
import io.vonley.mi.ui.screens.ftp.domain.model.FtpConnection
import io.vonley.mi.ui.screens.ftp.domain.model.RemoteFile
import io.vonley.mi.ui.screens.ftp.domain.repository.FTPRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FTPUiState(
    val hasTarget: Boolean = false,
    val connection: FtpConnection = FtpConnection.Disconnected,
    val busy: Boolean = false,
)

@HiltViewModel
class FTPViewModel @Inject constructor(
    private val ftp: FTPRepository,
    private val selectConsole: SelectConsoleUseCase,
) : ViewModel() {

    private val busy = MutableStateFlow(false)
    private val messageChannel = Channel<String>(Channel.BUFFERED)
    val messages: Flow<String> = messageChannel.receiveAsFlow()

    val state: StateFlow<FTPUiState> = combine(selectConsole.target, ftp.connection, busy) { target, connection, busy ->
        FTPUiState(hasTarget = target != null, connection = connection, busy = busy)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FTPUiState())

    init {
        // Reconnect whenever a different console becomes the target.
        viewModelScope.launch {
            var lastIp: String? = null
            selectConsole.target.collect { target ->
                if (target != null && target.ip != lastIp) {
                    lastIp = target.ip
                    connect()
                }
            }
        }
    }

    fun connect() = operation { ftp.connectToTarget() }

    fun open(file: RemoteFile) = operation { ftp.open(file) }

    fun navigateUp() = operation { ftp.navigateUp() }

    fun refresh() = operation { ftp.refresh() }

    fun upload(uri: Uri) = operation {
        val name = ftp.upload(uri)
        messageChannel.send(name?.let { "$it uploaded" } ?: "Upload failed")
        name != null
    }

    fun replace(file: RemoteFile, uri: Uri) = operation {
        val ok = ftp.replace(file, uri)
        messageChannel.send(if (ok) "${file.name} replaced" else "Failed to replace ${file.name}")
        ok
    }

    fun download(file: RemoteFile) = operation {
        val saved = ftp.download(file)
        messageChannel.send(saved?.let { "Saved to ${it.absolutePath}" } ?: "Failed to download ${file.name}")
        saved != null
    }

    fun delete(file: RemoteFile) = operation {
        val ok = ftp.delete(file)
        messageChannel.send(if (ok) "${file.name} deleted" else "Failed to delete ${file.name}")
        ok
    }

    fun rename(file: RemoteFile, newName: String) = operation {
        val ok = ftp.rename(file, newName)
        messageChannel.send(if (ok) "Renamed ${file.name} to $newName" else "Failed to rename ${file.name}")
        ok
    }

    private fun operation(block: suspend () -> Boolean) {
        viewModelScope.launch {
            busy.value = true
            try {
                block()
            } catch (e: Throwable) {
                messageChannel.send(e.message ?: "Something went wrong")
            } finally {
                busy.value = false
            }
        }
    }
}
