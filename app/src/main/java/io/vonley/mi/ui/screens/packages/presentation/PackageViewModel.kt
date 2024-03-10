package io.vonley.mi.ui.screens.packages.presentation

import android.app.Application
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import io.vonley.mi.common.Resource
import io.vonley.mi.di.network.callbacks.PayloadCallback
import io.vonley.mi.models.Payload
import io.vonley.mi.ui.screens.packages.data.local.entity.Package
import io.vonley.mi.ui.screens.packages.domain.usecase.AddRepositoryUseCase
import io.vonley.mi.ui.screens.packages.domain.usecase.GetRepositoryUseCase
import io.vonley.mi.ui.screens.packages.domain.usecase.SendPackageUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class UploadState {
    data object Default : UploadState()
    data object OnSent : UploadState()
    data class Failed(val status: String, val message: String, val throwable: Throwable) :
        UploadState()

    data object Writing : UploadState()
    data object Finished : UploadState()
}


@HiltViewModel
class PackageViewModel @Inject constructor(
    private val repoUseCase: GetRepositoryUseCase,
    private val addRepoUseCase: AddRepositoryUseCase,
    private val sendPackageUseCase: SendPackageUseCase
) : ViewModel() {

    private val _repoState: MutableState<PackageState> = mutableStateOf(PackageState.Loading)
    private val _uploadState: MutableStateFlow<UploadState> = MutableStateFlow(UploadState.Default)

    val repoState: State<PackageState> get() = _repoState
    val uploadState: StateFlow<UploadState> get() = _uploadState


    init {
        getRepos()
    }

    fun getRepos() = repoUseCase().onEach { result ->
        when (result) {
            is Resource.Success -> {
                _repoState.value = PackageState.Success(result.data ?: emptyList())
            }

            is Resource.Error -> {
                _repoState.value =
                    PackageState.Error(error = result.status ?: "An unexpected error occurred")
            }

            is Resource.Loading -> {
                _repoState.value = PackageState.Loading
            }
        }
    }.launchIn(viewModelScope)


    fun searchRelevance(search: String) = repoUseCase(search).onEach { result ->
        when (result) {
            is Resource.Success -> {
                _repoState.value = PackageState.Success(result.data ?: emptyList())
            }

            is Resource.Error -> {
                _repoState.value =
                    PackageState.Error(error = result.status ?: "An unexpected error occurred")
            }

            is Resource.Loading -> {
                _repoState.value = PackageState.Loading
            }
        }
    }.launchIn(viewModelScope)


    fun addRepo(link: String) = addRepoUseCase(link).onEach { result ->
        var value = repoState.value
        when (result) {
            is Resource.Error -> {
                _repoState.value = PackageState.Error("Unable to add repo...")
            }

            is Resource.Loading -> {
                // Dont do anything
            }

            is Resource.Success -> {
                if (value is PackageState.Success) {
                    value = PackageState.Success(value.repos + result.data!!)
                }
                _repoState.value = value
            }
        }
    }.launchIn(viewModelScope)


    fun send(pkg: Package) = sendPackageUseCase(pkg, object : PayloadCallback {
        override fun onFinished() {
            _uploadState.update {
                UploadState.Finished
            }

            viewModelScope.launch {
                delay(5000)
                _uploadState.update {
                    UploadState.Default
                }
            }
        }

        override fun onWriting(payload: Payload) {
            _uploadState.update {
                UploadState.Writing
            }
        }

        override fun onSent(payload: Payload) {
            _uploadState.update {
                UploadState.OnSent
            }
        }

        override fun onPayloadFailed(payload: Payload) {
            _uploadState.update {
                UploadState.Failed(
                    "Error",
                    "Unable to send payload",
                    Throwable("Payload failed to send")
                )
            }
            viewModelScope.launch {
                delay(5000)
                _uploadState.update {
                    UploadState.Default
                }
            }
        }
    }).onEach {
        when (it) {
            is Resource.Error -> {
                _uploadState.update { state ->
                    UploadState.Failed(it.status!!, it.data!!, Throwable(it.data))
                }
            }
            is Resource.Loading -> Unit
            is Resource.Success -> Unit
        }
    }.launchIn(viewModelScope)


    class Factory(
        val application: Application,
        private val getRepoUseCase: GetRepositoryUseCase,
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return modelClass.getConstructor(
                Application::class.java,
                GetRepositoryUseCase::class.java,
            ).newInstance(application, getRepoUseCase)
        }
    }
}