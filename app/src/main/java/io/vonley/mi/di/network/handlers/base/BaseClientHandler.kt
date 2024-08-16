package io.vonley.mi.di.network.handlers.base

import androidx.lifecycle.Observer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import io.vonley.mi.di.network.handlers.ClientHandler
import io.vonley.mi.di.network.listeners.OnClientListener
import kotlin.coroutines.CoroutineContext

abstract class BaseClientHandler <N : OnClientListener, D> : CoroutineScope, Observer<D>,
    ClientHandler {

    private val job = Job()

    val listeners = HashMap<Class<*>, N>()

    override fun onChanged(value: D) {
        if (value != null) {
            handle(value)
        }
    }

    override val coroutineContext: CoroutineContext = job + Dispatchers.IO

    protected abstract fun handle(event: D): Job

}