package io.vonley.mi.ui.compose.screens.ftp.data.repository

import io.vonley.mi.common.Resource
import io.vonley.mi.di.annotations.SharedPreferenceStorage
import io.vonley.mi.di.network.MiFTPClient
import io.vonley.mi.ui.compose.screens.consoles.data.remote.SyncService
import io.vonley.mi.ui.compose.screens.ftp.domain.repository.FTPEvent
import io.vonley.mi.ui.compose.screens.ftp.domain.repository.FTPRepository
import io.vonley.mi.utils.SharedPreferenceManager
import kotlinx.coroutines.flow.flow
import org.apache.commons.net.ftp.FTPFile
import java.io.InputStream
import javax.inject.Inject

class FTPRepositoryImpl @Inject constructor(
    private val ftp: MiFTPClient,
    private val sync: SyncService,
    @SharedPreferenceStorage private val manager: SharedPreferenceManager
) : FTPRepository {

    override val currentPath: String
        get() = manager.ftpPath ?: "/"

    override fun navigateTo(ftpFile: FTPFile) = flow<Resource<FTPEvent>> {
        if (ftpFile.isDirectory) {
            ftp.setWorkingDir(ftpFile)
            emit(Resource.Success(FTPEvent.WorkingDir(ftpFile.rawListing)))
        } else {
            emit(Resource.Error("Unable to set working dir because it is a file", FTPEvent.WorkingDir(ftpFile.rawListing)))
        }
    }

    override fun navigateTo(path: String) = flow<Resource<FTPEvent>> {
        ftp.setWorkingDir(path)
        emit(Resource.Success(FTPEvent.WorkingDir(path)))
    }

    override fun delete(ftpFile: FTPFile) = flow<Resource<FTPEvent>> {
        val delete = ftp.delete(ftpFile)
        val event = FTPEvent.Delete(filename = ftpFile.name)
        if (delete) {
            emit(Resource.Success(event))
        } else {
            emit(Resource.Error("Unable to delete", event))
        }
    }

    override fun download(ftpFile: FTPFile) = flow {
        val download = ftp.download(ftpFile)
        val event: Resource<FTPEvent> = download?.let {
            Resource.Success(FTPEvent.Download(filename = ftpFile.name, it))
        } ?: run {
            Resource.Error("Unable to download", FTPEvent.Download(filename = ftpFile.name))
        }
        emit(event)
    }

    override fun replace(
        ftpFile: FTPFile,
        stream: InputStream
    ) = flow<Resource<FTPEvent>> {
        val replaced = ftp.upload(ftpFile.name, stream)
        val replaceEvent = FTPEvent.Replace(filename = ftpFile.name)
        if (replaced) {
            emit(Resource.Success(replaceEvent))
        } else {
            emit(Resource.Error("Unable to replace file", replaceEvent))
        }
    }

    override fun upload(
        filename: String,
        stream: InputStream
    ) = flow<Resource<FTPEvent>> {
        val upload = ftp.upload(filename, stream)
        val event = FTPEvent.Upload(
            filename = filename
        )
        if (upload) {
            emit(Resource.Success(event))
        } else {
            emit(Resource.Error("Unable to upload file", event))
        }
    }

    override fun rename(
        ftpFile: FTPFile,
        input: String
    ) = flow<Resource<FTPEvent>> {
        val rename = ftp.rename(ftpFile, input)
        val upload = FTPEvent.Rename(
            originalFilename = ftpFile,
            toInputName = input
        )
        if (rename) {
            emit(Resource.Success(upload))
        } else {
            emit(Resource.Error("Unable to rename file", upload))
        }
    }

}