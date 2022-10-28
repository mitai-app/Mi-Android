package io.vonley.mi.ui.compose.screens.ftp.domain.usecase

import io.vonley.mi.ui.compose.screens.ftp.domain.repository.FTPRepository
import kotlinx.coroutines.flow.flow
import org.apache.commons.net.ftp.FTPFile
import java.io.InputStream
import javax.inject.Inject

class ReplaceUseCase @Inject constructor(private val ftpRepository: FTPRepository) {

    operator fun invoke(file: FTPFile, stream: InputStream) = flow {
        val event = ftpRepository.replace(file, stream)
        emit(event)
    }

}