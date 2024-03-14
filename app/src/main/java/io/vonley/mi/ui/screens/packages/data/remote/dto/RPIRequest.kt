package io.vonley.mi.ui.screens.packages.data.remote.dto

import io.vonley.mi.di.network.impl.Method

data class RPIRequest(
    val method: Method,
    val path: String,
    val headers: Map<String, String>,
    val content: String,
    val param: String
)