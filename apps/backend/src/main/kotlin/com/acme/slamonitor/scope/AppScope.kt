package com.acme.slamonitor.scope

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.slf4j.LoggerFactory
import kotlin.coroutines.CoroutineContext


/**
 * Application-level scope: живёт вместе с приложением и корректно отменяется при shutdown.
 */
class AppScope : CoroutineScope, AutoCloseable {
    private val log = LoggerFactory.getLogger(AppScope::class.java)

    private val job = SupervisorJob()
    private val handler = CoroutineExceptionHandler { _, e ->
        log.error("Unhandled exception in background coroutine", e)
    }

    override val coroutineContext: CoroutineContext =
        job + handler + CoroutineName("app-scope")

    override fun close() {
        job.cancel()
    }
}
