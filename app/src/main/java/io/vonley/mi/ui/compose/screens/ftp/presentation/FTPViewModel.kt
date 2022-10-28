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
import io.vonley.mi.di.network.MiFTPClient
import io.vonley.mi.extensions.e
import io.vonley.mi.ui.compose.screens.consoles.data.remote.SyncService
import io.vonley.mi.ui.compose.screens.ftp.domain.repository.FTPEvent
import io.vonley.mi.ui.compose.screens.ftp.domain.usecase.*
import io.vonley.mi.utils.SharedPreferenceManager
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
    val uploadUseCase: UploadUseCase
) : ViewModel(), Observer<Array<out FTPFile>> {

    private var _ftpDirs: MutableState<FTPState> = mutableStateOf(FTPState.Loading(emptyList(), FTPEvent.None))

    val ftpState: State<FTPState> get() = _ftpDirs

    init {
        initialize()
    }

    fun navigateTo(ftpFile: FTPFile) {
        navigateUseCase(ftpFile).onEach {

        }.launchIn(viewModelScope)
    }

    fun navigateTo(path: String) {
        navigateUseCase(path).onEach {
            when(it) {
                is Resource.Error -> TODO()
                is Resource.Loading -> TODO()
                is Resource.Success -> when(it.data) {
                    is FTPEvent.WorkingDir -> {

                    }
                    else -> {

                    }
                }
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
        deleteUseCase(ftpFile).onEach {
            when(it) {
                is Resource.Error -> handleError(it.status, it.data)
                is Resource.Loading -> handleLoading(it.status, it.data)
                is Resource.Success -> handleSuccess(it.status, it.data)
            }
        }.launchIn(viewModelScope)
    }

    fun download(ftpFile: FTPFile) {
        downloadUseCase(ftpFile).onEach {
            when(it) {
                is Resource.Error -> handleError(it.status, it.data)
                is Resource.Loading -> handleLoading(it.status, it.data)
                is Resource.Success -> handleSuccess(it.status, it.data)
            }
        }.launchIn(viewModelScope)
    }

    fun replace(ftpFile: FTPFile, stream: InputStream) {
        replaceUseCase(ftpFile, stream).onEach {
            when(it) {
                is Resource.Error -> handleError(it.status, it.data)
                is Resource.Loading -> handleLoading(it.status, it.data)
                is Resource.Success -> handleSuccess(it.status, it.data)
            }
        }.launchIn(viewModelScope)
    }

    fun upload(filename: String, stream: InputStream) {
        uploadUseCase(filename, stream).onEach {
            when(it) {
                is Resource.Error -> handleError(it.status, it.data)
                is Resource.Loading -> handleLoading(it.status, it.data)
                is Resource.Success -> handleSuccess(it.status, it.data)
            }
        }.launchIn(viewModelScope)
    }

    fun rename(ftpFile: FTPFile, input: String) {
        renameUseCase(ftpFile, input).onEach {
            when(it) {
                is Resource.Error -> handleError(it.status, it.data)
                is Resource.Loading -> handleLoading(it.status, it.data)
                is Resource.Success -> handleSuccess(it.status, it.data)
            }
        }.launchIn(viewModelScope)
    }


    private fun initialize() {
        ftp.cwd.removeObserver(this)
        ftp.cwd.observeForever(this)
        sync.target?.run {
            ftp.connect()
        } ?: run {
            //view.noTarget()
        }
    }

    fun cleanup() {
        if (ftp.cwd.hasObservers()) {
            ftp.cwd.removeObserver(this)
        }
        ftp.disconnect()
    }

    override fun onChanged(t: Array<out FTPFile>?) {
        t?.let {
            val state = ftpState.value
            if(state is FTPState.Success) {
                _ftpDirs.value = FTPState.Success(it.toList(), state.event)
            } else if (state is FTPState.Error){
                _ftpDirs.value = FTPState.Success(it.toList())
            }
        }
    }

    companion object {
        const val TAG = "FTPViewModel::class"
    }

}

