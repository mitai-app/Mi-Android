package io.vonley.mi.ui.compose.screens.ftp.domain.usecase

import io.vonley.mi.ui.compose.screens.ftp.domain.repository.FTPRepository
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.apache.commons.net.ftp.FTPFile
import javax.inject.Inject

class NavigateUseCase @Inject constructor(private val ftpRepository: FTPRepository) {

    operator fun invoke(file: FTPFile) = flow {
        val event = ftpRepository.navigateTo(file)
        emit(event)
    }

    operator fun invoke(file: String) = flow {
        val event = ftpRepository.navigateTo(file)
        emit(event)
    }

}