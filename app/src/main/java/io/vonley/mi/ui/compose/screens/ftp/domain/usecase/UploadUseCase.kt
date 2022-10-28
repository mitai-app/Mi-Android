package io.vonley.mi.ui.compose.screens.ftp.domain.usecase

import io.vonley.mi.ui.compose.screens.ftp.domain.repository.FTPRepository
import java.io.InputStream
import javax.inject.Inject

class UploadUseCase @Inject constructor(private val ftpRepository: FTPRepository) {

    operator fun invoke(file: String, stream: InputStream) = ftpRepository.upload(file, stream)

}