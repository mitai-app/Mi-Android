package io.vonley.mi.ui.screens.consoles.presentation.protocol

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.vonley.mi.di.network.PSXService
import io.vonley.mi.di.network.protocols.ccapi.CCAPI
import io.vonley.mi.di.network.protocols.common.PSXNotifier
import io.vonley.mi.di.network.protocols.common.cmds.Boot
import io.vonley.mi.di.network.protocols.common.cmds.Buzzer
import io.vonley.mi.di.network.protocols.klog.KLog
import io.vonley.mi.di.network.protocols.ps3mapi.PS3MAPI
import io.vonley.mi.di.network.protocols.webman.Webman
import io.vonley.mi.di.network.protocols.webman.models.Game
import io.vonley.mi.di.network.protocols.webman.models.GameType
import io.vonley.mi.extensions.e
import io.vonley.mi.models.enums.Feature
import io.vonley.mi.ui.screens.consoles.domain.model.Client
import io.vonley.mi.ui.screens.consoles.domain.model.protocolFeatures
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class ProtocolUiState(
    val features: List<Feature> = emptyList(),
    val ps3mapiConnected: Boolean? = null,
    val games: Map<GameType, List<Game>> = emptyMap(),
    val gamesLoading: Boolean = false,
    val klog: List<String> = emptyList(),
    val klogStreaming: Boolean = false,
    val busy: Boolean = false,
)

enum class ConsoleAction(val label: String) {
    Beep("Beep"),
    SoftReboot("Soft reboot"),
    Reboot("Reboot"),
    Shutdown("Shutdown"),
}

enum class DiscAction(val label: String) {
    Eject("Eject"),
    Insert("Insert"),
    Unmount("Unmount"),
    RefreshXml("Refresh XML"),
}

@HiltViewModel
class ProtocolViewModel @Inject constructor(
    private val service: PSXService,
    private val ps3mapi: PS3MAPI,
    private val webman: Webman,
    private val ccapi: CCAPI,
    private val klog: KLog,
) : ViewModel() {

    private val _state = MutableStateFlow(ProtocolUiState())
    val state: StateFlow<ProtocolUiState> = _state.asStateFlow()

    private val messageChannel = Channel<String>(Channel.BUFFERED)
    val messages: Flow<String> = messageChannel.receiveAsFlow()

    private var session: Job? = null
    private var boundIp: String? = null

    /** Connects to the services [target] exposes; a no-op when already bound to it. */
    fun bind(target: Client) {
        if (boundIp == target.ip && session?.isActive == true) return
        session?.cancel()
        klog.disconnect()
        boundIp = target.ip
        val features = target.protocolFeatures
        _state.value = ProtocolUiState(features = features)
        session = viewModelScope.launch {
            runCatching { service.connectTarget() }
            if (Feature.KLOG in features) launch { streamKlog() }
            if (Feature.PS3MAPI in features) launch { connectPs3mapi() }
            if (Feature.WEBMAN in features) launch { loadGames() }
        }
    }

    private suspend fun streamKlog(): Unit = coroutineScope {
        klog.connect()
        launch {
            klog.isStreaming.collect { streaming -> _state.update { it.copy(klogStreaming = streaming) } }
        }
        klog.logs.collect { line ->
            _state.update { it.copy(klog = (it.klog + line).takeLast(MAX_KLOG_LINES)) }
        }
    }

    /** Stops streaming when the sheet closes; the view model outlives it. */
    fun unbind() {
        session?.cancel()
        session = null
        boundIp = null
        klog.disconnect()
    }

    private suspend fun connectPs3mapi() {
        val connected = runCatching { withContext(Dispatchers.IO) { ps3mapi.connect() } }
            .onFailure { "PS3MAPI connect failed: ${it.message}".e(TAG, it) }
            .getOrDefault(false)
        _state.update { it.copy(ps3mapiConnected = connected) }
        if (!connected) {
            messageChannel.send("Couldn't reach PS3MAPI. The PS3 may be off or in the middle of a boot.")
        }
    }

    fun loadGames() {
        viewModelScope.launch {
            _state.update { it.copy(gamesLoading = true) }
            val games = runCatching { withContext(Dispatchers.IO) { webman.searchGames() } }
                .onFailure { messageChannel.send("Unable to load the webMAN game list") }
                .getOrDefault(emptyMap())
            _state.update { it.copy(games = games, gamesLoading = false) }
        }
    }

    fun perform(feature: Feature, action: ConsoleAction) = run(action.label) {
        val notifier: PSXNotifier = notifierFor(feature) ?: return@run
        when (action) {
            ConsoleAction.Beep -> notifier.buzzer(Buzzer.SINGLE)
            ConsoleAction.SoftReboot -> notifier.boot(Boot.SOFTREBOOT)
            ConsoleAction.Reboot -> notifier.boot(Boot.REBOOT)
            ConsoleAction.Shutdown -> notifier.boot(Boot.SHUTDOWN)
        }
    }

    fun notify(feature: Feature, message: String) = run("Notify") {
        notifierFor(feature)?.notify(message)
    }

    fun disc(action: DiscAction) = run(action.label) {
        when (action) {
            DiscAction.Eject -> webman.eject()
            DiscAction.Insert -> webman.insert()
            DiscAction.Unmount -> webman.unmount()
            DiscAction.RefreshXml -> webman.refresh()
        }
    }

    fun play(game: Game) = run("Launching ${game.title}") { webman.play(game) }

    private fun notifierFor(feature: Feature): PSXNotifier? = when (feature) {
        Feature.PS3MAPI -> ps3mapi
        Feature.WEBMAN -> webman
        Feature.CCAPI -> ccapi
        else -> null
    }

    private fun run(label: String, block: suspend () -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(busy = true) }
            // Protocol calls are blocking socket/HTTP I/O.
            runCatching { withContext(Dispatchers.IO) { block() } }
                .onSuccess { messageChannel.send("$label sent") }
                .onFailure {
                    "$label failed: ${it.message}".e(TAG, it)
                    messageChannel.send("$label failed: ${it.message ?: "console unreachable"}")
                }
            _state.update { it.copy(busy = false) }
        }
    }

    override fun onCleared() {
        unbind()
        super.onCleared()
    }

    private companion object {
        const val TAG = "ProtocolViewModel"
        const val MAX_KLOG_LINES = 500
    }
}
