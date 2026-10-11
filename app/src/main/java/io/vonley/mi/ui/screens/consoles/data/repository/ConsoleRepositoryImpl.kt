package io.vonley.mi.ui.screens.consoles.data.repository

import io.vonley.mi.models.enums.Feature
import io.vonley.mi.models.enums.PlatformType
import io.vonley.mi.ui.screens.consoles.data.local.ConsoleDao
import io.vonley.mi.ui.screens.consoles.domain.model.Client
import io.vonley.mi.ui.screens.consoles.domain.model.Console
import io.vonley.mi.ui.screens.consoles.domain.remote.SyncService
import io.vonley.mi.ui.screens.consoles.domain.repository.ConsoleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class ConsoleRepositoryImpl @Inject constructor(
    private val sync: SyncService,
    private val dao: ConsoleDao
) : ConsoleRepository {

    override val target: StateFlow<Client?> get() = sync.targetFlow

    override fun getMyConsoles(): Flow<List<Console>> {
        val wifi = sync.wifiSsid
        return if (sync.isConnected && wifi.isNotEmpty()) dao.get(wifi) else dao.getAll()
    }

    /**
     * Manually added consoles are pinned so the background scan, which drops
     * unpinned hosts it cannot find, does not remove them.
     */
    override suspend fun addConsole(ip: String): Console {
        val existing = dao.getByIp(ip)
        if (existing != null) {
            dao.setPin(ip, true)
            return existing.copy(pinned = true)
        }
        val console = Console(
            ip = ip,
            name = DEFAULT_NAME,
            type = PlatformType.PS4,
            features = listOf(Feature.FTP),
            lastKnownReachable = false,
            wifi = sync.wifiSsid,
            pinned = true
        )
        dao.insert(console)
        return console
    }

    override suspend fun setPin(ip: String, pinned: Boolean) {
        dao.setPin(ip, pinned)
    }

    override fun selectConsole(console: Client) {
        sync.setTarget(console)
    }

    override fun refresh() {
        sync.getClients(loop = false)
    }

    private companion object {
        const val DEFAULT_NAME = "Playstation 4"
    }
}
