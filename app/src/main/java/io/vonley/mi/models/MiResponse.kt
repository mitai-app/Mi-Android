package io.vonley.mi.models

import androidx.compose.ui.graphics.Color
import com.google.gson.annotations.SerializedName
import io.vonley.mi.Mi

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
        MIEnumCMD.INITIATED -> Mi.Color.SECONDARY
        MIEnumCMD.STARTED -> Mi.Color.Mi.BLUE
        MIEnumCMD.SUCCESS -> Mi.Color.Mi.GREEN
        MIEnumCMD.FAILED -> Mi.Color.Mi.RED
        MIEnumCMD.CONTINUE -> Mi.Color.Mi.PURPLE
        MIEnumCMD.PAYLOAD -> Mi.Color.Mi.BLUE
        MIEnumCMD.PAYLOAD_REQUEST -> Mi.Color.Mi.YELLOW
        MIEnumCMD.PENDING -> Mi.Color.Mi.ORANGE
    }
}