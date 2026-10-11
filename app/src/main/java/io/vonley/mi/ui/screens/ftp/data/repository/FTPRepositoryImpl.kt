package io.vonley.mi.ui.screens.ftp.data.repository

import android.net.Uri
import io.vonley.mi.common.files.LocalFileSource
import io.vonley.mi.ui.screens.consoles.domain.remote.SyncService
import io.vonley.mi.ui.screens.ftp.domain.model.FtpConnection
import io.vonley.mi.ui.screens.ftp.domain.model.RemoteFile
import io.vonley.mi.ui.screens.ftp.domain.remote.MiFTPClient
import io.vonley.mi.ui.screens.ftp.domain.repository.FTPRepository
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import javax.inject.Inject

class FTPRepositoryImpl @Inject constructor(
    private val ftp: MiFTPClient,
    private val sync: SyncService,
    private val files: LocalFileSource,
) : FTPRepository {

    override val connection: StateFlow<FtpConnection> get() = ftp.connection

    override suspend fun connectToTarget(): Boolean {
        val ip = sync.target?.ip ?: return false
        return ftp.connect(ip)
    }

    override suspend fun disconnect() = ftp.disconnect()

    override suspend fun open(dir: RemoteFile): Boolean = ftp.open(dir)

    override suspend fun navigateUp(): Boolean = ftp.navigateUp()

    override suspend fun refresh(): Boolean = ftp.refresh()

    override suspend fun upload(uri: Uri): String? {
        val staged = files.stage(uri)
        return try {
            staged.name.takeIf { ftp.upload(staged.name, staged.file) }
        } finally {
            staged.file.delete()
        }
    }

    override suspend fun replace(file: RemoteFile, uri: Uri): Boolean {
        val staged = files.stage(uri)
        return try {
            ftp.upload(file.name, staged.file)
        } finally {
            staged.file.delete()
        }
    }

    override suspend fun download(file: RemoteFile): File? {
        val target = files.downloadTarget(file.name)
        return target.takeIf { ftp.download(file, it) }
    }

    override suspend fun delete(file: RemoteFile): Boolean = ftp.delete(file)

    override suspend fun rename(file: RemoteFile, newName: String): Boolean = ftp.rename(file, newName)
}
