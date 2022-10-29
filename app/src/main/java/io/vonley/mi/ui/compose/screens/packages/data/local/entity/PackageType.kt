package io.vonley.mi.ui.compose.screens.packages.data.local.entity

import android.os.Parcelable
import androidx.room.Entity
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Entity
@Parcelize
enum class PackageType: Parcelable {
    @SerializedName("app")
    APP,

    @SerializedName("tool")
    TOOL,

    @SerializedName("plugin")
    PLUGIN
}