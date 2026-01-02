package com.acme.slamonitor.scope

import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.slf4j.LoggerFactory

/**
 * Корутинный scope приложения с supervisor job и обработкой необработанных ошибок.
 */
class AppScope : CoroutineScope, AutoCloseable {

    private val job = SupervisorJob()
    private val handler = CoroutineExceptionHandler { _, e ->
        LOG.error("Unhandled exception in background coroutine", e)
    }

    override val coroutineContext: CoroutineContext =
        job + handler + CoroutineName("app-scope")

    /**
     * Отменяет все корутины, запущенные в этом scope.
     */
    override fun close() {
        job.cancel()
    }
}

private val LOG by lazy { LoggerFactory.getLogger(AppScope::class.java) }
