package io.vonley.mi

import org.junit.Assert
import org.junit.Test

class KLogRegexText {
    //(["'])(?:\\.|[^\\])*?\1
    val name = "Mr-Smithy-x"
    val exampleString = "D/KLogImpl: <118>[SceUserService] User \"$name\" (0x10000000) logged in"
    val username = "([\"'])(?:\\\\.|[^\\\\])*?\\1"

    @Test
    fun extractUsernameRegex() {
        val extractPlaystation = username.toRegex()
        val matchEntire = extractPlaystation.findAll(exampleString).flatMap { it.groupValues }.distinct().filter { it.isNotEmpty() }.toList().firstOrNull();
        if(matchEntire != null){
            println(matchEntire)
        } else println("regex failed")
    }
}