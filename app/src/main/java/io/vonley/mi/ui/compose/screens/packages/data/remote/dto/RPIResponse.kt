package io.vonley.mi.ui.compose.screens.packages.data.remote.dto

import io.vonley.mi.models.Payload

data class RPIResponse(
    val status: Int,
    val content: Payload? = null,
    private val contentType: String = "application/x-newton-compatible-pkg"
)