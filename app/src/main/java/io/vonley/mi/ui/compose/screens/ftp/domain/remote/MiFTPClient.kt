package io.vonley.mi.ui.compose.screens.ftp.domain.remote

import androidx.lifecycle.LiveData
import io.vonley.mi.ui.compose.screens.consoles.domain.remote.SyncService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import io.vonley.mi.utils.SharedPreferenceManager
import org.apache.commons.net.ProtocolCommandListener
import org.apache.commons.net.ftp.FTPFile
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import kotlin.coroutines.CoroutineContext

interface MiFTPClient : CoroutineScope, ProtocolCommandListener {
    val connected: LiveData<Boolean>
    val job: Job
    val sync: SyncService
    val manager: SharedPreferenceManager
    override val coroutineContext: CoroutineContext get() = Dispatchers.IO + job
    val cwd: LiveData<Array<out FTPFile>>
    fun connect(ip: String, port: Int = 2121)
    fun connect()
    suspend fun setWorkingDir(dir: String?): Boolean
    suspend fun setWorkingDir(ftpFile: FTPFile): Boolean
    suspend fun upload(file: String, byteArray: InputStream): Boolean
    suspend fun upload(file: String, byteArray: ByteArray): Boolean = upload(file, ByteArrayInputStream(byteArray))
    suspend fun upload(file: File): Boolean = upload(file.name, FileInputStream(file))
    suspend fun delete(file: FTPFile): Boolean
    suspend fun download(ftpFile: FTPFile): ByteArray?
    suspend fun rename(ftpFile: FTPFile, input: String): Boolean
    suspend fun getDir(): String?
    fun disconnect()

}