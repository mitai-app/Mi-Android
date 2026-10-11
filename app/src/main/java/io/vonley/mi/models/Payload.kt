package io.vonley.mi.models

import java.io.File

/**
 * A payload staged on local storage so it can be re-read (retries, HTTP range
 * requests from the console's package installer) instead of being a one-shot stream.
 */
data class Payload(
    val name: String,
    val file: File,
) {
    val size: Long get() = file.length()
    val isBin: Boolean get() = name.endsWith(".bin", ignoreCase = true)
    val isPkg: Boolean get() = name.endsWith(".pkg", ignoreCase = true)
}
