package io.vonley.mi.ui.compose.screens.ftp.data.repository

import io.vonley.mi.common.Resource
import io.vonley.mi.di.annotations.SharedPreferenceStorage
import io.vonley.mi.ui.compose.screens.ftp.domain.remote.MiFTPClient
import io.vonley.mi.ui.compose.screens.ftp.domain.repository.FTPEvent
import io.vonley.mi.ui.compose.screens.ftp.domain.repository.FTPRepository
import io.vonley.mi.utils.SharedPreferenceManager
import org.apache.commons.net.ftp.FTPFile
import java.io.InputStream
import javax.inject.Inject

class FTPRepositoryImpl @Inject constructor(
    private val ftp: MiFTPClient,
    @SharedPreferenceStorage private val manager: SharedPreferenceManager
) : FTPRepository {

    override val currentPath: String
        get() = manager.ftpPath ?: "/"

    override suspend fun navigateTo(ftpFile: FTPFile): Resource<FTPEvent> {
        return if (ftpFile.isDirectory) {
            ftp.setWorkingDir(ftpFile)
            return ftp.getDir()?.let { dir ->
                Resource.Success(FTPEvent.WorkingDir(dir))
            }?: run {
                Resource.Error("Are you connected to the network?", FTPEvent.WorkingDir(ftpFile.rawListing))
            }
        } else {
            Resource.Error("Unable to set working dir because it is a file", FTPEvent.WorkingDir(ftpFile.rawListing))
        }
    }

    override suspend fun navigateTo(path: String): Resource<FTPEvent> {
        ftp.setWorkingDir(path)
        return Resource.Success(FTPEvent.WorkingDir(path))
    }

    override suspend fun delete(ftpFile: FTPFile): Resource<FTPEvent> {
        val delete = ftp.delete(ftpFile)
        val event = FTPEvent.Delete(filename = ftpFile.name)
        return if (delete) {
            Resource.Success(event)
        } else {
            Resource.Error("Unable to delete", event)
        }
    }

    override suspend fun download(ftpFile: FTPFile): Resource<FTPEvent> {
        val download = ftp.download(ftpFile)
        val event: Resource<FTPEvent> = download?.let {
            Resource.Success(FTPEvent.Download(filename = ftpFile.name, it))
        } ?: run {
            Resource.Error("Unable to download", FTPEvent.Download(filename = ftpFile.name))
        }
        return event
    }

    override suspend fun replace(
        ftpFile: FTPFile,
        stream: InputStream
    ): Resource<FTPEvent> {
        val replaced = ftp.upload(ftpFile.name, stream)
        val replaceEvent = FTPEvent.Replace(filename = ftpFile.name)
        return if (replaced) {
            Resource.Success(replaceEvent)
        } else {
            Resource.Error("Unable to replace file", replaceEvent)
        }
    }

    override suspend fun upload(
        filename: String,
        stream: InputStream
    ): Resource<FTPEvent> {
        val upload = ftp.upload(filename, stream)
        val event = FTPEvent.Upload(
            filename = filename
        )
        return if (upload) {
            Resource.Success(event)
        } else {
            Resource.Error("Unable to upload file", event)
        }
    }

    override suspend fun rename(
        ftpFile: FTPFile,
        input: String
    ): Resource<FTPEvent> {
        val rename = ftp.rename(ftpFile, input)
        val upload = FTPEvent.Rename(
            originalFilename = ftpFile,
            toInputName = input
        )
        return if (rename) {
            Resource.Success(upload)
        } else {
            Resource.Error("Unable to rename file", upload)
        }
    }

}