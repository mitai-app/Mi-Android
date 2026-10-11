package io.vonley.mi.consoles

import io.vonley.mi.models.enums.PlatformType
import io.vonley.mi.ui.screens.consoles.domain.model.Client
import io.vonley.mi.ui.screens.consoles.domain.model.Console
import io.vonley.mi.ui.screens.consoles.domain.repository.ConsoleRepository
import io.vonley.mi.ui.screens.consoles.domain.usecase.AddConsoleUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AddConsoleUseCaseTest {

    private class FakeRepo : ConsoleRepository {
        val added = mutableListOf<String>()
        override val target: StateFlow<Client?> = MutableStateFlow(null)
        override fun getMyConsoles(): Flow<List<Console>> = emptyFlow()
        override suspend fun addConsole(ip: String): Console {
            added += ip
            return Console(ip, ip, PlatformType.PS4, emptyList(), false, "wifi", pinned = true)
        }
        override suspend fun setPin(ip: String, pinned: Boolean) = Unit
        override fun selectConsole(console: Client) = Unit
        override fun refresh() = Unit
    }

    @Test
    fun `validates IPv4 addresses`() {
        assertTrue(AddConsoleUseCase.isValidIpv4("192.168.1.45"))
        assertTrue(AddConsoleUseCase.isValidIpv4("0.0.0.0"))
        assertFalse(AddConsoleUseCase.isValidIpv4("192.168.1"))
        assertFalse(AddConsoleUseCase.isValidIpv4("192.168.1.256"))
        assertFalse(AddConsoleUseCase.isValidIpv4("192.168.?.?"))
        assertFalse(AddConsoleUseCase.isValidIpv4("1.2.3.4.5"))
    }

    @Test
    fun `adds trimmed address and rejects invalid input`() = runTest {
        val repo = FakeRepo()
        val useCase = AddConsoleUseCase(repo)
        assertTrue(useCase(" 10.0.0.7 ").isSuccess)
        assertTrue(useCase("not an ip").isFailure)
        assertEquals(listOf("10.0.0.7"), repo.added)
    }
}
