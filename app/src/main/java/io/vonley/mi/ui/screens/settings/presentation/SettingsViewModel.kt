package io.vonley.mi.ui.screens.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.vonley.mi.models.enums.Feature
import io.vonley.mi.ui.screens.settings.domain.Settings
import io.vonley.mi.ui.screens.settings.domain.SettingsRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(repository.current())
    val state: StateFlow<Settings> = _state.asStateFlow()

    private val messageChannel = Channel<String>(Channel.BUFFERED)
    val messages: Flow<String> = messageChannel.receiveAsFlow()

    fun refresh() {
        _state.value = repository.current()
    }

    private inline fun change(block: SettingsRepository.() -> Unit) {
        repository.block()
        refresh()
    }

    fun setFtpUser(user: String) = change { setFtpUser(user) }
    fun setFtpPassword(password: String) = change { setFtpPassword(password) }
    fun setPayloadFeature(feature: Feature) = change { setPayloadFeature(feature) }
    fun setScanInterval(seconds: Int) = change { setScanInterval(seconds) }
    fun setCacheEnabled(enabled: Boolean) = change { setCacheEnabled(enabled) }

    fun setJbPort(port: Int) = change {
        setJbPort(port)
        messageChannel.trySend("Jailbreak server moved to port $port")
    }

    fun setJbServiceEnabled(enabled: Boolean) = change {
        setJbServiceEnabled(enabled)
        messageChannel.trySend(if (enabled) "Jailbreak server started" else "Jailbreak server stopped")
    }

    fun clearAll() {
        viewModelScope.launch {
            repository.clearAll()
            refresh()
            messageChannel.send("Settings cleared")
        }
    }

    companion object {
        fun isValidPort(text: String): Boolean = text.toIntOrNull()?.let { it in 1024..65535 } == true
    }
}
