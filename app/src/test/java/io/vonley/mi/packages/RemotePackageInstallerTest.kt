package io.vonley.mi.packages

import io.vonley.mi.models.Payload
import io.vonley.mi.models.enums.Feature
import io.vonley.mi.ui.screens.consoles.domain.model.Client
import io.vonley.mi.ui.screens.consoles.domain.remote.SyncService
import io.vonley.mi.ui.screens.packages.data.remote.RemotePackageInstallerImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.OkHttpClient
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File
import java.net.HttpURLConnection
import java.net.ServerSocket
import java.net.Socket
import java.net.URL

class RemotePackageInstallerTest {

    private class LoopbackSync : SyncService {
        override val targetFlow: StateFlow<Client?> = MutableStateFlow(null)
        override val wifiSsid = "test"
        override val localDeviceIp = "127.0.0.1"
        override val isConnected = true
        override val http = OkHttpClient()
        override fun getClients(loop: Boolean) = Unit
        override fun stop() = Unit
        override fun setTarget(client: Client) = Unit
        override fun cleanup() = Unit
        override fun createSocket(client: Client?, feature: Feature): Socket? = null
        override fun getSocket(client: Client?, feature: Feature): Socket? = null
    }

    private fun freePort() = ServerSocket(0).use { it.localPort }

    private val bytes = ByteArray(10_000) { (it % 251).toByte() }

    private fun hosted(): Pair<RemotePackageInstallerImpl, String> {
        val file = File.createTempFile("payload", ".pkg").apply { writeBytes(bytes); deleteOnExit() }
        val installer = RemotePackageInstallerImpl(LoopbackSync(), freePort())
        return installer to installer.hostPackage(Payload("My Game.pkg", file))
    }

    @Test
    fun `serves the whole file`() {
        val (_, url) = hosted()
        val connection = URL(url).openConnection() as HttpURLConnection
        assertEquals(200, connection.responseCode)
        assertArrayEquals(bytes, connection.inputStream.use { it.readBytes() })
    }

    @Test
    fun `serves the requested byte range`() {
        val (_, url) = hosted()
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.setRequestProperty("Range", "bytes=1000-1999")
        assertEquals(206, connection.responseCode)
        assertEquals("bytes 1000-1999/10000", connection.getHeaderField("Content-Range"))
        assertArrayEquals(bytes.copyOfRange(1000, 2000), connection.inputStream.use { it.readBytes() })
    }

    @Test
    fun `unknown paths are 404`() {
        val (_, url) = hosted()
        val connection = URL(url.replace("My%20Game", "Other")).openConnection() as HttpURLConnection
        assertEquals(404, connection.responseCode)
    }
}
