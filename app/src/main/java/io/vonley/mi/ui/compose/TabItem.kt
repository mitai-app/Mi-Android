package io.vonley.mi.ui.compose

import androidx.compose.runtime.Composable
import io.vonley.mi.R
import io.vonley.mi.ui.compose.screens.*
import io.vonley.mi.ui.compose.screens.consoles.presentation.ConsolesView
import io.vonley.mi.ui.compose.screens.ftp.FtpView
import io.vonley.mi.ui.compose.screens.packages.presentation.RepositoryView
import io.vonley.mi.ui.compose.screens.settings.SettingsView

typealias ComposableFun = @Composable () -> Unit

sealed class TabItem(var icon: Int, var title: String, var screen: ComposableFun) {
    val route: String get() = title.lowercase()
    object Home : TabItem(R.drawable.icon_svg_home, "Home", { HomeView() })
    object Consoles : TabItem(R.drawable.icon_svg_monitor, "Consoles", { ConsolesView() })
    object Packages : TabItem(R.drawable.icon_svg_wifi, "Repository", { RepositoryView() })
    object Ftp : TabItem(R.drawable.icon_svg_inbox, "Ftp", { FtpView() })
    object Settings : TabItem(R.drawable.icon_svg_settings, "Settings", { SettingsView() })
}