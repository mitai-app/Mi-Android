package io.vonley.mi.di.network.protocols.ps3mapi.models

data class PS3MAPIResponse(
    val success: Boolean = false,
    val response: String,
    val code: Code?
){

    enum class SYSCALL8MODE {
        ENABLED, ONLY_COBRAMAMBA_AND_PS3API_ENABLED, ONLY_PS3MAPI_ENABLED, FAKEDISABLED, DISABLED
    }

    enum class Code(val value: Int, explain: String = "") {
        PS3MAPI_OK_DATA_CONNECTION_ALREADY_OPEN(125),
        PS3MAPI_OK_MEMORY_STATUS(150, "Binary status okay; about to open connection."),
        PS3MAPI_OK_SUCCESSFUL_COMMAND(200, "The requested action has been successfully completed."),
        PS3MAPI_OK_MGR_SERVER_CONNECTING(220, "PS3 Manager API Server v1."),
        PS3MAPI_OK_SERVICE_CLOSING_CONTROL(221, "Service closing control connection."),
        PS3MAPI_OK_CLOSING_DATA_CONNECTION(226, "Closing data connection. Requested binary action successful."),
        PS3MAPI_OK_ENTERING_PASSIVE_MODE(227, ""),
        PS3MAPI_OK_MGR_SERVER_CONNECTED(230, "Connected to PS3 Manager API Server."),
        PS3MAPI_OK_MEMORY_ACTION_COMPLETED(250),
        PS3MAPI_OK_MEMORY_ACTION_PENDING(350),
        PS3MAPI_ERROR_425(425, "Can't open data connection."),
        PS3MAPI_ERROR_451(451, "Requested action aborted. Local error in processing."),
        PS3MAPI_ERROR_501(501, "Requested action not taken."),
        PS3MAPI_ERROR_502(502, "Syntax error in parameters"),
        PS3MAPI_ERROR_550(550, "Requested action not taken."),
    }

    companion object {


        private fun findResponse(value: Int) = Code.values().find { c -> c.value == value }

        private fun parseResponse(success: Boolean, response: String): PS3MAPIResponse {
            if (success) {
                val responseCode = Integer.valueOf(response.substring(0, 3)).toInt()
                var buffer = response.substring(4).replace("\r", "").replace("\n", "")
                if (buffer.contains("OK: ")) {
                    buffer = buffer.replace("OK: ", "")
                }
                return create(success, buffer, findResponse(responseCode))
            }
            return create(success, response, null)
        }

        fun parse(
            response: String,
        ): PS3MAPIResponse {
            return parseResponse(true, response)
        }

        fun create(
            success: Boolean,
            response: String,
            code: Code?
        ): PS3MAPIResponse {
            return PS3MAPIResponse(success, response, code)
        }
    }
}
