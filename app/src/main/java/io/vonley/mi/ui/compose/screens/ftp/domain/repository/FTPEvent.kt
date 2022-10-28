package io.vonley.mi.ui.compose.screens.ftp.domain.repository

import org.apache.commons.net.ftp.FTPFile

sealed class FTPEvent {

    data class Delete(val filename: String): FTPEvent()
    data class Download(var filename: String, val data: ByteArray = byteArrayOf()): FTPEvent() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Download) return false

            if (filename != other.filename) return false
            if (!data.contentEquals(other.data)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = filename.hashCode()
            result = 31 * result + data.contentHashCode()
            return result
        }
    }
    data class Rename(val originalFilename: FTPFile, var toInputName: String): FTPEvent()
    data class Replace(var filename: String): FTPEvent()
    data class Upload(var filename: String): FTPEvent()
    data class WorkingDir(var path: String): FTPEvent()
    object None: FTPEvent()

}