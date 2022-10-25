package io.vonley.mi.ui.main.console.presentation.sheets.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import io.vonley.mi.databinding.ViewCcapiBinding
import io.vonley.mi.databinding.ViewKlogBinding
import io.vonley.mi.databinding.ViewPs3mapiBinding
import io.vonley.mi.databinding.ViewWebmanBinding
import io.vonley.mi.di.network.PSXService
import io.vonley.mi.di.network.protocols.ccapi.CCAPI
import io.vonley.mi.di.network.protocols.common.PSXProtocol
import io.vonley.mi.di.network.protocols.klog.KLog
import io.vonley.mi.di.network.protocols.ps3mapi.PS3MAPI
import io.vonley.mi.di.network.protocols.webman.Webman
import io.vonley.mi.models.enums.Feature
import io.vonley.mi.ui.main.MainContract
import io.vonley.mi.ui.compose.screens.consoles.domain.model.Client
import io.vonley.mi.ui.main.console.presentation.sheets.views.*
import javax.inject.Inject

class ProtocolRecyclerAdapter @Inject constructor(
    val view: MainContract.View,
    val ps3mapi: PS3MAPI,
    val ccapi: CCAPI,
    val webman: Webman,
    val klog: KLog,
    val service: PSXService
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var services: List<PSXProtocol> = emptyList()

    private val clientObserver = Observer<Client> {
        it?.let { client ->
            init(client.features)
        }
    }

    fun init() {
        service.liveTarget.observeForever(clientObserver)
        service.initialize()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = when (viewType) {
            Feature.KLOG.ordinal -> KLogViewHolder(
                ViewKlogBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                ), klog
            )
            Feature.WEBMAN.ordinal -> WebmanViewHolder(
                ViewWebmanBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                ), webman
            )
            Feature.PS3MAPI.ordinal -> PS3MAPIViewHolder(
                ViewPs3mapiBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                ), ps3mapi
            )
            Feature.CCAPI.ordinal -> CCAPIViewHolder(
                ViewCcapiBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                ), ccapi
            )
            else -> throw Exception("Invalid View")
        }
        return view
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val protocol = this.services[position]
        val holder = when (protocol.feature) {
            Feature.WEBMAN -> (holder as? WebmanViewHolder)
            Feature.PS3MAPI -> (holder as? PS3MAPIViewHolder)
            Feature.CCAPI -> (holder as? CCAPIViewHolder)
            Feature.KLOG -> (holder as? KLogViewHolder)
            else -> null
        }
        holder?.init()
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        (holder as? ViewHolderProtocol<*>)?.cleanup()
    }

    override fun getItemViewType(position: Int): Int = this.services[position].feature.ordinal

    override fun getItemCount(): Int {
        return this.services.size
    }

    fun cleanup() {
        service.liveTarget.removeObserver(clientObserver)
    }

    fun init(features: List<Feature>) {
        val target = service.target
        if (target != null) {
            services = features.mapNotNull { f ->
                when (f) {
                    ps3mapi.feature -> ps3mapi
                    ccapi.feature -> ccapi
                    webman.feature -> webman
                    klog.feature -> klog
                    else -> null
                }
            }
            notifyDataSetChanged()
        }
    }

}