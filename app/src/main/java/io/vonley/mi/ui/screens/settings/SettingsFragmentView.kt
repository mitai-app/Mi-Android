package io.vonley.mi.ui.screens.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SettingsFragmentView: Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                SettingsView()
            }
        }
    }
}

@Composable
fun SettingsView() {

}