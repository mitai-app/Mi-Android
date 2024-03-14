package io.vonley.mi.di.network.protocols.webman

import io.vonley.mi.di.network.PSXService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import java.io.BufferedReader
import java.io.OutputStream
import java.io.PrintWriter
import kotlin.coroutines.CoroutineContext

class WebManImpl(override val service: PSXService) : Webman {

    private val job = Job()
    override val pw: OutputStream
        get() = System.err
    override val br: BufferedReader
        get() = System.`in`.bufferedReader()

    override val coroutineContext: CoroutineContext = Dispatchers.IO + job
}