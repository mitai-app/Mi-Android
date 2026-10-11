package io.vonley.mi.ui.screens.consoles.domain.usecase

import io.vonley.mi.ui.screens.consoles.domain.model.Console
import io.vonley.mi.ui.screens.consoles.domain.repository.ConsoleRepository
import javax.inject.Inject

class AddConsoleUseCase @Inject constructor(
    private val repo: ConsoleRepository
) {
    suspend operator fun invoke(input: String): Result<Console> {
        val ip = input.trim()
        if (!isValidIpv4(ip)) {
            return Result.failure(IllegalArgumentException("\"$ip\" is not a valid IPv4 address"))
        }
        return runCatching { repo.addConsole(ip) }
    }

    companion object {
        fun isValidIpv4(ip: String): Boolean {
            val parts = ip.split(".")
            return parts.size == 4 && parts.all { part ->
                part.isNotEmpty() && part.length <= 3 && part.all(Char::isDigit) && part.toInt() in 0..255
            }
        }
    }
}
