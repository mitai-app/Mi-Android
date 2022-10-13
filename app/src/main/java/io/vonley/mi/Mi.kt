package io.vonley.mi

import android.os.Build
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase

object Mi {

    enum class MiEvent(private val event: String) {
        MI_SERVICE_START("mi_service_start"),
        MI_SERVICE_END("mi_service_end"),

        JB_SERVER_START("mi_jb_server_start"),
        JB_SERVER_END("mi_jb_server_end"),

        JB_SUCCESS("mi_jb_success"),
        JB_STARTED("mi_jb_started"),
        JB_INITIATED("mi_jb_initiated"),
        JB_CONTINUE("mi_jb_continue"),
        JB_PAYLOAD_REQUEST("mi_jb_send_payload_request"),
        JB_PAYLOAD("mi_jb_send_payload"),
        JB_PENDING("mi_jb_send_pending"),
        JB_FAILURE("mi_jb_failure"),

        CONSOLE("mi_console"),
        CONSOLE_SERVICES("mi_console_service"),

        CONSOLE_SERVICE_START("mi_console_service_start"),
        CONSOLE_SERVICE_END("mi_console_service_end");

        override fun toString(): String {
            return event
        }
    }

    private val analytics: FirebaseAnalytics = Firebase.analytics

    fun log(
        event: MiEvent,
        vararg params: Pair<String, Any>
    ) {
        analytics.logEvent(event.toString()) {
            params.onEach { pair ->
                when (pair.second) {
                    is String -> {
                        param(pair.first, pair.second as String)
                    }
                    is Long -> {
                        param(pair.first, pair.second as Long)
                    }
                    is Bundle -> {
                        param(pair.first, pair.second as Bundle)
                    }
                    is Double -> {
                        param(pair.first, pair.second as Double)
                    }
                    is Array<*> -> {
                        if ((pair.second as? Array<Bundle>) != null) {
                            param(pair.first, pair.second as Array<Bundle>)
                        }
                    }
                    else -> param(pair.first, pair.second.toString())
                }
            }
            param("Model", Build.MODEL)
            param("Device", Build.DEVICE)
            param("Device", Build.SUPPORTED_ABIS.joinToString())
            param("Version", Build.VERSION.SDK_INT.toLong())
        }
    }

}