package io.vonley.mi.ui.compose.screens.ftp.domain.usecase

import io.vonley.mi.ui.compose.screens.ftp.domain.repository.FTPRepository
import org.apache.commons.net.ftp.FTPFile
import javax.inject.Inject

class DownloadUseCase @Inject constructor(private val ftpRepository: FTPRepository) {

    operator fun invoke(file: FTPFile) = ftpRepository.download(file)

}