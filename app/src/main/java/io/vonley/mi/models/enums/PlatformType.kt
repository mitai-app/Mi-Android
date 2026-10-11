package io.vonley.mi.models.enums

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class PlatformType(vararg val features: Feature) : Parcelable {
    UNKNOWN(
        Feature.FTP
    ),
    PS3(
        Feature.PS3MAPI, Feature.WEBMAN, Feature.CCAPI
    ),
    PS4(
        Feature.GOLDENHEN, Feature.NETCAT, Feature.ORBISAPI, Feature.RPI, Feature.KLOG
    )
}