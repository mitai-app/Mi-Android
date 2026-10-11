package io.vonley.mi.models.enums

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class Protocol : Parcelable {
    HTTP, SOCKET, FTP, NONE
}