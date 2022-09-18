package io.vonley.mi.ui.main.console.presentation.sheets.views

import androidx.recyclerview.widget.RecyclerView
import io.vonley.mi.databinding.ViewWebmanBinding
import io.vonley.mi.di.network.protocols.webman.Webman
import io.vonley.mi.ui.main.console.presentation.sheets.adapters.WebmanGameSetAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WebmanViewHolder(
    val binding: ViewWebmanBinding,
    override val protocol: Webman
) : RecyclerView.ViewHolder(binding.root), ViewHolderProtocol<Webman> {


    override fun init() {
        binding.vhConsoleDisc.eject.setOnClickListener {
            launch {
                try {
                    protocol.eject()
                } catch (e: Exception) {
                }
            }
        }
        binding.vhConsoleDisc.unmount.setOnClickListener {
            launch {
                try {
                    protocol.unmount()
                } catch (e: Exception) {
                }
            }
        }
        binding.vhConsoleDisc.insert.setOnClickListener {
            launch {
                try {
                    protocol.insert()
                } catch (e: Exception) {
                }
            }
        }
        binding.vhConsoleDisc.refreshXml.setOnClickListener {
            launch {
                try {
                    protocol.refresh()
                } catch (e: Exception) {

                }
            }
        }
        launch {
            try {
                val games = protocol.searchGames()
                withContext(Dispatchers.Main) {
                    binding.webmanRecycler.adapter = WebmanGameSetAdapter(games, protocol)
                }
            } catch (e: Throwable) {

            }
        }
    }

}