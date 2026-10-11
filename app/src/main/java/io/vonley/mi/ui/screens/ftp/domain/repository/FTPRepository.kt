package io.vonley.mi.ui.screens.ftp.domain.repository

import android.net.Uri
import io.vonley.mi.ui.screens.ftp.domain.model.FtpConnection
import io.vonley.mi.ui.screens.ftp.domain.model.RemoteFile
import kotlinx.coroutines.flow.StateFlow
import java.io.File

interface FTPRepository {
    val connection: StateFlow<FtpConnection>

    /** Connects to the selected console; false when no console is selected or reachable. */
    suspend fun connectToTarget(): Boolean
    suspend fun disconnect()
    suspend fun open(dir: RemoteFile): Boolean
    suspend fun navigateUp(): Boolean
    suspend fun refresh(): Boolean
    suspend fun upload(uri: Uri): String?
    suspend fun replace(file: RemoteFile, uri: Uri): Boolean
    suspend fun download(file: RemoteFile): File?
    suspend fun delete(file: RemoteFile): Boolean
    suspend fun rename(file: RemoteFile, newName: String): Boolean
}
