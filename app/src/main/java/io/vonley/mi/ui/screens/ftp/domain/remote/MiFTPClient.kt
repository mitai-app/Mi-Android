package io.vonley.mi.ui.screens.ftp.domain.remote

import io.vonley.mi.ui.screens.ftp.domain.model.FtpConnection
import io.vonley.mi.ui.screens.ftp.domain.model.RemoteFile
import kotlinx.coroutines.flow.StateFlow
import java.io.File

interface MiFTPClient {
    val connection: StateFlow<FtpConnection>

    /** Connects to [ip] on the first open FTP port and lists the last visited directory. */
    suspend fun connect(ip: String): Boolean
    suspend fun disconnect()
    suspend fun open(dir: RemoteFile): Boolean
    suspend fun navigateTo(path: String): Boolean
    suspend fun navigateUp(): Boolean
    suspend fun refresh(): Boolean
    suspend fun upload(name: String, source: File): Boolean
    suspend fun delete(file: RemoteFile): Boolean
    suspend fun download(file: RemoteFile, destination: File): Boolean
    suspend fun rename(file: RemoteFile, newName: String): Boolean
}
