package io.vonley.mi.models.enums

import android.os.Parcelable
import androidx.room.Entity
import kotlinx.parcelize.Parcelize

@Entity
@Parcelize
enum class Protocol : Parcelable {
    HTTP, SOCKET, FTP, NONE
}