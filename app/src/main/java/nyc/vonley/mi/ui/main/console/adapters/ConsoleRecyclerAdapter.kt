package nyc.vonley.mi.ui.main.console.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import nyc.vonley.mi.databinding.VhConsoleBinding
import nyc.vonley.mi.di.network.SyncService
import nyc.vonley.mi.models.Client
import nyc.vonley.mi.models.Console
import nyc.vonley.mi.models.activeFeatures
import nyc.vonley.mi.models.featureString
import nyc.vonley.mi.ui.main.MainContract
import javax.inject.Inject

class ConsoleRecyclerAdapter @Inject constructor(
    val view: MainContract.View,
    val sync: SyncService
) : RecyclerView.Adapter<ConsoleRecyclerAdapter.ConsoleViewHolder>() {

    private var consoles = emptyList<Console>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConsoleViewHolder {
        return ConsoleViewHolder(
            VhConsoleBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ConsoleViewHolder, position: Int) {
        holder.setConsole(consoles[position])
    }

    override fun getItemCount(): Int {
        return this.consoles.size
    }

    fun setData(consoles: List<Console>) {
        this.consoles = consoles
        notifyDataSetChanged()
    }

    inner class ConsoleViewHolder(val binding: VhConsoleBinding) : RecyclerView.ViewHolder(binding.root) {

        fun setTarget(console: Client) {
            sync.setTarget(console)
            view.setSummary("Current Target: ${console.name}, w/ ${console.featureString}")
            Toast.makeText(itemView.context, "Target set!", Toast.LENGTH_SHORT).show()
        }

        fun setConsole(console: Client) {
            val headers = "${console.name} - ${console.ip}"
            binding.vhConsoleNickname.text = headers
            binding.vhConsoleIp.text = console.featureString.takeIf { it.isNotEmpty() } ?: "Incompatible"
            binding.root.setOnClickListener {
                if (console.activeFeatures.isNotEmpty()) {
                    setTarget(console)
                } else {
                    MaterialAlertDialogBuilder(it.context)
                        .setTitle("Warning")
                        .setMessage("This device does not have any port opens it seems, would you still like to connect?")
                        .setPositiveButton("Connect") { dialog, which ->
                            setTarget(console)
                            dialog.dismiss()
                        }
                        .setNegativeButton("Cancel") { dialog, which ->
                            dialog.dismiss()
                        }.show()
                }
            }
        }
    }

}