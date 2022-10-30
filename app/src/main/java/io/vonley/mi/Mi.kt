package io.vonley.mi

import android.os.Build
import android.os.Bundle
import androidx.compose.ui.graphics.Color
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import io.vonley.mi.models.Article

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



    val ARTICLES = arrayOf(
        Article.BasicArticle(
            name = "What is Mi?",
            description = "Learn more about what you can do with Mi.",
            icon = "question",
            link = "https://github.com/mitai-app"
        ),
        Article.PayloadArticle(
            name = "Recommended Payload",
            description = "Goldhen, the all-time recommended payload develop by sistro",
            icon = null,
            link = "https://github.com/GoldHen/GoldHen",
            banner = "",
            source = "",
            mapOf()
        ),
        Article.BasicArticle(
            name = "Invite your friend",
            description = "This bad boy will send payloads to your ps4 and manage your ps3",
            icon = "help",
            link = "https://github.com/mitai-app"
        ),
        Article.ProfileArticle(
            name = "Follow me on Twitter!",
            description = "Stay tuned for more updates!",
            link = "https://twitter.com/MrSmithyx",
            icon = null
        ),
        Article.BasicArticle(
            name = "Special Thanks",
            description = "Specials thanks to the jailbreak scene developers for making this possible.",
            icon = null,
            link = "https://github.com/mitai-app"
        ),
        Article.ReadableArticle(
            name = "Support Mi",
            description = "Visit the project page to see how you can support Mi.",
            icon = "team",
            link = "https://ko-fi.com/mrsmithyx",
            author = "Mr Smithy x",
            summary = "We will talk more about this",
            paragraphs = arrayOf(
                "This is cool"
            ),
            credit = "Mr Smithy x"
        )
    )

    object Color {

        val TERTIARY = Color(0xFF05668D)
        val QUINARY = Color(0xFF02172B)
        val QUATERNARY = Color(0xFFC1292D)
        val PURPLE_DARK = Color(0xFF2D1370)
        val SECONDARY = Color(0xFFD1CCDC)

        object Mi {
            val BLUE = Color(0xFF5595FF)
            val RED = Color(0xFFC14A49)
            val GREEN = Color(0xFF48B06A)
            val PURPLE = Color(0xFF615497)
            val ORANGE = Color(0xFFFF865D)
            val YELLOW = Color(0xFFE6C354)
        }

    }

}