package io.vonley.mi.ui.compose.screens.ftp.domain.usecase

import io.vonley.mi.ui.compose.screens.ftp.domain.repository.FTPRepository
import kotlinx.coroutines.flow.flow
import org.apache.commons.net.ftp.FTPFile
import javax.inject.Inject

class DeleteUseCase @Inject constructor(private val ftpRepository: FTPRepository) {

    operator fun invoke(file: FTPFile) = flow {
        val event = ftpRepository.delete(file)
        emit(event)
    }

}