package io.vonley.mi.ui.compose.screens.packages.data.local.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import io.vonley.mi.room.converters.PackageConverter
import kotlinx.android.parcel.Parcelize

@Entity
@Parcelize
@TypeConverters(PackageConverter::class)
data class Repo(
    @PrimaryKey
    var link: String,
    val title: String,
    val author: String,
    val banner: String,
    val description: String,
    val packages: Array<Package>
): Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Repo) return false

        if (title != other.title) return false
        if (author != other.author) return false
        if (banner != other.banner) return false
        if (description != other.description) return false
        if (!packages.contentEquals(other.packages)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + author.hashCode()
        result = 31 * result + banner.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + packages.contentHashCode()
        return result
    }
}