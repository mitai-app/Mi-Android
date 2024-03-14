package io.vonley.mi.ui.screens.ftp.domain.repository

import io.vonley.mi.common.Resource
import org.apache.commons.net.ftp.FTPFile
import java.io.InputStream

interface FTPRepository {

    val currentPath: String

    suspend fun navigateTo(ftpFile: FTPFile): Resource<FTPEvent>
    suspend fun navigateTo(path: String): Resource<FTPEvent>
    suspend fun delete(ftpFile: FTPFile): Resource<FTPEvent>
    suspend fun download(ftpFile: FTPFile): Resource<FTPEvent>
    suspend fun replace(ftpFile: FTPFile, stream: InputStream): Resource<FTPEvent>
    suspend fun upload(filename: String, stream: InputStream): Resource<FTPEvent>
    suspend fun rename(ftpFile: FTPFile, input: String): Resource<FTPEvent>

    val TAG: String
        get() = FTPRepository::class.java.name
}