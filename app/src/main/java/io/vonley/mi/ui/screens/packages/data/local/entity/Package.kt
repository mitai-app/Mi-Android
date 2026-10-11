package io.vonley.mi.ui.screens.packages.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Package(
    val link: String,
    val name: String,
    val author: String,
    val version: String,
    val type: PackageType,
    val icon: String,
    val dl: Map<String, String>,
    @PrimaryKey var repo: String = ""
) {
    /** Payloads are sent to the loader, apps go through the package installer. */
    val fileExtension: String
        get() = when (type) {
            PackageType.APP -> ".pkg"
            PackageType.PLUGIN, PackageType.TOOL -> ".bin"
        }

    /**
     * Finds the download for a firmware version such as "9.00". Repos key their
     * downloads as "v900"; plain "900" and "9.00" are accepted too.
     */
    fun downloadFor(firmware: String): String? {
        val compact = firmware.replace(".", "")
        return dl["v$compact"] ?: dl[compact] ?: dl[firmware]
    }
}
