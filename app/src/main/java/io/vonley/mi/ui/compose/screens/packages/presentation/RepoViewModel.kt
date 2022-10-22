package io.vonley.mi.ui.compose.screens.packages.presentation

import android.app.Application
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import io.vonley.mi.common.Resource
import io.vonley.mi.ui.compose.screens.packages.domain.usecase.GetRepositoryUseCase
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class RepoViewModel @Inject constructor(
    private val repoUseCase: GetRepositoryUseCase,
) : ViewModel() {

    private val _newThisMonth = MutableLiveData<RepoState>()
    val newThisMonth: LiveData<RepoState> get() = _newThisMonth

    init {
        getComics()
    }

    fun getComics() {
        repoUseCase().onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _newThisMonth.value = RepoState(false, result.data ?: emptyList())
                }
                is Resource.Error -> {
                    _newThisMonth.value = RepoState(error = result.status ?: "An unexpected error occurred")
                }
                is Resource.Loading -> {
                    _newThisMonth.value = RepoState(loading = true)
                }
            }
        }.launchIn(viewModelScope)
    }

    fun searchComics(search: String) {
        repoUseCase(search).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _newThisMonth.value = RepoState(false, result.data ?: emptyList())
                }
                is Resource.Error -> {
                    _newThisMonth.value = RepoState(
                        error = result.status ?: "An unexpected error occurred"
                    )
                }
                is Resource.Loading -> {
                    _newThisMonth.value = RepoState(loading = true)
                }
            }
        }.launchIn(viewModelScope)
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