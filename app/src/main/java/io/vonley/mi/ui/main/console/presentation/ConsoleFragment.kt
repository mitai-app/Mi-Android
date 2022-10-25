package io.vonley.mi.ui.main.console.presentation

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import io.vonley.mi.databinding.FragmentConsoleBinding
import io.vonley.mi.extensions.e
import io.vonley.mi.ui.compose.screens.consoles.presentation.ConsoleViewModel
import io.vonley.mi.ui.compose.screens.consoles.domain.model.Console
import io.vonley.mi.ui.dialogs.MiInputDialog
import io.vonley.mi.ui.main.MainContract
import io.vonley.mi.ui.compose.screens.consoles.domain.model.Client
import io.vonley.mi.ui.main.console.presentation.adapters.ConsoleRecyclerAdapter
import javax.inject.Inject

/**
 * Console Fragment to handle connected clients
 */
@AndroidEntryPoint
class ConsoleFragment : Fragment(), ConsoleContract.View, Observer<List<Console>> {

    private var mainView: MainContract.View? = null

    val vm: ConsoleViewModel by viewModels()

    @Inject
    lateinit var adapter: ConsoleRecyclerAdapter

    @Inject
    lateinit var presenter: ConsoleContract.Presenter

    private var _binding: FragmentConsoleBinding? = null

    private val binding get() = _binding!!

    private var swipeCallback: ItemTouchHelper.SimpleCallback = object :
        ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            Toast.makeText(requireContext(), "on Move", Toast.LENGTH_SHORT).show()
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, swipeDir: Int) {
            val vh = viewHolder as ConsoleRecyclerAdapter.ConsoleViewHolder
            val pos = viewHolder.bindingAdapterPosition
            val client = vh.client?:return
            if (swipeDir == ItemTouchHelper.LEFT) {
                vm.unpin(client)
            } else if (swipeDir == ItemTouchHelper.RIGHT) {
                vm.pin(client)
            }
        }
    }
    private val swipeTouchHelper by lazy { ItemTouchHelper(swipeCallback) }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConsoleBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.consoleRecycler.adapter = adapter
        swipeTouchHelper.attachToRecyclerView(binding.consoleRecycler)
        mainView?.setSummary(presenter.getTargetSummary)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainView = (context as? MainContract.View)
    }

    override fun onDetach() {
        super.onDetach()
        mainView = null
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        presenter.cleanup()
    }

    override fun onStop() {
        super.onStop()
        //vm.consoles.removeObservers(viewLifecycleOwner)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onClientsFound(clients: List<Client>) = Unit

    override fun addConsole() {
        MiInputDialog.createDialog("Enter Console IP", "192.168.?.?")
            .show(childFragmentManager, TAG)
    }

    override fun onConsoleAdded() {
        Snackbar.make(requireView(), "Console added!", Snackbar.LENGTH_LONG).show()
    }

    override fun onError(e: Throwable) {
        Log.e("ERROR", "You are shit", e)
    }

    override fun onEmptyDataReceived() {

    }


    override fun onDialogInput(input: String) {
        super.onDialogInput(input)
        vm.addConsole(input)
    }

    override fun onAlreadyStored() {

    }

    override val TAG: String
        get() = ConsoleFragment::class.java.name

    override fun onChanged(consoles: List<Console>?) {
        "Called $consoles".e("OK")
        consoles?.let { consoles ->
            if (consoles.isNotEmpty()) {
                "NOT EMPTY".e("OK")
                adapter.setData(consoles)
            } else {
                "EMPTY".e("CRAZY")
            }
        } ?: kotlin.run {
            "Null?".e("WOW")
        }
    }


}