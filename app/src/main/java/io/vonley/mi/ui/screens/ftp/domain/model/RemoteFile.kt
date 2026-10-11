package io.vonley.mi.ui.screens.ftp.domain.model

/** A file or directory listed on the console's FTP server. */
data class RemoteFile(
    val name: String,
    val isDirectory: Boolean,
    val size: Long,
    val modifiedMillis: Long?,
)

sealed interface FtpConnection {
    data object Disconnected : FtpConnection
    data object Connecting : FtpConnection
    data class Connected(val host: String, val path: String, val files: List<RemoteFile>) : FtpConnection
    data class Failed(val message: String) : FtpConnection
}
