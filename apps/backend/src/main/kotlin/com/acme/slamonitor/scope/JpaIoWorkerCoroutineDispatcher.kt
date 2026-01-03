package com.acme.slamonitor.scope

import com.acme.slamonitor.persistence.util.JpaIoTransactionalLogging
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Запускает JPA-операции в отдельном dispatcher с логированием транзакций.
 */
open class JpaIoWorkerCoroutineDispatcher(
    private val appScope: AppScope,
    private val dispatcher: CoroutineDispatcher,
    private val jpaWorkerLogging: JpaIoTransactionalLogging
) {

    /**
     * Выполняет блок без результата внутри транзакционного логирования.
     */
    fun executeWithTransactionalConsumer(logMessage: String, block: () -> Unit) {
        appScope.launch(dispatcher) {
            jpaWorkerLogging.runConsumer(logMessage) { block() }
        }
    }

    /**
     * Выполняет блок с результатом внутри транзакционного логирования.
     */
    suspend fun <T> executeWithTransactionalSupplier(logMessage: String, block: () -> T): T =
        withContext(dispatcher) {
            jpaWorkerLogging.runSupplier(logMessage) { block() }
        }

}
