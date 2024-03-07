package io.vonley.mi.ui.screens.packages.presentation

import android.app.Application
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import io.vonley.mi.common.Resource
import io.vonley.mi.ui.screens.packages.domain.usecase.AddRepositoryUseCase
import io.vonley.mi.ui.screens.packages.domain.usecase.GetRepositoryUseCase
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class PackageViewModel @Inject constructor(
    private val repoUseCase: GetRepositoryUseCase,
    private val addRepoUseCase: AddRepositoryUseCase
) : ViewModel() {

    private val _repoState: MutableState<PackageState> = mutableStateOf(PackageState.Loading)
    val repoState: State<PackageState> get() = _repoState

    init {
        getRepos()
    }

    fun getRepos() {
        repoUseCase().onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _repoState.value = PackageState.Success(result.data ?: emptyList())
                }
                is Resource.Error -> {
                    _repoState.value = PackageState.Error(error = result.status ?: "An unexpected error occurred")
                }
                is Resource.Loading -> {
                    _repoState.value = PackageState.Loading
                }
            }
        }.launchIn(viewModelScope)
    }

    fun searchRelevance(search: String) {
        repoUseCase(search).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _repoState.value = PackageState.Success(result.data ?: emptyList())
                }
                is Resource.Error -> {
                    _repoState.value = PackageState.Error(error = result.status ?: "An unexpected error occurred")
                }
                is Resource.Loading -> {
                    _repoState.value = PackageState.Loading
                }
            }
        }.launchIn(viewModelScope)
    }

    fun addRepo(link: String) {
        addRepoUseCase(link).onEach { result ->
            var value = repoState.value
            when(result) {
                is Resource.Error -> {
                    _repoState.value = PackageState.Error("Unable to add repo...")
                }
                is Resource.Loading -> {
                    // Dont do anything
                }
                is Resource.Success -> {
                    if(value is PackageState.Success) {
                        value = PackageState.Success(value.repos + result.data!!)
                    }
                    _repoState.value = value
                }
            }
        }
    }

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