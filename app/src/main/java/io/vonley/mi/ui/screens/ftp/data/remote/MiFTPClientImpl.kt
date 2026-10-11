package io.vonley.mi.ui.screens.ftp.data.remote

import io.vonley.mi.BuildConfig
import io.vonley.mi.extensions.e
import io.vonley.mi.models.enums.Feature
import io.vonley.mi.ui.screens.ftp.domain.model.FtpConnection
import io.vonley.mi.ui.screens.ftp.domain.model.RemoteFile
import io.vonley.mi.ui.screens.ftp.domain.remote.MiFTPClient
import io.vonley.mi.utils.SharedPreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.apache.commons.net.PrintCommandListener
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPFile
import org.apache.commons.net.ftp.FTPReply
import java.io.File
import java.io.PrintWriter

/**
 * Thin wrapper over commons-net's [FTPClient], which is not thread-safe: every
 * call goes through [mutex] on the IO dispatcher.
 */
class MiFTPClientImpl(
    private val manager: SharedPreferenceManager
) : MiFTPClient {

    private val mutex = Mutex()
    private val client = FTPClient().apply {
        connectTimeout = TIMEOUT_MS
        // The Duration overload needs API 26 (java.time); minSdk is 23.
        @Suppress("DEPRECATION")
        setDataTimeout(TIMEOUT_MS)
        if (BuildConfig.DEBUG) addProtocolCommandListener(PrintCommandListener(PrintWriter(System.out), true))
    }
    private var host: String? = null

    private val _connection = MutableStateFlow<FtpConnection>(FtpConnection.Disconnected)
    override val connection: StateFlow<FtpConnection> = _connection.asStateFlow()

    private suspend fun <T> locked(block: suspend () -> T): T =
        withContext(Dispatchers.IO) { mutex.withLock { block() } }

    override suspend fun connect(ip: String): Boolean = locked {
        if (client.isConnected && host == ip) return@locked listInto(client.printWorkingDirectory() ?: "/")
        closeQuietly()
        _connection.value = FtpConnection.Connecting
        for (port in Feature.FTP.ports.sortedDescending()) {
            try {
                client.connect(ip, port)
                if (!FTPReply.isPositiveCompletion(client.replyCode)) {
                    closeQuietly()
                    continue
                }
                // webMAN and GoldHEN both accept anonymous:anonymous by default.
                val user = manager.ftpUser.ifEmpty { ANONYMOUS }
                val pass = manager.ftpPass.ifEmpty { ANONYMOUS }
                if (!client.login(user, pass)) {
                    closeQuietly()
                    _connection.value = FtpConnection.Failed("Login rejected, check the FTP user and password in Settings")
                    return@locked false
                }
                client.enterLocalPassiveMode()
                client.setFileType(FTP.BINARY_FILE_TYPE)
                host = ip
                val start = manager.ftpPath
                return@locked (client.changeWorkingDirectory(start) && listInto(start)) || (client.changeWorkingDirectory("/") && listInto("/"))
            } catch (e: Throwable) {
                "FTP connect to $ip:$port failed: ${e.message}".e(TAG)
                closeQuietly()
            }
        }
        _connection.value = FtpConnection.Failed("No FTP server found on $ip. Enable FTP on the console.")
        false
    }

    override suspend fun disconnect() = locked {
        closeQuietly()
        _connection.value = FtpConnection.Disconnected
    }

    override suspend fun open(dir: RemoteFile): Boolean =
        if (dir.isDirectory) navigateTo(dir.name) else false

    override suspend fun navigateTo(path: String): Boolean = command {
        client.changeWorkingDirectory(path) && listInto(client.printWorkingDirectory() ?: path)
    }

    override suspend fun navigateUp(): Boolean = command {
        client.changeToParentDirectory() && listInto(client.printWorkingDirectory() ?: "/")
    }

    override suspend fun refresh(): Boolean = command {
        listInto(client.printWorkingDirectory() ?: "/")
    }

    override suspend fun upload(name: String, source: File): Boolean = command {
        val stored = source.inputStream().use { client.storeFile(name, it) }
        if (stored) listInto(client.printWorkingDirectory() ?: "/")
        stored
    }

    override suspend fun delete(file: RemoteFile): Boolean = command {
        val deleted = if (file.isDirectory) client.removeDirectory(file.name) else client.deleteFile(file.name)
        if (deleted) listInto(client.printWorkingDirectory() ?: "/")
        deleted
    }

    override suspend fun download(file: RemoteFile, destination: File): Boolean = command {
        if (file.isDirectory) return@command false
        val ok = destination.outputStream().use { client.retrieveFile(file.name, it) }
        if (!ok) destination.delete()
        ok
    }

    override suspend fun rename(file: RemoteFile, newName: String): Boolean = command {
        val renamed = client.rename(file.name, newName)
        if (renamed) listInto(client.printWorkingDirectory() ?: "/")
        renamed
    }

    /** Runs an operation on a connected session, reporting a dropped connection. */
    private suspend fun command(block: () -> Boolean): Boolean = locked {
        if (!client.isConnected) return@locked false
        try {
            block()
        } catch (e: Throwable) {
            "FTP command failed: ${e.message}".e(TAG, e)
            if (!client.isConnected) {
                closeQuietly()
                _connection.value = FtpConnection.Failed("Connection to the console was lost")
            }
            false
        }
    }

    private fun listInto(path: String): Boolean {
        val files = client.listFiles()
            ?.filter { it.name != "." && it.name != ".." }
            ?.map { it.toRemoteFile() }
            ?.sortedWith(compareByDescending<RemoteFile> { it.isDirectory }.thenBy { it.name.lowercase() })
            ?: return false
        manager.ftpPath = path
        _connection.value = FtpConnection.Connected(host.orEmpty(), path, files)
        return true
    }

    private fun closeQuietly() {
        try {
            if (client.isConnected) {
                runCatching { client.logout() }
                client.disconnect()
            }
        } catch (_: Throwable) {
        }
        host = null
    }

    private fun FTPFile.toRemoteFile() = RemoteFile(
        name = name,
        isDirectory = isDirectory,
        size = size,
        modifiedMillis = timestamp?.timeInMillis
    )

    companion object {
        const val TAG = "MiFTPClient"
        private const val TIMEOUT_MS = 5000
        private const val ANONYMOUS = "anonymous"
    }
}
