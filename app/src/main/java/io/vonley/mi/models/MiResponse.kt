package io.vonley.mi.models

import androidx.compose.ui.graphics.Color
import com.google.gson.annotations.SerializedName
import io.vonley.mi.Constants

typealias MiCMDResponse = MiResponse<MiResponse.Cmd>

data class MiResponse<T>(
    @SerializedName("response") val response: String,
    @SerializedName("data") val data: T,
    var device: Device? = null
) {
    data class Cmd(@SerializedName("cmd") val cmd: MIEnumCMD)
}


enum class MIEnumCMD {

    @SerializedName("jb.initiated")
    INITIATED,

    @SerializedName("jb.started")
    STARTED,

    @SerializedName("jb.success")
    SUCCESS,

    @SerializedName("jb.failed")
    FAILED,

    @SerializedName("jb.continue")
    CONTINUE,

    @SerializedName("send.continue")
    PAYLOAD,

    @SerializedName("send.payload.request")
    PAYLOAD_REQUEST,

    @SerializedName("send.pending")
    PENDING
}

val MIEnumCMD.bg: Color get() {
    return when(this) {
        MIEnumCMD.INITIATED -> Constants.Color.SECONDARY
        MIEnumCMD.STARTED -> Constants.Color.Mi.BLUE
        MIEnumCMD.SUCCESS -> Constants.Color.Mi.GREEN
        MIEnumCMD.FAILED -> Constants.Color.Mi.RED
        MIEnumCMD.CONTINUE -> Constants.Color.Mi.PURPLE
        MIEnumCMD.PAYLOAD -> Constants.Color.Mi.BLUE
        MIEnumCMD.PAYLOAD_REQUEST -> Constants.Color.Mi.YELLOW
        MIEnumCMD.PENDING -> Constants.Color.Mi.ORANGE
    }
}