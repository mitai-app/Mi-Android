package io.vonley.mi.common.coroutines

import io.vonley.mi.extensions.e
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

/**
 * Context for long-lived singletons that launch background work.
 * A plain Job() lets one failing child cancel every sibling and an uncaught
 * exception crash the process; this keeps children isolated and logs failures.
 */
fun supervisedIoContext(tag: String): CoroutineContext =
    SupervisorJob() + Dispatchers.IO + CoroutineExceptionHandler { _, throwable ->
        "Unhandled coroutine failure: ${throwable.message}".e(tag, throwable)
    }
