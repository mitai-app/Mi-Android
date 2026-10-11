package io.vonley.mi.ui.screens.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.vonley.mi.BuildConfig
import io.vonley.mi.di.annotations.SharedPreferenceStorage
import io.vonley.mi.di.network.JbEvent
import io.vonley.mi.di.network.JbLog
import io.vonley.mi.di.network.MiServer
import io.vonley.mi.models.AppUpdate
import io.vonley.mi.models.Device
import io.vonley.mi.models.ver
import io.vonley.mi.utils.SharedPreferenceManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class HomeUiState(
    val hostAddress: String,
    val serverRunning: Boolean,
    val device: Device? = null,
    val logs: List<JbLog> = emptyList(),
    val update: AppUpdate? = null,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    @SharedPreferenceStorage private val manager: SharedPreferenceManager,
    private val server: MiServer,
) : ViewModel() {

    private val update = MutableStateFlow(availableUpdate())

    val state: StateFlow<HomeUiState> = combine(
        server.isRunning,
        server.device,
        server.logs,
        update,
    ) { running, device, logs, update ->
        HomeUiState(
            hostAddress = server.hostAddress,
            serverRunning = running,
            device = device,
            logs = logs,
            update = update,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        HomeUiState(server.hostAddress, server.isRunning.value)
    )

    /** Jailbreak results to surface as dialogs/snackbars; not replayed. */
    val events: Flow<JbEvent> = server.events

    fun refresh() {
        update.value = availableUpdate()
    }

    private fun availableUpdate(): AppUpdate? = manager.update?.takeIf {
        runCatching { it.version.ver > BuildConfig.VERSION_NAME.ver }.getOrDefault(false)
    }
}
