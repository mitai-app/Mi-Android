package io.vonley.mi.models.enums

enum class Event(var filename: String, var data: Any? = null) {
    DELETE(""),
    DOWNLOAD(""),
    RENAME(""),
    REPLACE(""),
    UPLOAD("")
}