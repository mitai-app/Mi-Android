package io.vonley.mi.ui.compose

import androidx.compose.runtime.Composable
import io.vonley.mi.R
import io.vonley.mi.ui.compose.screens.*
import io.vonley.mi.ui.compose.screens.consoles.ConsoleView
import io.vonley.mi.ui.compose.screens.ftp.FtpView
import io.vonley.mi.ui.compose.screens.packages.PackageView
import io.vonley.mi.ui.compose.screens.settings.SettingsView

typealias ComposableFun = @Composable () -> Unit

sealed class TabItem(var icon: Int, var title: String, var screen: ComposableFun) {
    val route: String get() = title.lowercase()
    object Home : TabItem(R.drawable.icon_svg_home, "Home", { HomeView() })
    object Consoles : TabItem(R.drawable.icon_svg_monitor, "Consoles", { ConsoleView() })
    object Packages : TabItem(R.drawable.icon_svg_wifi, "Repository", { PackageView() })
    object Ftp : TabItem(R.drawable.icon_svg_inbox, "Ftp", { FtpView() })
    object Settings : TabItem(R.drawable.icon_svg_settings, "Settings", { SettingsView() })
}