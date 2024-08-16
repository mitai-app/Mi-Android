package io.vonley.mi.ui.screens.ftp.data.remote

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.vonley.mi.BuildConfig
import io.vonley.mi.di.annotations.SharedPreferenceStorage
import io.vonley.mi.ui.screens.ftp.domain.remote.MiFTPClient
import io.vonley.mi.models.enums.Feature
import io.vonley.mi.ui.screens.consoles.domain.remote.SyncService
import io.vonley.mi.utils.SharedPreferenceManager
import io.vonley.mi.utils.set
import kotlinx.coroutines.*
import org.apache.commons.net.ProtocolCommandEvent
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPConnectionClosedException
import org.apache.commons.net.ftp.FTPFile
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.InetSocketAddress
import java.net.Socket

class MiFTPClientImpl constructor(
    @SharedPreferenceStorage override val manager: SharedPreferenceManager,
    override val sync: SyncService
) : MiFTPClient {

    override val connected: LiveData<Boolean>
        get() = _connected

    override val job: Job = Job()

    private var client: FTPClient = FTPClient()
    private val ftpPath get() = manager.ftpPath
    private val ftpUser get() = manager.ftpUser
    private val ftpPass get() = manager.ftpPass
    private var _ip: String? = null
    private var _port: Int? = null

    private val _cwd: MutableLiveData<Array<out FTPFile>> = MutableLiveData<Array<out FTPFile>>()
    private val _connected: MutableLiveData<Boolean> = MutableLiveData<Boolean>(false)

    companion object {
        const val TAG = ".MiFTPClientImpl"
    }

    private val callback = object : MiFTPEventListener {

        override fun onFailedToConnect() {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "Unable to connect")
            }
        }

        override fun onClosed() {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "ftp connection closed")
            }
        }

        override fun onNoSelectedTarget() {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "No Selected Target")
            }
        }

        override fun onLoggedIn() {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "User is logged in")
            }
        }

        override fun onInvalidCredentials() {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "User entered invalid credentials")
            }
        }

        override fun onDirChanged() {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "Directory Changed")
            }
        }

        override fun isLoggedInAlready() {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "User is already logged in")
            }
        }

    }

    interface MiFTPEventListener {
        fun onLoggedIn()
        fun onInvalidCredentials()
        fun onDirChanged()
        fun isLoggedInAlready()
        fun onFailedToConnect()
        fun onClosed()
        fun onNoSelectedTarget()
    }

    private suspend fun _connect(ip: String, port: Int) {
        try {
            client.connect(ip, port)
            client.addProtocolCommandListener(this@MiFTPClientImpl)
            val login = client.login(ftpUser, ftpPass)
            if (login) {
                setWorkingDir(ftpPath)
                client.setFileType(FTP.BINARY_FILE_TYPE)
                callback.onLoggedIn()
                withContext(Dispatchers.Main) {
                    synchronized(this) {
                        _connected.postValue(client.isConnected)
                    }
                }
            } else {
                callback.onInvalidCredentials()
            }
        } catch (e: Throwable) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, e.message ?: "Something went wrong")
            }
            callback.onFailedToConnect()
        }
    }

    override fun connect() {
        sync.target?.let { target ->
            launch {
                Feature.FTP.ports.filter { port ->
                    try {
                        val socket = Socket()
                        socket.connect(InetSocketAddress(target.ip, port))
                        socket.close()
                        return@filter true
                    } catch (e: Throwable) {
                        return@filter false
                    }
                }.firstOrNull()?.let { port ->
                    withContext(Dispatchers.Main) {
                        connect(target.ip, port)
                    }
                } ?: run {
                    callback.onFailedToConnect()
                }
            }
        } ?: run {
            callback.onNoSelectedTarget()
        }
    }

    override fun connect(ip: String, port: Int) {
        if (_ip == null || _port == null) {
            _ip = ip
            _port = port
        } else if (_ip != ip || _port != port) {
            if (client.isConnected) {
                client.disconnect()
            }
        }
        val block: suspend CoroutineScope.() -> Unit = {
            if (client.isConnected) {
                if (_ip != ip || _port != port) {
                    _ip = ip
                    _port = port
                    _connect(ip, port)
                } else {
                    callback.isLoggedInAlready()
                }
            } else {
                _ip = ip
                _port = port
                _connect(ip, port)
            }
        }
        launch(block = block)
    }

    override val cwd: LiveData<Array<out FTPFile>> get() = _cwd

    override suspend fun setWorkingDir(ftpFile: FTPFile): Boolean {
        if (ftpFile.isDirectory) {
            return setWorkingDir(ftpFile.name)
        }
        return false
    }

    override suspend fun setWorkingDir(dir: String?): Boolean {
        if (BuildConfig.DEBUG) {
            Log.e(TAG, "dir: $dir")
        }
        try {
            val changed = client.changeWorkingDirectory(dir)
            client.printWorkingDirectory()?.let {
                withContext(Dispatchers.Main) {
                    synchronized(this) {
                        manager[SharedPreferenceManager.FTPPATH] = it
                    }
                }
            }
            if (changed) {
                getGWD()
                callback.onDirChanged()
            }
            return changed
        } catch (e: FTPConnectionClosedException) {
            if (BuildConfig.DEBUG) {
                callback.onClosed()
            }
        } catch (e: Throwable) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, e.message ?: "Something went wrong")
            }
        }
        return false
    }

    private suspend fun getGWD() {
        try {
            if (BuildConfig.DEBUG) {
                val cwm = client.printWorkingDirectory()
                Log.e("CWD", "CWD: $cwm")
            }
            val dir: Array<out FTPFile> = if (client.isConnected) {
                val listFiles = client.listFiles()
                listFiles.sortByDescending { it.isDirectory }
                listFiles
            } else arrayOf()
            withContext(Dispatchers.Main) {
                synchronized(_cwd) {
                    _cwd.postValue(dir)
                }
            }
        } catch (e: Throwable) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, e.message ?: "Something went wrong")
            }
        }
    }

    override suspend fun upload(file: String, stream: InputStream): Boolean {
        try {
            if (client.isConnected) {
                client.enterLocalPassiveMode()
                val uploaded = client.storeFile(file, stream)
                withContext(Dispatchers.IO) {
                    stream.close()
                }
                if (uploaded) {
                    getGWD()
                }
                return uploaded
            }
            return false
        } catch (e: Throwable) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, e.message ?: "Something went wrong")
            }
        }
        return false
    }

    override fun disconnect() {
        launch {
            try {
                client.removeProtocolCommandListener(this@MiFTPClientImpl)
                client.logout()
                client.disconnect()
                withContext(Dispatchers.Main) {
                    synchronized(this) {
                        _connected.postValue(client.isConnected)
                    }
                }
            } catch (e: Throwable) {
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, e.message ?: "Something went wrong")
                }
            }
        }
    }

    override suspend fun delete(file: FTPFile): Boolean {
        try {
            if (client.isConnected) {
                if (file.isFile) {
                    val deleteFile = client.deleteFile("${ftpPath}/${file.name}")
                    getGWD()
                    return deleteFile
                }
            }
            return false
        } catch (e: Throwable) {

        }
        return false
    }

    override suspend fun download(ftpFile: FTPFile): ByteArray? {
        try {
            if (client.isConnected) {
                if (ftpFile.isFile) {
                    val remoteFile = "$ftpPath/${ftpFile.name}"
                    val stream = ByteArrayOutputStream()
                    val success: Boolean = client.retrieveFile(remoteFile, stream)
                    val bytes = stream.toByteArray()
                    stream.close()
                    if (success) {
                        return bytes
                    }
                }
            }
        } catch (e: Throwable) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, e.message ?: "Something went wrong")
            }
        }
        return null
    }

    override suspend fun rename(ftpFile: FTPFile, input: String): Boolean {
        if (client.isConnected) {
            if (ftpFile.isFile) {
                val remoteFile = "$ftpPath/${ftpFile.name}"
                val toRemoteFile = "$ftpPath/$input"
                val rename = client.rename(remoteFile, toRemoteFile)
                getGWD()
                return rename
            }
        }
        return false
    }

    override suspend fun getDir(): String? {
        if (client.isConnected) {
            return client.printWorkingDirectory()
        }
        return null
    }

    override fun protocolCommandSent(event: ProtocolCommandEvent?) {
        if (BuildConfig.DEBUG) {
            Log.e(TAG, "SENT: ${event?.message}")
        }
    }

    override fun protocolReplyReceived(event: ProtocolCommandEvent?) {
        if (BuildConfig.DEBUG) {
            Log.e(TAG, "RCV: ${event?.message}")
        }
    }


}