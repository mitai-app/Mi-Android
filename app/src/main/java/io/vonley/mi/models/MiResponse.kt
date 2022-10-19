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
    data class Cmd(@SerializedName("cmd") val cmd: Minum)
}


enum class Minum {

    @SerializedName("jb.initiated")
    initiated,

    @SerializedName("jb.started")
    started,

    @SerializedName("jb.success")
    success,

    @SerializedName("jb.failed")
    failed,

    @SerializedName("jb.continue")
    continuee,

    @SerializedName("send.continue")
    payload,

    @SerializedName("send.payload.request")
    payloadReq,

    @SerializedName("send.pending")
    pending
}

val Minum.bg: Color get() {
    return when(this) {
        Minum.initiated -> Constants.Color.SECONDARY
        Minum.started -> Constants.Color.Mi.BLUE
        Minum.success -> Constants.Color.Mi.GREEN
        Minum.failed -> Constants.Color.Mi.RED
        Minum.continuee -> Constants.Color.Mi.PURPLE
        Minum.payload -> Constants.Color.Mi.BLUE
        Minum.payloadReq -> Constants.Color.Mi.YELLOW
        Minum.pending -> Constants.Color.Mi.ORANGE
    }
       /* switch (this) {
            case .initiated:
            return Color("secondary")
            case .success:
            return Color("mi.green")
            case .failed:
            return Color("mi.red")
            case .payload:
            return Color("mi.blue")
            case .pending:
            return Color("mi.orange")
            case .started:
            return Color("mi.blue")
            case .continuee:
            return Color("mi.purple")
            case .payloadReq:
            return Color("mi.yellow")
        }
    }*/
}