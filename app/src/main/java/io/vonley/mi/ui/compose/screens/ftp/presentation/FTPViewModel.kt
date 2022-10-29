package io.vonley.mi.ui.compose.screens.ftp.presentation

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.vonley.mi.common.Resource
import io.vonley.mi.di.annotations.SharedPreferenceStorage
import io.vonley.mi.ui.compose.screens.ftp.domain.remote.MiFTPClient
import io.vonley.mi.extensions.e
import io.vonley.mi.ui.compose.screens.consoles.domain.remote.SyncService
import io.vonley.mi.ui.compose.screens.ftp.domain.repository.FTPEvent
import io.vonley.mi.ui.compose.screens.ftp.domain.usecase.*
import io.vonley.mi.utils.SharedPreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.apache.commons.net.ftp.FTPFile
import java.io.InputStream
import javax.inject.Inject


@HiltViewModel
class FTPViewModel @Inject constructor(
    @SharedPreferenceStorage val manager: SharedPreferenceManager,
    val ftp: MiFTPClient,
    val sync: SyncService,
    val downloadUseCase: DownloadUseCase,
    val deleteUseCase: DeleteUseCase,
    val navigateUseCase: NavigateUseCase,
    val renameUseCase: RenameUseCase,
    val replaceUseCase: ReplaceUseCase,
    val uploadUseCase: UploadUseCase,
) : ViewModel() {

    private val connectedObserver: Observer<in Boolean> = Observer<Boolean> { connected ->
        if (connected) {

        } else {

        }
    }

    private val dirObserver: Observer<Array<out FTPFile>> =  Observer<Array<out FTPFile>> { files ->
        val state = ftpState.value
        when (state) {
            is FTPState.Success -> {
                _ftpDirs.value = FTPState.Success(files.toList(), state.event)
            }
            is FTPState.Error -> {
                _ftpDirs.value = FTPState.Success(files.toList())
            }
            is FTPState.Loading -> {
                _ftpDirs.value = FTPState.Success(files.toList())
            }
        }
    }
    private var _ftpDirs: MutableState<FTPState> = mutableStateOf(FTPState.Loading(emptyList(), FTPEvent.None))
    private var _connected: MutableState<Boolean> = mutableStateOf(false)

    val ftpState: State<FTPState> get() = _ftpDirs

    val connected: State<Boolean> get() = _connected

    init {
        initialize()
    }

    fun navigateTo(ftpFile: FTPFile) {
        navigateUseCase(ftpFile).flowOn(Dispatchers.IO).onEach {
            when(it) {
                is Resource.Error -> handleError(it.status, it.data)
                is Resource.Loading -> handleLoading(it.status, it.data)
                is Resource.Success -> handleSuccess(it.status, it.data)
            }
        }.launchIn(viewModelScope)
    }

    fun navigateTo(path: String) {
        navigateUseCase(path).flowOn(Dispatchers.IO).onEach {
            when(it) {
                is Resource.Error -> handleError(it.status, it.data)
                is Resource.Loading -> handleLoading(it.status, it.data)
                is Resource.Success -> handleSuccess(it.status, it.data)
            }
        }.launchIn(viewModelScope)
    }

    private fun handleLoading(status: String?, data: FTPEvent?) {
        val state = ftpState.value
        when(data) {
            null -> {
                "Why are you null?".e(TAG)
            }
            else -> {
                if(state is FTPState.Success) {
                    _ftpDirs.value = FTPState.Loading(state.files, data)
                } else if(state is FTPState.Loading) {
                    _ftpDirs.value = FTPState.Loading((ftp.cwd.value?: emptyArray()).toList(), data)
                }
                else if (state is FTPState.Error){
                    _ftpDirs.value = FTPState.Loading((ftp.cwd.value?: emptyArray()).toList(), data)
                }
            }
        }
    }

    private fun handleSuccess(status: String?, data: FTPEvent?) {
        val state = ftpState.value
        when(data) {
            null -> {
                "Why are you null?".e(TAG)
            }
            else -> {
                if(state is FTPState.Success) {
                    _ftpDirs.value = FTPState.Success(state.files, data)
                } else if(state is FTPState.Loading) {
                    _ftpDirs.value = FTPState.Success((ftp.cwd.value?: emptyArray()).toList(), data)
                }
                else if (state is FTPState.Error){
                    _ftpDirs.value = FTPState.Success((ftp.cwd.value?: emptyArray()).toList(), data)
                }
            }
        }
    }

    private fun handleError(status: String?, data: FTPEvent?) {
        val state = ftpState.value
        when(data) {
            null -> {
                "Why are you null?".e(TAG)
            }
            else -> {
                if(state is FTPState.Success) {
                    _ftpDirs.value = FTPState.Error(state.files, data)
                } else if(state is FTPState.Loading) {
                    _ftpDirs.value = FTPState.Error((ftp.cwd.value?: emptyArray()).toList(), data)
                }
                else if (state is FTPState.Error){
                    _ftpDirs.value = FTPState.Error((ftp.cwd.value?: emptyArray()).toList(), data)
                }
            }
        }
    }

    fun delete(ftpFile: FTPFile) {
        deleteUseCase(ftpFile).flowOn(Dispatchers.IO).onEach {
            when(it) {
                is Resource.Error -> handleError(it.status, it.data)
                is Resource.Loading -> handleLoading(it.status, it.data)
                is Resource.Success -> handleSuccess(it.status, it.data)
            }
        }.launchIn(viewModelScope)
    }

    fun download(ftpFile: FTPFile) {
        downloadUseCase(ftpFile).flowOn(Dispatchers.IO).onEach {
            when(it) {
                is Resource.Error -> handleError(it.status, it.data)
                is Resource.Loading -> handleLoading(it.status, it.data)
                is Resource.Success -> handleSuccess(it.status, it.data)
            }
        }.launchIn(viewModelScope)
    }

    fun replace(ftpFile: FTPFile, stream: InputStream) {
        replaceUseCase(ftpFile, stream).flowOn(Dispatchers.IO).onEach {
            when(it) {
                is Resource.Error -> handleError(it.status, it.data)
                is Resource.Loading -> handleLoading(it.status, it.data)
                is Resource.Success -> handleSuccess(it.status, it.data)
            }
        }.launchIn(viewModelScope)
    }

    fun upload(filename: String, stream: InputStream) {
        uploadUseCase(filename, stream).flowOn(Dispatchers.IO).onEach {
            when(it) {
                is Resource.Error -> handleError(it.status, it.data)
                is Resource.Loading -> handleLoading(it.status, it.data)
                is Resource.Success -> handleSuccess(it.status, it.data)
            }
        }.launchIn(viewModelScope)
    }

    fun rename(ftpFile: FTPFile, input: String) {
        renameUseCase(ftpFile, input).flowOn(Dispatchers.IO).onEach {
            when(it) {
                is Resource.Error -> handleError(it.status, it.data)
                is Resource.Loading -> handleLoading(it.status, it.data)
                is Resource.Success -> handleSuccess(it.status, it.data)
            }
        }.launchIn(viewModelScope)
    }

    private fun initialize() {
        ftp.cwd.removeObserver(dirObserver)
        ftp.cwd.observeForever(dirObserver)
        ftp.connected.removeObserver(connectedObserver)
        ftp.connected.observeForever(connectedObserver)
    }

    fun connect() {
        sync.target?.run {
            ftp.connect()
        } ?: run {
            _ftpDirs.value = FTPState.Error(emptyList(), FTPEvent.None, message = "Please connect to a target.")
        }
    }

    fun cleanup() {
        if (ftp.cwd.hasObservers()) {
            ftp.cwd.removeObserver(dirObserver)
        }
        if(ftp.connected.hasObservers()) {
            ftp.connected.removeObserver(connectedObserver)
        }
        ftp.disconnect()
    }

    companion object {
        const val TAG = "FTPViewModel::class"
    }

}

