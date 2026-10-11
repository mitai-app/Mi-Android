package io.vonley.mi.ui.screens.packages.presentation

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.vonley.mi.common.Resource
import io.vonley.mi.di.annotations.SharedPreferenceStorage
import io.vonley.mi.extensions.isLink
import io.vonley.mi.models.Payload
import io.vonley.mi.ui.screens.packages.data.local.entity.Package
import io.vonley.mi.ui.screens.packages.data.local.entity.Repo
import io.vonley.mi.ui.screens.packages.domain.remote.PayloadProgress
import io.vonley.mi.ui.screens.packages.domain.usecase.AddRepositoryUseCase
import io.vonley.mi.ui.screens.packages.domain.usecase.GetRepositoryUseCase
import io.vonley.mi.ui.screens.packages.domain.usecase.SendPackageException
import io.vonley.mi.ui.screens.packages.domain.usecase.SendPackageUseCase
import io.vonley.mi.ui.screens.packages.domain.usecase.SendPayloadsUseCase
import io.vonley.mi.utils.SharedPreferenceManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface RepoListState {
    data object Loading : RepoListState
    data class Success(val repos: List<Repo>) : RepoListState
    data class Error(val message: String) : RepoListState
}

enum class PayloadStatus { Queued, Sending, Sent, Failed }

data class QueuedPayload(val payload: Payload, val status: PayloadStatus = PayloadStatus.Queued, val error: String? = null)

data class PackagesUiState(
    val query: String = "",
    val repos: RepoListState = RepoListState.Loading,
    val queue: List<QueuedPayload> = emptyList(),
    val sending: Boolean = false,
    /** Destination for .bin payloads, e.g. "Golden Hen (port 9090)". */
    val binTarget: String = "",
) {
    val queryIsLink: Boolean get() = query.isLink()
}

@HiltViewModel
class PackageViewModel @Inject constructor(
    private val getRepositories: GetRepositoryUseCase,
    private val addRepository: AddRepositoryUseCase,
    private val sendPackageUseCase: SendPackageUseCase,
    private val sendPayloads: SendPayloadsUseCase,
    @SharedPreferenceStorage private val manager: SharedPreferenceManager,
) : ViewModel() {

    private val _state = MutableStateFlow(PackagesUiState(binTarget = binTarget()))
    val state: StateFlow<PackagesUiState> = _state.asStateFlow()

    private val messageChannel = Channel<String>(Channel.BUFFERED)
    val messages: Flow<String> = messageChannel.receiveAsFlow()

    private var searchJob: Job? = null

    init {
        loadRepos()
    }

    fun refreshTarget() {
        _state.update { it.copy(binTarget = binTarget()) }
    }

    fun onQueryChange(query: String) {
        _state.update { it.copy(query = query) }
        searchJob?.cancel()
        when {
            query.isBlank() -> loadRepos()
            query.isLink() -> Unit
            else -> searchJob = viewModelScope.launch {
                delay(SEARCH_DEBOUNCE_MS)
                getRepositories(query).collect(::applyRepos)
            }
        }
    }

    private fun loadRepos() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch { getRepositories().collect(::applyRepos) }
    }

    private fun applyRepos(result: Resource<List<Repo>>) {
        val repos = when (result) {
            is Resource.Loading -> RepoListState.Loading
            is Resource.Success -> RepoListState.Success(result.data)
            is Resource.Error -> RepoListState.Error(result.status ?: "An unexpected error occurred")
        }
        _state.update { it.copy(repos = repos) }
    }

    fun addRepo() {
        val link = state.value.query.trim()
        viewModelScope.launch {
            when (val result = addRepository(link)) {
                is Resource.Success -> {
                    messageChannel.send("Added ${result.data.title}")
                    _state.update { it.copy(query = "") }
                    loadRepos()
                }

                else -> messageChannel.send(result.status ?: "Unable to add repo")
            }
        }
    }

    fun sendPackage(pkg: Package) {
        viewModelScope.launch {
            messageChannel.send("Downloading ${pkg.name}…")
            sendPackageUseCase(pkg)
                .onEach { progress ->
                    when (progress) {
                        is PayloadProgress.Writing -> messageChannel.send("Sending ${pkg.name}…")
                        is PayloadProgress.Sent -> messageChannel.send("${pkg.name} sent!")
                        is PayloadProgress.Failed -> messageChannel.send("${pkg.name}: ${progress.reason}")
                    }
                }
                .catch { error ->
                    val message = (error as? SendPackageException)?.let { "${it.title}: ${it.message}" }
                        ?: "Unable to send ${pkg.name}: ${error.message}"
                    messageChannel.send(message)
                }
                .collect()
        }
    }

    fun addLocal(uris: List<Uri>) {
        viewModelScope.launch {
            uris.forEach { uri ->
                runCatching { sendPayloads.stage(uri) }
                    .onSuccess { payload ->
                        _state.update { state ->
                            if (state.queue.any { it.payload.name == payload.name }) state
                            else state.copy(queue = state.queue + QueuedPayload(payload))
                        }
                    }
                    .onFailure { messageChannel.send("Couldn't read the selected file: ${it.message}") }
            }
        }
    }

    fun remove(item: QueuedPayload) {
        _state.update { it.copy(queue = it.queue - item) }
        item.payload.file.delete()
    }

    fun sendQueue() {
        val pending = state.value.queue.filter { it.status != PayloadStatus.Sent }.map { it.payload }
        if (pending.isEmpty() || state.value.sending) return
        viewModelScope.launch {
            _state.update { it.copy(sending = true) }
            sendPayloads(pending)
                .onEach { progress ->
                    val (status, error) = when (progress) {
                        is PayloadProgress.Writing -> PayloadStatus.Sending to null
                        is PayloadProgress.Sent -> PayloadStatus.Sent to null
                        is PayloadProgress.Failed -> PayloadStatus.Failed to progress.reason
                    }
                    setStatus(progress.payload, status, error)
                }
                .catch { messageChannel.send("Unable to send payloads: ${it.message}") }
                .collect()
            _state.update { it.copy(sending = false) }
            val failed = state.value.queue.count { it.status == PayloadStatus.Failed }
            messageChannel.send(if (failed == 0) "All payloads sent" else "$failed payload(s) failed")
        }
    }

    fun clearSent() {
        val sent = state.value.queue.filter { it.status == PayloadStatus.Sent }
        sent.forEach { it.payload.file.delete() }
        _state.update { it.copy(queue = it.queue - sent.toSet()) }
    }

    private fun setStatus(payload: Payload, status: PayloadStatus, error: String?) {
        _state.update { state ->
            state.copy(queue = state.queue.map {
                if (it.payload == payload) it.copy(status = status, error = error) else it
            })
        }
    }

    private fun binTarget(): String {
        val feature = manager.featurePort
        return "${feature.title} (port ${feature.ports.first()})"
    }

    private companion object {
        const val SEARCH_DEBOUNCE_MS = 300L
    }
}
