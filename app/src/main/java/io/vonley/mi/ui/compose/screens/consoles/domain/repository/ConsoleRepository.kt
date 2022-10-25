package io.vonley.mi.ui.compose.screens.consoles.domain.repository

import androidx.lifecycle.LiveData
import io.vonley.mi.ui.compose.screens.consoles.domain.model.Console

interface ConsoleRepository {
    fun getMyConsoles(): LiveData<List<Console>>
    suspend fun exists(ip: String): Boolean
    suspend fun updateNickName(ip: String, nickname: String)
    suspend fun insert(console: String)
    suspend fun getConsole(ip: String): Console
    suspend fun setPin(ip: String, b: Boolean)
    fun selectConsole(console: Console)
}