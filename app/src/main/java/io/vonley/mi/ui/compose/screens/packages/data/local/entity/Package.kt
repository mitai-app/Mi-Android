package io.vonley.mi.ui.compose.screens.packages.data.local.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Entity
@Parcelize
data class Package(
    val link: String,
    val name: String,
    val author: String,
    val version: String,
    val type: PackageType,
    val icon: String,
    val dl: Map<String, String>,
    @PrimaryKey var repo: String = ""
): Parcelable