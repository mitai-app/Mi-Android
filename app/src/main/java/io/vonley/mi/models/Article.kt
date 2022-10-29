package io.vonley.mi.models

import androidx.compose.ui.graphics.Color
import io.vonley.mi.Constants

sealed class Article(
    open val name: String,
    open val description: String,
    open val icon: String?,
    open val link: String?,
) {

    abstract val background: Color

    data class BasicArticle(
        override val name: String,
        override val description: String,
        override val icon: String?,
        override val link: String?,
    ) : Article(name, description, icon, link) {

        override val background: Color = Constants.Color.TERTIARY //tertiary

    }

    data class ProfileArticle(
        override val name: String,
        override val description: String,
        override val icon: String?,
        override val link: String?,
    ) : Article(name, description, icon, link) {

        override val background: Color = Constants.Color.QUINARY // quinary
    }


    data class PayloadArticle(
        override val name: String,
        override val description: String,
        override val icon: String?,
        override val link: String?,
        val banner: String,
        val source: String,
        val download: Map<String, String>
    ) : Article(name, description, icon, link) {

        override val background: Color = Constants.Color.QUATERNARY //quaternary
    }


    data class ReadableArticle(
        override val name: String,
        override val description: String,
        override val icon: String?,
        override val link: String?,
        val author: String,
        val summary: String,
        val paragraphs: Array<String>,
        val credit: String,
    ) : Article(name, description, icon, link) {

        override val background: Color = Constants.Color.PURPLE_DARK //purpleDark

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is ReadableArticle) return false

            if (name != other.name) return false
            if (description != other.description) return false
            if (icon != other.icon) return false
            if (link != other.link) return false
            if (background != other.background) return false
            if (author != other.author) return false
            if (summary != other.summary) return false
            if (!paragraphs.contentEquals(other.paragraphs)) return false
            if (credit != other.credit) return false

            return true
        }

        override fun hashCode(): Int {
            var result = name.hashCode()
            result = 31 * result + description.hashCode()
            result = 31 * result + (icon?.hashCode() ?: 0)
            result = 31 * result + (link?.hashCode() ?: 0)
            result = 31 * result + background.hashCode()
            result = 31 * result + author.hashCode()
            result = 31 * result + summary.hashCode()
            result = 31 * result + paragraphs.contentHashCode()
            result = 31 * result + credit.hashCode()
            return result
        }
    }

}