package io.vonley.mi

import androidx.compose.ui.graphics.Color
import io.vonley.mi.models.Article

object Constants {

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
}