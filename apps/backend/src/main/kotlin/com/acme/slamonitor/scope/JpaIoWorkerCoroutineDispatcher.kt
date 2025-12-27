package com.acme.slamonitor.scope

import com.acme.slamonitor.configuration.VIRTUAL_THREAD_DISPATCHER_BEAN_NAME
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component
class JpaIoWorkerCoroutineDispatcher(
    private val appScope: AppScope,
    @param:Qualifier(VIRTUAL_THREAD_DISPATCHER_BEAN_NAME)
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
