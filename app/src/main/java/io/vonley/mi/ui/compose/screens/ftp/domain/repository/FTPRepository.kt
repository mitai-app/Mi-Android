package io.vonley.mi.ui.compose.screens.ftp.domain.repository

import io.vonley.mi.common.Resource
import kotlinx.coroutines.flow.Flow
import org.apache.commons.net.ftp.FTPFile
import java.io.InputStream

interface FTPRepository {

    val currentPath: String

    fun navigateTo(ftpFile: FTPFile): Flow<Resource<FTPEvent>>
    fun navigateTo(path: String): Flow<Resource<FTPEvent>>
    fun delete(ftpFile: FTPFile): Flow<Resource<FTPEvent>>
    fun download(ftpFile: FTPFile): Flow<Resource<FTPEvent>>
    fun replace(ftpFile: FTPFile, stream: InputStream): Flow<Resource<FTPEvent>>
    fun upload(filename: String, stream: InputStream): Flow<Resource<FTPEvent>>
    fun rename(ftpFile: FTPFile, input: String): Flow<Resource<FTPEvent>>

    val TAG: String
        get() = FTPRepository::class.java.name
}