package io.vonley.mi.models

import java.io.InputStream

/**
 * status: -1 failed, 0 nothing, 1 succeeded
 */
data class Payload(
    val name: String,
    var stream: InputStream,
    var status: Int = 0,
    var size: Long = 0,
                   ) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Payload) return false
        return true
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}