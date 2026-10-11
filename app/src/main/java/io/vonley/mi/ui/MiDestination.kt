package io.vonley.mi.ui

import androidx.annotation.DrawableRes
import io.vonley.mi.R

enum class MiDestination(val route: String, val title: String, @DrawableRes val icon: Int) {
    Home("home", "Home", R.drawable.icon_svg_home),
    Consoles("consoles", "Consoles", R.drawable.icon_svg_monitor),
    Payloads("payloads", "Payloads", R.drawable.icon_svg_folder),
    Ftp("ftp", "FTP", R.drawable.icon_svg_upload_two),
    Settings("settings", "Settings", R.drawable.icon_svg_settings),
}
