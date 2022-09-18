package io.vonley.mi.di.network.protocols.ps3mapi

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.vonley.mi.di.network.PSXService
import io.vonley.mi.di.network.protocols.common.OnPipeReconnected
import io.vonley.mi.di.network.protocols.common.models.Process
import io.vonley.mi.di.network.protocols.common.models.Temperature
import io.vonley.mi.di.network.protocols.ps3mapi.models.PS3MAPIResponse
import io.vonley.mi.extensions.e
import io.vonley.mi.extensions.toJson
import io.vonley.mi.ui.main.console.data.remote.get
import io.vonley.mi.ui.main.console.data.remote.set
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import java.io.BufferedReader
import java.io.IOException
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import kotlin.coroutines.CoroutineContext

class PS3MAPIImpl(
    override val service: PSXService,
) : PS3MAPI, PS3MAPI.Listener {

    private var server: ServerSocket? = null
    private var data_sock: Socket? = null

    private val _socket: Socket? get() = service[service.target, feature]

    override var authed: Boolean = false

    override val socket: Socket get() = _socket!!

    private val job = Job()
    override val coroutineContext: CoroutineContext get() = Dispatchers.IO + job

    private var _br: BufferedReader? = null
    private var _pw: OutputStream? = null

    override val br: BufferedReader get() = _br!!
    override val pw: OutputStream get() = _pw!!

    override suspend fun send(data: String?) {
        super.send(data + "\r\n")
    }
    override suspend fun connect(): Boolean {
        if (_socket == null || _socket?.isClosed == true || _socket?.isConnected == false) {
            try {
                authed = false
                service[service.target] = feature
                "Socket is null, so we are connecting :)".e(TAG)
                if (_socket == null) {
                    "Socket is still null?)".e(TAG)
                    return false
                }
            }catch (e: Throwable){
                "Brother man brother man, ${e.message}".e(TAG)
                return false
            }
        }
        if(!socket.isConnected) {
            authed = false
            service[service.target] = feature
        }
        return when {
            authed && _socket?.isConnected == true -> {
                "Already connected bruh".e(TAG)
                _br = _socket?.getInputStream()?.bufferedReader()
                _pw = _socket?.getOutputStream()
                true
            }
            !authed && _socket?.isConnected == true && _socket?.isClosed == false-> {
                "We have to auth this bitch ass socket".e(TAG)
                try {
                    _br = _socket?.getInputStream()?.bufferedReader()
                    _pw = _socket?.getOutputStream()
                    val firstRecv = br.readLine()
                    val secondRecv = br.readLine()
                    "Got Value: $firstRecv".e(TAG)
                    val first: PS3MAPIResponse = PS3MAPIResponse.parse(firstRecv)
                    "Parsed: ${first.toJson()}".e(TAG)
                    if (first.success && first.code === PS3MAPIResponse.Code.PS3MAPI_OK_MGR_SERVER_CONNECTING) {
                        "Next Value: $secondRecv".e(TAG)
                        val second: PS3MAPIResponse = PS3MAPIResponse.parse(secondRecv)
                        "Parsed second: ${second.toJson()}".e(TAG)
                        if (second.success && second.code === PS3MAPIResponse.Code.PS3MAPI_OK_MGR_SERVER_CONNECTED) {
                            authed = true
                            return true
                        }
                        return true
                    }
                    true
                } catch (e: Throwable) {
                    e.message?.e("PS3MAPI:Connect:Error:notauthed:butconnected", e)
                    false
                }
            }
            else -> try {
                "Turn this shit off and try again next time".e(TAG)
                authed = false
                service[service.target] = feature
                false
            } catch (e: Throwable) {
                e.message?.e("PS3MAPI:Connect:Error:else", e)
                false
            }
        }
    }

    override val listener: PS3MAPI.Listener = this
    private var _liveProcesses = MutableLiveData<List<Process>>()
    override val liveProcesses: LiveData<List<Process>> get() = _liveProcesses

    private var _processes: ArrayList<Process> = arrayListOf()
        set(value) {
            if (value.isNotEmpty()) {
                synchronized(_processes) {
                    _liveProcesses.postValue(value)
                }
                field = value
            }
        }


    override val processes: List<Process> get() = _processes
    override var attached: Boolean = false
    override var process: Process? = null
    override suspend fun getPids(): List<Process> {
        val process: ArrayList<Process> = ArrayList<Process>()
        val text = "PROCESS GETALLPID"
        val (_, response, code) = PS3MAPIResponse.parse(sendAndRecv(text) ?: return emptyList())
        for (s in response.split("\\|".toRegex()).toTypedArray()) {
            if (s == "0") continue
            val (_, response1, _) = PS3MAPIResponse.parse(
                sendAndRecv("PROCESS GETNAME $s") ?: continue
            )
            process.add(Process.create(response1, s))
        }
        if (process.size > 0) {
            this._processes = process
            listener.onProcessReceived(PS3MAPIResponse.Code.PS3MAPI_OK_CLOSING_DATA_CONNECTION, process)
        }
        return process
    }


    override suspend fun openDataSocket() {
        try {
            sendAndRecv("PASV")?.let { response ->
                val pav = PS3MAPIResponse.parse(response)
                val start: Int = pav.response.indexOf("(") + 1
                val end: Int = pav.response.indexOf(")")
                val split: Array<String> =
                    pav.response.substring(start, end).split(",").toTypedArray()
                val ip = String.format(
                    "%s.%s.%s.%s",
                    split[0], split[1], split[2], split[3]
                )
                val port = (Integer.valueOf(split[4]) shl 8) + Integer.valueOf(
                    split[5]
                )
                data_sock = Socket()
                data_sock?.connect(InetSocketAddress(ip, port))
            } ?: run {

            }
        } catch (e: IOException) {
            throw e
        } catch (ex: Exception) {
            //throw JMAPIException("Malformed PASV")
        }
    }

    override fun connectDataSocket() {
        if (data_sock != null) // already connected (always so if passive mode)
            return
        try {
            data_sock = server?.accept() // Accept is blocking
            server?.close()
            server = null
            if (data_sock == null) {
                throw Exception("Could not establish server")
            }
        } catch (ex: Exception) {
            throw Exception("Failed to connect for data transfer: " + ex.message)
        }
    }

    override fun closeDataSocket(): Boolean {
        try {
            data_sock?.close()
            data_sock = null
            server = null
            return true
        } catch (e: IOException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }
        return false
    }


    override fun onError(error: String?) {
        error.e("FAILURE")
    }

    override fun onResponse(
        ps3Op: PS3MAPI.PS3OP?,
        responseCode: PS3MAPIResponse.Code?,
        message: String?
    ) {

    }

    override fun onProcessReceived(responseCode: PS3MAPIResponse.Code?, processes: List<Process>?) {

    }

    override suspend fun reconnectPipes(callback: OnPipeReconnected): String? {
        if(_socket?.isConnected == true) {
            _socket?.close()
            authed = false
            connect()
            return callback()
        }
        return null
    }

    override fun onTemperatureReceived(
        responseCode: PS3MAPIResponse.Code?,
        temperature: Temperature?
    ) {

    }

}