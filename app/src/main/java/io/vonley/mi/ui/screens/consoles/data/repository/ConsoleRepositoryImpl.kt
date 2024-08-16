package io.vonley.mi.ui.screens.consoles.data.repository

import androidx.lifecycle.LiveData
import io.vonley.mi.di.network.listeners.OnConsoleListener
import io.vonley.mi.ui.screens.consoles.domain.remote.SyncService
import io.vonley.mi.common.base.BaseRepository
import io.vonley.mi.models.enums.Feature
import io.vonley.mi.models.enums.PlatformType
import io.vonley.mi.ui.screens.consoles.domain.model.Console
import io.vonley.mi.ui.screens.consoles.data.local.ConsoleDao
import io.vonley.mi.ui.screens.consoles.domain.repository.ConsoleRepository
import javax.inject.Inject


class ConsoleRepositoryImpl @Inject constructor(
    private val sync: SyncService, dao: ConsoleDao
) : BaseRepository<ConsoleDao>(dao), ConsoleRepository, OnConsoleListener {

    init {
        sync.addConsoleListener(this)
    }

    override fun getMyConsoles(): LiveData<List<Console>> {
        if (sync.isConnected) {
            val wifi = sync.wifiInfo.ssid
            val consoles = if (wifi != null) {
                dao.get(wifi)
            } else {
                dao.getAll()
            }
            return consoles
        }
        return dao.getAll()
    }

    override suspend fun exists(ip: String): Boolean {
        return dao.exists(ip)
    }

    override suspend fun updateNickName(ip: String, nickname: String) {
        dao.insert(
            Console(
            ip,
            "Playstation 4",
            PlatformType.UNKNOWN,
            listOf(Feature.FTP),
            false,
            sync.wifiInfo.ssid,
            true
        )
        )
    }

    override suspend fun insert(console: String) {
        dao.insert(
            Console(
            console,
            "Playstation 4",
            PlatformType.PS4,
            listOf(Feature.FTP),
            false,
            sync.wifiInfo.ssid
        )
        )
    }

    override suspend fun getConsole(ip: String): Console {
        return dao.get(ip, sync.wifiInfo.ssid)
    }

    override suspend fun setPin(ip: String, b: Boolean) {
        dao.setPin(ip, b)
    }

    override fun selectConsole(console: Console) {
        sync.setTarget(console)
    }

    override fun onEmptyDataReceived() {

    }

    override fun onAlreadyStored() {

    }

    override val TAG: String
        get() = ""


}