package io.vonley.mi.di.network.impl

data class RPIRequest(
    val method: Method,
    val path: String,
    val headers: Map<String, String>,
    val content: String,
    val param: String
)