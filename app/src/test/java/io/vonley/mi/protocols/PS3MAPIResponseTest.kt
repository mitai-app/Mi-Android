package io.vonley.mi.protocols

import io.vonley.mi.di.network.protocols.ps3mapi.models.PS3MAPIResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class PS3MAPIResponseTest {

    @Test
    fun `parses code and message`() {
        val response = PS3MAPIResponse.parse("220 OK: PS3 Manager API Server v1.\r\n")
        assertEquals(PS3MAPIResponse.Code.PS3MAPI_OK_MGR_SERVER_CONNECTING, response.code)
        assertEquals("PS3 Manager API Server v1.", response.response)
    }

    @Test
    fun `short or non numeric lines do not throw`() {
        val bare = PS3MAPIResponse.parse("200")
        assertEquals(PS3MAPIResponse.Code.PS3MAPI_OK_SUCCESSFUL_COMMAND, bare.code)
        assertEquals("", bare.response)

        val garbage = PS3MAPIResponse.parse("hi")
        assertFalse(garbage.success)
        assertNull(garbage.code)
    }
}
