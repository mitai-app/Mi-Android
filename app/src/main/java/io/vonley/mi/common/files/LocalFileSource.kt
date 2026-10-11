package io.vonley.mi.common.files

import android.net.Uri
import io.vonley.mi.models.Payload
import java.io.File
import java.io.InputStream

/** Moves files between the device and the app's staging/download areas. */
interface LocalFileSource {

    /** Copies a user-picked document into the cache so it can be re-read. */
    suspend fun stage(uri: Uri): Payload

    /** Writes [source] into the cache under [name]. */
    suspend fun stage(name: String, source: InputStream): Payload

    /** Destination for files pulled from the console, e.g. FTP downloads. */
    fun downloadTarget(name: String): File
}
