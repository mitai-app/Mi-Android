package io.vonley.mi.di.network.impl

import io.vonley.mi.models.Payload

data class RPIResponse(
    val status: Int,
    val content: Payload? = null,
    private val contentType: String = "application/x-newton-compatible-pkg"
)