package io.vonley.mi.common.files

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import io.vonley.mi.models.Payload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream

class LocalFileSourceImpl(private val context: Context) : LocalFileSource {

    private val stagingDir: File
        get() = File(context.cacheDir, "payloads").apply { mkdirs() }

    override suspend fun stage(uri: Uri): Payload = withContext(Dispatchers.IO) {
        val name = displayName(uri)
        val input = context.contentResolver.openInputStream(uri)
            ?: throw FileNotFoundException("Unable to open $uri")
        input.use { stageBlocking(name, it) }
    }

    override suspend fun stage(name: String, source: InputStream): Payload = withContext(Dispatchers.IO) {
        source.use { stageBlocking(name, it) }
    }

    override fun downloadTarget(name: String): File {
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir
        return File(dir, sanitize(name))
    }

    private fun stageBlocking(name: String, source: InputStream): Payload {
        val file = File(stagingDir, sanitize(name))
        file.outputStream().use { source.copyTo(it) }
        return Payload(name, file)
    }

    private fun displayName(uri: Uri): String {
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { cursor ->
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0 && cursor.moveToFirst()) cursor.getString(index)?.let { return it }
            }
        return uri.lastPathSegment?.substringAfterLast('/') ?: "payload"
    }

    /** Keeps a remote-supplied name from escaping the target directory. */
    private fun sanitize(name: String): String =
        name.substringAfterLast('/').substringAfterLast('\\').ifBlank { "payload" }
}
