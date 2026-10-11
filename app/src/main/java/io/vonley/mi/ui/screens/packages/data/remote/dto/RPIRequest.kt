package io.vonley.mi.ui.screens.packages.data.remote.dto

import io.vonley.mi.di.network.impl.Method

data class RPIRequest(
    val method: Method,
    val path: String,
    /** Header names are lower-cased. */
    val headers: Map<String, String>,
) {
    companion object {
        /** Parses the request line and headers of an HTTP/1.x request head. */
        fun parse(head: List<String>): RPIRequest? {
            val requestLine = head.firstOrNull()?.split(" ") ?: return null
            if (requestLine.size < 2) return null
            val method = Method.entries.find { it.name == requestLine[0] } ?: return null
            val path = requestLine[1].substringBefore("?")
            val headers = head.drop(1)
                .mapNotNull { line ->
                    val separator = line.indexOf(':')
                    if (separator <= 0) null
                    else line.substring(0, separator).trim().lowercase() to line.substring(separator + 1).trim()
                }
                .toMap()
            return RPIRequest(method, path, headers)
        }

        /**
         * Resolves a single "bytes=start-end" range against a resource of [size] bytes.
         * Returns null when there is no usable range (serve the whole file).
         */
        fun parseRange(header: String?, size: Long): LongRange? {
            if (header == null || size <= 0) return null
            val spec = header.trim().removePrefix("bytes=").split(",").first().trim()
            val dash = spec.indexOf('-')
            if (dash < 0) return null
            val startText = spec.substring(0, dash).trim()
            val endText = spec.substring(dash + 1).trim()
            val range = if (startText.isEmpty()) {
                // suffix range: last N bytes
                val suffix = endText.toLongOrNull() ?: return null
                (size - suffix).coerceAtLeast(0) until size
            } else {
                val start = startText.toLongOrNull() ?: return null
                val end = endText.toLongOrNull()?.coerceAtMost(size - 1) ?: (size - 1)
                start..end
            }
            return range.takeIf { it.first in 0 until size && !it.isEmpty() }
        }
    }
}
