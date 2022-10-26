package io.vonley.mi.ui.compose.screens.ftp.presentation

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.vonley.mi.di.annotations.SharedPreferenceStorage
import io.vonley.mi.di.network.MiFTPClient
import io.vonley.mi.ui.compose.screens.consoles.data.remote.SyncService
import io.vonley.mi.ui.main.ftp.FTPPresenter
import io.vonley.mi.utils.SharedPreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.net.ftp.FTPFile
import java.io.InputStream
import javax.inject.Inject


@HiltViewModel
class FTPViewModel @Inject constructor(
    @SharedPreferenceStorage val manager: SharedPreferenceManager,
    val ftp: MiFTPClient,
    val sync: SyncService,
) : ViewModel() {

    private var _ftpDirs: MutableState<FTPState> = mutableStateOf(FTPState.Loading)

    val ftpState: State<FTPState> get() = _ftpDirs

    init {

    }

    val currentPath: String
        get() = manager.ftpPath ?:"/"

    fun navigateTo(ftpFile: FTPFile) {
        if (ftpFile.isDirectory) {
            ftp.setWorkingDir(ftpFile)
        }
    }

    fun navigateTo(path: String) {
        ftp.setWorkingDir(path)
    }

    fun delete(ftpFile: FTPFile) {
        launch {
            val delete = ftp.delete(ftpFile)
            val event = Event.DELETE.apply {
                filename = ftpFile.name
            }
            withContext(Dispatchers.Main) {
                if (delete) {
                    view.onFTPEventCompleted(event)
                } else {
                    view.onFTPEventFailed(event)
                }
            }
        }
    }

    fun download(ftpFile: FTPFile) {
        launch {
            val event = Event.DOWNLOAD.apply {
                filename = ftpFile.name
            }
            val download = ftp.download(ftpFile)
            withContext(Dispatchers.Main) {
                download?.let {
                    event.data = it
                    view.onFTPEventCompleted(event)
                } ?: run {
                    view.onFTPEventFailed(event)
                }
            }
        }
    }

    enum class Event(var filename: String, var data: Any? = null) {
        DELETE(""),
        DOWNLOAD(""),
        RENAME(""),
        REPLACE(""),
        UPLOAD("")
    }

    fun replace(ftpFile: FTPFile, stream: InputStream) {
        launch {
            val replaced = ftp.upload(ftpFile.name, stream)
            val replaceEvent = Event.REPLACE.apply {
                filename = ftpFile.name
            }
            withContext(Dispatchers.Main) {
                if (replaced) {
                    view.onFTPEventCompleted(replaceEvent)
                } else {
                    view.onFTPEventFailed(replaceEvent)
                }
            }
        }
    }

    fun upload(filename: String, stream: InputStream) {
        launch {
            val upload = ftp.upload(filename, stream)
            val event = Event.UPLOAD.apply {
                this.filename = filename
            }
            withContext(Dispatchers.Main) {
                if (upload) {
                    view.onFTPEventCompleted(event)
                } else {
                    view.onFTPEventFailed(event)
                }
            }
        }
    }

    fun rename(ftpFile: FTPFile, input: String) {
        launch {
            val rename = ftp.rename(ftpFile, input)
            val upload = Event.RENAME.apply {
                filename = input
                data = ftpFile
            }
            withContext(Dispatchers.Main) {
                if (rename) {
                    view.onFTPEventCompleted(upload)
                } else {
                    view.onFTPEventFailed(upload)
                }
            }
        }
    }

    fun init() {
        if (!ftp.cwd.hasObservers()) {
            ftp.cwd.observeForever(this)
        }
        sync.target?.run {
            ftp.connect()
        } ?: run {
            view.noTarget()
        }
    }

    fun cleanup() {
        if (ftp.cwd.hasObservers()) {
            ftp.cwd.removeObserver(this)
            ftp.disconnect()
        }
    }

    val TAG: String
        get() = FTPPresenter::class.java.name

    fun onChanged(t: Array<out FTPFile>?) {
        t?.let {
            view.onFTPDirOpened(it)
        } ?: run {
            //TODO nothing?
        }
    }
}

