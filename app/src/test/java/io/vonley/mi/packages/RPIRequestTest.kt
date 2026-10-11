package io.vonley.mi.packages

import io.vonley.mi.di.network.impl.Method
import io.vonley.mi.ui.screens.packages.data.remote.dto.RPIRequest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RPIRequestTest {

    @Test
    fun `parses request line and lower-cases headers`() {
        val request = RPIRequest.parse(
            listOf("GET /pkg/game.pkg?x=1 HTTP/1.1", "Host: 192.168.1.2:12800", "Range: bytes=0-99")
        )!!
        assertEquals(Method.GET, request.method)
        assertEquals("/pkg/game.pkg", request.path)
        assertEquals("bytes=0-99", request.headers["range"])
    }

    @Test
    fun `rejects malformed request lines`() {
        assertNull(RPIRequest.parse(emptyList()))
        assertNull(RPIRequest.parse(listOf("BREW /pot HTTP/1.1")))
    }

    @Test
    fun `resolves closed, open and suffix ranges`() {
        assertEquals(100L..199L, RPIRequest.parseRange("bytes=100-199", 1000))
        assertEquals(500L..999L, RPIRequest.parseRange("bytes=500-", 1000))
        assertEquals(900L..999L, RPIRequest.parseRange("bytes=-100", 1000))
        // end past the file is clamped
        assertEquals(990L..999L, RPIRequest.parseRange("bytes=990-5000", 1000))
    }

    @Test
    fun `ignores missing or unsatisfiable ranges`() {
        assertNull(RPIRequest.parseRange(null, 1000))
        assertNull(RPIRequest.parseRange("bytes=1000-1100", 1000))
        assertNull(RPIRequest.parseRange("bytes=abc", 1000))
    }
}
