package com.acme.slamonitor.scope

import com.acme.slamonitor.persistence.util.JpaIoTransactionalLogging
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

open class JpaIoWorkerCoroutineDispatcher(
    private val appScope: AppScope,
    private val dispatcher: CoroutineDispatcher,
    private val jpaWorkerLogging: JpaIoTransactionalLogging
) {

    fun executeWithTransactionalConsumer(logMessage: String, block: () -> Unit) {
        appScope.launch(dispatcher) {
            jpaWorkerLogging.runConsumer(logMessage) { block() }
        }
    }

    suspend fun <T> executeWithTransactionalSupplier(logMessage: String, block: () -> T): T =
        withContext(dispatcher) {
            jpaWorkerLogging.runSupplier(logMessage) { block() }
        }

}
