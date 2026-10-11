package io.vonley.mi.ui.screens.packages.data.local.entity

import com.google.gson.annotations.SerializedName

enum class PackageType {
    @SerializedName("app")
    APP,

    @SerializedName("tool")
    TOOL,

    @SerializedName("plugin")
    PLUGIN
}
