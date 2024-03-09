package io.vonley.mi.ui.screens.home.presentation

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.vonley.mi.di.annotations.SharedPreferenceStorage
import io.vonley.mi.di.network.MiServer
import io.vonley.mi.di.network.impl.MiServerImpl
import io.vonley.mi.models.Device
import io.vonley.mi.models.MiCMDResponse
import io.vonley.mi.models.MiResponse
import io.vonley.mi.utils.SharedPreferenceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

sealed class HomeState {
    data object Empty : HomeState()
    data class Log(val device: Device, val logs: ArrayList<MiCMDResponse>) : HomeState()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    @SharedPreferenceStorage val manager: SharedPreferenceManager,
    val jb: MiServer,
) : ViewModel(), MiServerImpl.MiJbServerListener {

    private val _state = MutableStateFlow<HomeState>(HomeState.Empty)
    val state: StateFlow<HomeState> get() = _state

    init {
        jb.add(this)
    }

    override fun onDeviceConnected(device: Device) = Unit
    override fun onLog(string: String) = Unit
    override fun onJailbreakSucceeded(message: String) = Unit
    override fun onJailbreakFailed(message: String) = Unit
    override fun onPayloadSent(msg: String?) = Unit
    override fun onUnsupported(s: String) = Unit
    override fun onSendPayloadAttempt(attempt: Int) = Unit

    override fun onCommand(mi: MiResponse<MiResponse.Cmd>) {
        _state.update { currState ->
            when (currState) {
                HomeState.Empty -> HomeState.Log(mi.device!!, arrayListOf(mi))
                is HomeState.Log -> if (currState.device != mi.device) {
                    currState.copy(mi.device!!, arrayListOf(mi))
                } else {
                    val logs = currState.logs
                    logs.add(mi)
                    currState.copy(currState.device, logs)
                }
            }
        }
    }

}